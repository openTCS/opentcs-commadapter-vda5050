/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_CUSTOM_ACTION_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_CUSTOM_DEST_ACTION_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_EXECUTABLE_ACTIONS_TAGS;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Node;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.Vehicle.Orientation;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Unit tests for {@link OrderMapper}.
 */
public class OrderMapperTest {

  private TransportOrder transportOrder;
  private Vehicle vehicle;
  private TCSObjectService objectService;
  /**
   * The order mapper being tested.
   */
  private OrderMapper mapper;

  @BeforeEach
  public void setup() {
    transportOrder
        = new TransportOrder(
            "some-order",
            List.of(
                new DriveOrder(new DriveOrder.Destination(new Point("some-point").getReference()))
            )
        )
            .withCurrentDriveOrderIndex(0);
    vehicle = new Vehicle("vehicle-0001")
        .withPrecisePosition(new Triple(0, 0, 0))
        .withTransportOrder(transportOrder.getReference());

    objectService = mock(TCSObjectService.class);
    when(objectService.fetchObject(Vehicle.class, vehicle.getReference()))
        .thenReturn(vehicle);
    when(objectService.fetchObject(TransportOrder.class, transportOrder.getReference()))
        .thenReturn(transportOrder);

    mapper = new OrderMapper(vehicle.getReference(),
                             s -> true,
                             objectService);
  }

