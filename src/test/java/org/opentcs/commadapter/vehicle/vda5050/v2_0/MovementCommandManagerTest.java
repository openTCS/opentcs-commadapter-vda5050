/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Edge;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Node;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionStatus;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.BatteryState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.EStop;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.EdgeState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.NodeState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.SafetyState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Unit tests for {@link MovementCommandManager}.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class MovementCommandManagerTest {

  /**
   * The movement command manager to test.
   */
  private MovementCommandManager manager;
  /**
   * The callback for finished commands.
   */
  private Consumer<MovementCommand> callback;

  @BeforeEach
  @SuppressWarnings("unchecked")
  public void setup() {

    callback = mock(Consumer.class);
    manager = new MovementCommandManager();
  }

  @Test
  public void finishNonFinalMovementIfNodeAndEdgeStatesAreEmpty() {
    MovementCommand command = new DummyMovementCommand(new Point("source-point"),
                                                       new Point("dest-point"),
                                                       false);
    Order order
        = new Order(
            "some-order-id",
            0L,
            List.of(
                new Node("source-point", 0L, true, List.of()),
                new Node("dest-point", 2L, true, List.of())
            ),
            List.of(
                new Edge("some-path", 1L, true, "source-point", "dest-point", List.of())
            )
        );
    manager.enqueue(new OrderAssociation(order, command));

    manager.onStateMessage(
        new State("some-order-id", 0L, "", 0L, List.of(), List.of(), false, false, List.of(),
                  new BatteryState(0.0, false), OperatingMode.AUTOMATIC, List.of(), List.of(),
                  new SafetyState(EStop.NONE, false)),
        callback
    );

    verify(callback, times(1)).accept(command);
  }

  @Test
  public void keepMovementCommandIfEdgeIsStillInEdgeStates() {
    MovementCommand command = new DummyMovementCommand(new Point("source-point"),
                                                       new Point("dest-point"),
                                                       false);
    Order order
        = new Order(
            "some-order-id",
            0L,
            List.of(
                new Node("source-point", 0L, true, List.of()),
                new Node("dest-point", 2L, true, List.of())
            ),
            List.of(
                new Edge("some-path", 1L, true, "source-point", "dest-point", List.of())
            )
        );
    manager.enqueue(new OrderAssociation(order, command));

    manager.onStateMessage(
        new State("some-order-id", 0L, "", 0L, List.of(),
                  List.of(new EdgeState("some-path", 1L, true)),
                  false, false, List.of(),
                  new BatteryState(0.0, false), OperatingMode.AUTOMATIC, List.of(), List.of(),
                  new SafetyState(EStop.NONE, false)),
        callback
    );

    verifyNoInteractions(callback);
  }

  @Test
  public void keepMovementCommandIfNodesAreStillInNodeStates() {
    MovementCommand command = new DummyMovementCommand(new Point("source-point"),
                                                       new Point("dest-point"),
                                                       false);
    Order order
        = new Order(
            "some-order-id",
            0L,
            List.of(
                new Node("source-point", 0L, true, List.of()),
                new Node("dest-point", 2L, true, List.of())
            ),
            List.of(
                new Edge("some-path", 1L, true, "source-point", "dest-point", List.of())
            )
        );
    manager.enqueue(new OrderAssociation(order, command));

    manager.onStateMessage(
        new State("some-order-id", 0L, "", 0L,
                  List.of(
                      new NodeState("source-point", 0L, true),
                      new NodeState("dest-point", 2L, true)
                  ),
                  List.of(), false, false, List.of(),
                  new BatteryState(0.0, false), OperatingMode.AUTOMATIC, List.of(), List.of(),
                  new SafetyState(EStop.NONE, false)),
        callback
    );

    verifyNoInteractions(callback);
  }

  @Test
  public void finishMovementCommandIfStateContainsNodesAndEdgesWithDifferentSequenceIds() {
    MovementCommand command = new DummyMovementCommand(new Point("source-point"),
                                                       new Point("dest-point"),
                                                       false);
    Order order
        = new Order(
            "some-order-id",
            0L,
            List.of(
                new Node("source-point", 0L, true, List.of()),
                new Node("dest-point", 2L, true, List.of())
            ),
            List.of(
                new Edge("some-path", 1L, true, "source-point", "dest-point", List.of())
            )
        );
    manager.enqueue(new OrderAssociation(order, command));

    manager.onStateMessage(
        new State("some-order-id", 0L, "", 0L,
                  List.of(
                      new NodeState("source-point", 3L, true),
                      new NodeState("dest-point", 5L, true)
                  ),
                  List.of(new EdgeState("some-path", 4L, true)),
                  false, false, List.of(),
                  new BatteryState(0.0, false), OperatingMode.AUTOMATIC, List.of(), List.of(),
                  new SafetyState(EStop.NONE, false)),
        callback
    );

    verify(callback, times(1)).accept(command);
  }

  @Test
  public void keepMovementCommandWithDifferentOrderId() {
    MovementCommand command = new DummyMovementCommand(new Point("source-point"),
                                                       new Point("dest-point"),
                                                       false);
    Order order
        = new Order(
            "some-order-id",
            0L,
            List.of(
                new Node("source-point", 0L, true, List.of()),
                new Node("dest-point", 2L, true, List.of())
            ),
            List.of(
                new Edge("some-path", 1L, true, "source-point", "dest-point", List.of())
            )
        );
    manager.enqueue(new OrderAssociation(order, command));

    manager.onStateMessage(
        new State("some-OTHER-order-id", 0L, "", 0L,
                  List.of(),
                  List.of(),
                  false, false, List.of(),
                  new BatteryState(0.0, false), OperatingMode.AUTOMATIC, List.of(), List.of(),
                  new SafetyState(EStop.NONE, false)),
        callback
    );

    verifyNoInteractions(callback);
  }

  @ParameterizedTest
  @EnumSource(value = ActionStatus.class, names = {"INITIALIZING", "RUNNING", "WAITING"})
  public void finishNonFinalMovementCommandWithRelatedUnfinishedAction(ActionStatus actionStatus) {
    MovementCommand command = new DummyMovementCommand(new Point("source-point"),
                                                       new Point("dest-point"),
                                                       false);
    Order order
        = new Order(
            "some-order-id",
            0L,
            List.of(
                new Node("source-point", 0L, true, List.of()),
                new Node(
                    "dest-point",
                    2L,
                    true,
                    List.of(new Action("some-action-type", "some-action-id", BlockingType.NONE))
                )
            ),
            List.of(
                new Edge("some-path", 1L, true, "source-point", "dest-point", List.of())
            )
        );
    manager.enqueue(new OrderAssociation(order, command));

    manager.onStateMessage(
        new State("some-order-id", 0L, "", 0L,
                  List.of(),
                  List.of(),
                  false, false,
                  List.of(
                      new ActionState("some-action-id", "some-action-type", actionStatus)
                  ),
                  new BatteryState(0.0, false), OperatingMode.AUTOMATIC, List.of(), List.of(),
                  new SafetyState(EStop.NONE, false)),
        callback
    );

    verify(callback, times(1)).accept(command);
  }

  @ParameterizedTest
  @EnumSource(value = ActionStatus.class, names = {"INITIALIZING", "RUNNING", "WAITING"})
  public void finishNonFinalMovementWithUnrelatedUnfinishedAction(ActionStatus actionStatus) {
    MovementCommand command = new DummyMovementCommand(new Point("source-point"),
                                                       new Point("dest-point"),
                                                       false);
    Order order
        = new Order(
            "some-order-id",
            0L,
            List.of(
                new Node("source-point", 0L, true, List.of()),
                new Node(
                    "dest-point",
                    2L,
                    true,
                    List.of(new Action("some-action-type", "some-action-id", BlockingType.NONE))
                )
            ),
            List.of(
                new Edge("some-path", 1L, true, "source-point", "dest-point", List.of())
            )
        );
    manager.enqueue(new OrderAssociation(order, command));

    manager.onStateMessage(
        new State("some-order-id", 0L, "", 0L,
                  List.of(),
                  List.of(),
                  false, false,
                  List.of(
                      new ActionState("some-OTHER-action-id", "some-action-type", actionStatus)
                  ),
                  new BatteryState(0.0, false), OperatingMode.AUTOMATIC, List.of(), List.of(),
                  new SafetyState(EStop.NONE, false)),
        callback
    );

    verify(callback, times(1)).accept(command);
  }

  @ParameterizedTest
  @EnumSource(value = ActionStatus.class, names = {"INITIALIZING", "RUNNING", "WAITING"})
  public void finishFinalMovementWithUnrelatedUnfinishedAction(ActionStatus actionStatus) {
    MovementCommand command = new DummyMovementCommand(new Point("source-point"),
                                                       new Point("dest-point"),
                                                       true);
    Order order
        = new Order(
            "some-order-id",
            0L,
            List.of(
                new Node("source-point", 0L, true, List.of()),
                new Node(
                    "dest-point",
                    2L,
                    true,
                    List.of()
                )
            ),
            List.of(
                new Edge("some-path", 1L, true, "source-point", "dest-point", List.of())
            )
        );
    manager.enqueue(new OrderAssociation(order, command));

    manager.onStateMessage(
        new State("some-order-id", 0L, "", 0L,
                  List.of(),
                  List.of(),
                  false, false,
                  List.of(
                      new ActionState("some-OTHER-action-id", "some-action-type", actionStatus)
                  ),
                  new BatteryState(0.0, false), OperatingMode.AUTOMATIC, List.of(), List.of(),
                  new SafetyState(EStop.NONE, false)),
        callback
    );

    verify(callback, times(1)).accept(command);
  }

  @ParameterizedTest
  @EnumSource(value = ActionStatus.class, names = {"FINISHED", "FAILED"})
  public void finishFinalMovementWithRelatedFinishedAction(ActionStatus actionStatus) {
    MovementCommand command = new DummyMovementCommand(new Point("source-point"),
                                                       new Point("dest-point"),
                                                       true);
    Order order
        = new Order(
            "some-order-id",
            0L,
            List.of(
                new Node("source-point", 0L, true, List.of()),
                new Node(
                    "dest-point",
                    2L,
                    true,
                    List.of(new Action("some-action-type", "some-action-id", BlockingType.NONE))
                )
            ),
            List.of(
                new Edge("some-path", 1L, true, "source-point", "dest-point", List.of())
            )
        );
    manager.enqueue(new OrderAssociation(order, command));

    manager.onStateMessage(
        new State("some-order-id", 0L, "", 0L,
                  List.of(),
                  List.of(),
                  false, false,
                  List.of(
                      new ActionState("some-action-id", "some-action-type", actionStatus)
                  ),
                  new BatteryState(0.0, false), OperatingMode.AUTOMATIC, List.of(), List.of(),
                  new SafetyState(EStop.NONE, false)),
        callback
    );

    verify(callback, times(1)).accept(command);
  }

  @ParameterizedTest
  @EnumSource(value = ActionStatus.class, names = {"INITIALIZING", "RUNNING", "WAITING"})
  public void keepFinalMovementWithRelatedUnfinishedAction(ActionStatus actionStatus) {
    MovementCommand command = new DummyMovementCommand(new Point("source-point"),
                                                       new Point("dest-point"),
                                                       true);
    Order order
        = new Order(
            "some-order-id",
            0L,
            List.of(
                new Node("source-point", 0L, true, List.of()),
                new Node(
                    "dest-point",
                    2L,
                    true,
                    List.of(new Action("some-action-type", "some-action-id", BlockingType.NONE))
                )
            ),
            List.of(
                new Edge("some-path", 1L, true, "source-point", "dest-point", List.of())
            )
        );
    manager.enqueue(new OrderAssociation(order, command));

    manager.onStateMessage(
        new State("some-order-id", 0L, "", 0L,
                  List.of(),
                  List.of(),
                  false, false,
                  List.of(
                      new ActionState("some-action-id", "some-action-type", actionStatus)
                  ),
                  new BatteryState(0.0, false), OperatingMode.AUTOMATIC, List.of(), List.of(),
                  new SafetyState(EStop.NONE, false)),
        callback
    );

    verifyNoInteractions(callback);
  }

  private class DummyMovementCommand
      implements MovementCommand {

    private final Route.Step dummyStep;

    private boolean finalMovement;

    DummyMovementCommand(Point source, Point dest, boolean finalMovement) {
      this(source, dest);
      this.finalMovement = finalMovement;
    }

    DummyMovementCommand(Point source, Point dest) {
      Path path = null;
      if (source != null && dest != null) {
        path = new Path("Path-0001", source.getReference(), dest.getReference());
      }

      dummyStep = new Route.Step(
          path,
          source,
          dest,
          Vehicle.Orientation.FORWARD,
          0
      );
    }

    @Override
    public Route getRoute() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Route.Step getStep() {
      return dummyStep;
    }

    @Override
    public String getOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isWithoutOperation() {
      return true;
    }

    @Override
    public Location getOpLocation() {
      return null;
    }

    @Override
    public boolean isFinalMovement() {
      return finalMovement;
    }

    public void setFinalMovement(boolean finalMovement) {
      this.finalMovement = finalMovement;
    }

    @Override
    public Point getFinalDestination() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Location getFinalDestinationLocation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getFinalOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, String> getProperties() {
      return new HashMap<>();
    }
  }

}
