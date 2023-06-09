/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Edge;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Node;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.ActionState;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.ActionStatus;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.EdgeState;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.NodeState;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.State;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Tracks the progress of movement commands and reports back finished ones.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class MovementCommandManager {

  /**
   * A list of currently tracked orders.
   */
  private final List<OrderAssociation> trackedOrders = new ArrayList<>();

  /**
   * Construct a new MovementCommandManager.
   */
  public MovementCommandManager() {
  }

  /**
   * Adds a movement command and an order to be tracked.
   *
   * @param orderAssociation The order association to track.
   */
  public void enqueue(@Nonnull OrderAssociation orderAssociation) {
    requireNonNull(orderAssociation, "orderAssociation");

    trackedOrders.add(orderAssociation);
  }

  /**
   * Clears tracked order associations.
   */
  public void clear() {
    trackedOrders.clear();
  }

  /**
   * Notifies this instance about a new state reported by the vehicle.
   * Calls the callback function for any movement commands considered to be completed.
   *
   * @param currentState The current state message.
   * @param callback The callback for completed movement commands.
   */
  public void onStateMessage(@Nonnull State currentState,
                             @Nonnull Consumer<MovementCommand> callback) {
    requireNonNull(currentState, "currentState");
    requireNonNull(callback, "callback");

    if (!reportsOrderIdForCurrentDriveOrder(currentState)) {
      return;
    }

    Iterator<OrderAssociation> iter = trackedOrders.iterator();
    while (iter.hasNext() && checkForCompletionAndReport(iter.next(), currentState, callback)) {
      iter.remove();
    }
  }

  private boolean checkForCompletionAndReport(OrderAssociation association,
                                              State state,
                                              Consumer<MovementCommand> callback) {
    if (orderComplete(association, state)) {
      callback.accept(association.getCommand());
      return true;
    }
    else {
      return false;
    }
  }

  private boolean orderComplete(OrderAssociation assocation, State state) {
    return movementComplete(assocation, state)
        && (!assocation.getCommand().isFinalMovement()
            || actionsComplete(assocation.getOrder(), state));
  }

  private boolean movementComplete(OrderAssociation association, State state) {
    return edgesComplete(association.getOrder(), state)
        && nodesComplete(association.getOrder(), state);
  }

  private boolean edgesComplete(Order order, State state) {
    return order.getEdges().stream().allMatch(edge -> edgeComplete(edge, state.getEdgeStates()));
  }

  private boolean nodesComplete(Order order, State state) {
    return order.getNodes().stream().allMatch(node -> nodeComplete(node, state.getNodeStates()));
  }

  private boolean edgeComplete(Edge edge, List<EdgeState> edgeStates) {
    // An edge is complete if it is not in the list of edge states (any more).
    return edgeStates.stream().noneMatch(edgeState -> {
      return Objects.equals(edgeState.getEdgeId(), edge.getEdgeId())
          && Objects.equals(edgeState.getSequenceId(), edge.getSequenceId());
    });
  }

  private boolean nodeComplete(Node node, List<NodeState> nodeStates) {
    // A node is complete if it is not in the list of node states (any more).
    return nodeStates.stream().noneMatch(nodeState -> {
      return Objects.equals(nodeState.getNodeId(), node.getNodeId())
          && Objects.equals(nodeState.getSequenceId(), node.getSequenceId());
    });
  }

  private boolean actionsComplete(Order order, State state) {
    return Stream.concat(order.getNodes().stream().flatMap(node -> node.getActions().stream()),
                         order.getEdges().stream().flatMap(edge -> edge.getActions().stream()))
        .allMatch(action -> actionComplete(action, state.getActionStates()));
  }

  private boolean actionComplete(Action action, List<ActionState> actionStates) {
    return actionStates.stream()
        .anyMatch(actionState -> {
          return Objects.equals(actionState.getActionId(), action.getActionId())
              && isFinalActionStatus(actionState.getActionStatus());
        });
  }

  private boolean isFinalActionStatus(ActionStatus actionStatus) {
    return actionStatus == ActionStatus.FAILED
        || actionStatus == ActionStatus.FINISHED;
  }

  private boolean reportsOrderIdForCurrentDriveOrder(@Nonnull State state) {
    return Objects.equals(
        state.getOrderId(),
        trackedOrders.stream()
            .findAny()
            .map(orderAssociation -> orderAssociation.getOrder().getOrderId())
            .orElse("")
    );
  }
}