  @Test
  public void shouldGenerateCorrectOrderWithTwoNodesAndOneEdge() {
    // setup a movement command
    Point sourcePoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002");
    MovementCommand command = new DummyMovementCommand(sourcePoint, destPoint);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getEdges().size(), is(1));
    assertThat(order.getNodes().get(0).getSequenceId(), is(0L));
    assertThat(order.getEdges().get(0).getSequenceId(), is(1L));
    assertThat(order.getNodes().get(1).getSequenceId(), is(2L));
  }

  @Test
  public void generateSingleNodeIfMovementCommandHasNoActualMovement() {
    // setup a movement command
    Point destPoint = new Point("Point-0001");
    MovementCommand command = new DummyMovementCommand(null, destPoint);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes(), hasSize(1));
    assertThat(order.getNodes().get(0).getSequenceId(), is(0L));
    assertThat(order.getEdges(), is(empty()));
  }

  @Test
  public void shouldGenerateCustomActions() {
    // setup a movement command
    Point sourcePoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.blockingType", "SOFT")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.parameters", "x = 234 | y = 567")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.blockingType", "SOFT")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameters", "x = 234 | y = 567");

    Path path = new Path("Path-0001", sourcePoint.getReference(), destPoint.getReference());
    Step step = new Step(path, sourcePoint, destPoint, Orientation.FORWARD, 0);

    MovementCommand command = new DummyMovementCommand(step);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().get(0).getActionType(), is("beep"));
    assertThat(order.getNodes().get(1).getActions().get(1).getActionType(), is("duck"));
  }

  @Test
  public void shouldGenerateCustomActionsForSourcePoints() {
    // setup a movement command
    Point sourcePoint = new Point("Point-0001")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.blockingType", "SOFT")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.parameters", "x = 234 | y = 567");
    Point destPoint = new Point("Point-0002")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.blockingType", "SOFT")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.parameters", "x = 234 | y = 567");

    Path path = new Path("Path-0001", sourcePoint.getReference(), destPoint.getReference());
    Step step = new Step(path, sourcePoint, destPoint, Orientation.FORWARD, 0);

    MovementCommand command = new DummyMovementCommand(step);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getNodes().get(0).getActions().size(), is(1));
    assertThat(order.getNodes().get(1).getActions().size(), is(1));
  }

  @Test
  public void shouldGenerateCustomActionsFromLinkedLocation() {
    // setup a movement command
    Point sourcePoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002");
    Location destLoc = new Location("Location-0002", new LocationType("Loc-type").getReference())
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.blockingType", "SOFT")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".ab.parameters", "x = 234 | y = 567");

    Path path = new Path("Path-0001", sourcePoint.getReference(), destPoint.getReference());
    Step step = new Step(path, sourcePoint, destPoint, Orientation.FORWARD, 0);

    DummyMovementCommand command = new DummyMovementCommand(step);
    command.setOpLocation(destLoc);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().size(), is(1));
    assertThat(order.getNodes().get(1).getActions().get(0).getActionType(), is("duck"));
  }

  @Test
  public void shouldGenerateCustomActionsFromDriveOrder() {
    // setup a movement command
    Point sourcePoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002");

    DummyMovementCommand command = new DummyMovementCommand(sourcePoint,
                                                            destPoint,
                                                            1000,
                                                            500);
    LocationType locType = new LocationType("loc-type");
    command.setOpLocation(new Location("location-0001", locType.getReference()));
    when(objectService.fetchObject(LocationType.class, locType.getReference()))
        .thenReturn(locType);

    command.setOperation("customOp");
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".customOp.blockingType", "SOFT");
    properties.put(PROPKEY_CUSTOM_DEST_ACTION_PREFIX + ".customOp.parameters", "x = 234 | y = 567");
    command.setProperties(properties);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().size(), is(1));
    assertThat(order.getNodes().get(1).getActions().get(0).getActionType(), is("customOp"));
  }

  @Test
  public void onlyAddActionsThatTheVehicleCanExecute() {
    // Setup mapper with a vehicle with executable actions.
    vehicle = vehicle
        .withProperty(ObjectProperties.PROPKEY_EXECUTABLE_ACTIONS_TAGS, "tag_beep | tag_duck");
    when(objectService.fetchObject(Vehicle.class, vehicle.getReference()))
        .thenReturn(vehicle);

    mapper = new OrderMapper(vehicle.getReference(),
                             new ExecutableActionsTagsPredicate(vehicle),
                             objectService);

    // setup a movement command
    Point sourcePoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "tag_duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02.tags", "tag_beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".03", "run")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".03.tags", "tag_run");
    MovementCommand command = new DummyMovementCommand(sourcePoint, destPoint);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().get(0).getActionType(), is("duck"));
    assertThat(order.getNodes().get(1).getActions().get(1).getActionType(), is("beep"));
  }

  @Test
  public void onlyAddActionsThatTheMovementCommandCanExecute() {
    // setup a movement command
    Point sourcePoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "tag_duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02.tags", "tag_beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".03", "run")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".03.tags", "tag_run");

    DummyMovementCommand command = new DummyMovementCommand(sourcePoint, destPoint);
    Map<String, String> properties = new HashMap<>();
    properties.put(PROPKEY_EXECUTABLE_ACTIONS_TAGS, "tag_beep | tag_duck");
    command.setProperties(properties);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().size(), is(2));
    assertThat(order.getNodes().get(1).getActions().get(0).getActionType(), is("duck"));
    assertThat(order.getNodes().get(1).getActions().get(1).getActionType(), is("beep"));
  }

  @Test
  public void destNodeAndSourceNodeOfConsecutiveOrdersMustMatch() {
    // setup a movement command
    Point startPoint = new Point("Point-0001");
    Point middlePoint = new Point("Point-0002");
    Point endPoint = new Point("Point-0003");

    MovementCommand commandOne = new DummyMovementCommand(startPoint, middlePoint);
    Order orderOne = mapper.toOrder(commandOne);

    MovementCommand commandTwo = new DummyMovementCommand(middlePoint, endPoint);
    Order orderTwo = mapper.toOrder(commandTwo);

    assertThat(orderOne.getNodes().size(), is(2));
    assertThat(orderTwo.getNodes().size(), is(2));

    Node dest = orderOne.getNodes().get(1);
    Node source = orderTwo.getNodes().get(0);

    assertEquals(dest.getNodeId(), source.getNodeId());
    assertEquals(dest.getSequenceId(), source.getSequenceId());
    assertEquals(dest.getNodePosition(), source.getNodePosition());
    assertEquals(dest.getActions().size(), source.getActions().size());
  }

  @Test
  public void destNodeAndSourceNodeOfConsecutiveOrdersMustNotMatchIfANewOrderHasStarted() {
    // setup a movement command
    Point startPoint = new Point("Point-0001");
    Point destPoint = new Point("Point-0002");
    Point sourcePoint = new Point("Point-0003");
    Point endPoint = new Point("Point-0004");

    MovementCommand commandOne = new DummyMovementCommand(startPoint, destPoint);
    Order orderOne = mapper.toOrder(commandOne);

    when(objectService.fetchObject(TransportOrder.class, transportOrder.getReference()))
        .thenReturn(transportOrder.withCurrentDriveOrderIndex(1));

    MovementCommand commandTwo = new DummyMovementCommand(sourcePoint, endPoint);
    Order orderTwo = mapper.toOrder(commandTwo);

    assertThat(orderOne.getNodes().size(), is(2));
    assertThat(orderTwo.getNodes().size(), is(2));

    Node dest = orderOne.getNodes().get(1);
    assertThat(dest.getNodeId(), is(destPoint.getName()));

    Node source = orderTwo.getNodes().get(0);
    assertThat(source.getNodeId(), is(sourcePoint.getName()));
  }

  @Test
  public void filterDestinationNodeActionsBasedOnEdgeTags() {
    Point source = new Point("Point-0001");
    Point dest = new Point("Point-0002")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "tag_beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02", "duck")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".02.tags", "tag_duck");
    Path path = new Path("path-0001", source.getReference(), dest.getReference())
        .withProperty(PROPKEY_EXECUTABLE_ACTIONS_TAGS, "tag_duck");

    Step step = new Step(
        path,
        source,
        dest,
        Orientation.FORWARD,
        0
    );
    MovementCommand command = new DummyMovementCommand(step);

    Order order = mapper.toOrder(command);

    assertThat(order.getNodes().size(), is(2));

    Node destNode = order.getNodes().get(1);
    assertThat(destNode.getActions().size(), is(1));
    assertThat(destNode.getActions().get(0).getActionType(), is("duck"));
  }

  @Test
  public void consecutiveOrdersMatchWithEdgeFilter() {
    Point source = new Point("Point-0001");
    Point middle = new Point("Point-0002")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01", "beep")
        .withProperty(PROPKEY_CUSTOM_ACTION_PREFIX + ".01.tags", "tag_beep");
    Point dest = new Point("Point-0003");

    Path path = new Path("path-0001", source.getReference(), dest.getReference())
        .withProperty(PROPKEY_EXECUTABLE_ACTIONS_TAGS, "tag_duck");
    Step step = new Step(path, source, dest, Orientation.FORWARD, 0);

    MovementCommand commandOne = new DummyMovementCommand(step);
    Order orderOne = mapper.toOrder(commandOne);

    MovementCommand commandTwo = new DummyMovementCommand(middle, source);
    Order orderTwo = mapper.toOrder(commandTwo);

    assertThat(orderOne.getNodes().size(), is(2));
    assertThat(orderTwo.getNodes().size(), is(2));

    Node destNode = orderOne.getNodes().get(1);
    Node sourceNode = orderTwo.getNodes().get(0);

    assertEquals(destNode.getNodeId(), sourceNode.getNodeId());
    assertEquals(destNode.getSequenceId(), sourceNode.getSequenceId());
    assertEquals(destNode.getNodePosition(), sourceNode.getNodePosition());
    assertEquals(destNode.getActions().size(), sourceNode.getActions().size());
  }

  @Test
  public void firstNodeShouldHaveVehicleBasedDeviationRange() {
    // The source point is 1.234m away from the vehicle.
    // The deviation range of that node should be atleast this distance.
    Point source = new Point("Point-0001")
        .withPosition(new Triple(
            vehicle.getPrecisePosition().getX() + 1234,
            vehicle.getPrecisePosition().getY(),
            0
        ));
    Point dest = new Point("Point-0002");

    MovementCommand commandOne = new DummyMovementCommand(source, dest);
    Order orderOne = mapper.toOrder(commandOne);

    assertThat(orderOne.getNodes().size(), is(2));

    Node sourceNode = orderOne.getNodes().get(0);
    assertThat(sourceNode.getNodePosition().getAllowedDeviationTheta(), is(greaterThan(1.234)));
  }

  @Test
  public void includeRouteAsHorizon() {
    Point p1 = new Point("Point-0001");
    Point p2 = new Point("Point-0002");
    Point p3 = new Point("Point-0003");

    Path l1 = new Path("path-0001", p1.getReference(), p2.getReference());
    Step s1 = new Step(l1, p1, p2, Orientation.FORWARD, 0);
    Path l2 = new Path("path-0002", p2.getReference(), p3.getReference());
    Step s2 = new Step(l2, p2, p3, Orientation.FORWARD, 1);

    DummyMovementCommand command = new DummyMovementCommand(new Route(Arrays.asList(s1, s2), 0), 0);
    command.finalOperation = MovementCommand.NO_OPERATION;
    command.finalDestinationLocation = new Location(
        "Location-0001",
        new LocationType("Loc-Type").getReference()
    );

    Order order = mapper.toOrder(command);
    assertThat(order.getNodes().size(), is(3));
    assertThat(order.getEdges().size(), is(2));
    assertThat(order.getNodes().get(0).isReleased(), is(true));
    assertThat(order.getNodes().get(1).isReleased(), is(true));
    assertThat(order.getNodes().get(2).isReleased(), is(false));
    assertThat(order.getEdges().get(0).getReleased(), is(true));
    assertThat(order.getEdges().get(1).getReleased(), is(false));
  }

  private class DummyMovementCommand
      implements MovementCommand {

    private final Route.Step dummyStep;

    private final Route dummyRoute;

    private String operation;

    private Map<String, String> properties = new HashMap<>();

    private Location location;

    private boolean isFinalMovment;

    private Location finalDestinationLocation;

    private String finalOperation;

    DummyMovementCommand(Step step) {
      dummyStep = step;
      dummyRoute = new Route(Arrays.asList(dummyStep), 0);
    }

    DummyMovementCommand(Route route, int currentIndex) {
      dummyRoute = route;
      dummyStep = route.getSteps().get(currentIndex);
    }

    DummyMovementCommand(Point source, Point dest) {
      this(source, dest, 1000, 500, 0, false);
    }

    DummyMovementCommand(Point source,
                         Point dest,
                         int maxSpeedForward,
                         int maxSpeedReverse) {
      this(source, dest, maxSpeedForward, maxSpeedReverse, 0, false);
    }

    DummyMovementCommand(Point source,
                         Point dest,
                         int maxSpeedForward,
                         int maxSpeedReverse,
                         int routeIndex,
                         boolean isFinalMovement) {
      Path path = null;
      if (source != null && dest != null) {
        path = new Path("Path-0001", source.getReference(), dest.getReference())
            .withMaxVelocity(maxSpeedForward)
            .withMaxReverseVelocity(maxSpeedReverse);
      }

      this.isFinalMovment = isFinalMovement;

      dummyStep = new Step(
          path,
          source,
          dest,
          Orientation.FORWARD,
          routeIndex
      );
      dummyRoute = new Route(Arrays.asList(dummyStep), 0);
    }

    @Override
    public Route getRoute() {
      return dummyRoute;
    }

    @Override
    public Route.Step getStep() {
      return dummyStep;
    }

    @Override
    public String getOperation() {
      return operation;
    }

    public void setOperation(String operation) {
      this.operation = operation;
    }

    @Override
    public boolean isWithoutOperation() {
      return operation == null;
    }

    @Override
    public Location getOpLocation() {
      return location;
    }

    public void setOpLocation(Location location) {
      this.location = location;
    }

    @Override
    public boolean isFinalMovement() {
      return isFinalMovment;
    }

    @Override
    public Point getFinalDestination() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Location getFinalDestinationLocation() {
      return finalDestinationLocation;
    }

    @Override
    public String getFinalOperation() {
      return finalOperation;
    }

    @Override
    public Map<String, String> getProperties() {
      return properties;
    }

    public void setProperties(Map<String, String> properties) {
      this.properties = properties;
    }
  }
}
