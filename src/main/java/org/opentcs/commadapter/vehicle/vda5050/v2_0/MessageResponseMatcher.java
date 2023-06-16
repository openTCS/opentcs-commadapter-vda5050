/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import java.util.LinkedList;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ErrorEntry;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matches a state messages with sent order messages to confirm their delivery.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class MessageResponseMatcher {

  private static final Logger LOG = LoggerFactory.getLogger(MessageResponseMatcher.class);
  /**
   * The comm adapter.
   */
  private final String commAdapterName;
  /**
   * Queue for requests that need to be send to the vehicle.
   */
  private final Queue<Object> requests = new LinkedList<>();
  /**
   * The callback for sending the next order.
   */
  private final Consumer<Order> sendOrderCallback;
  /**
   * The callback for sending the next instant actions.
   */
  private final Consumer<InstantActions> sendInstantActionsCallback;
  /**
   * The callback for when an order is accepted by the vehicle.
   */
  private final Consumer<OrderAssociation> orderAcceptedCallback;
  /**
   * The callback for when an order was rejected by the vehicle.
   */
  private final Consumer<OrderAssociation> orderRejectedCallback;

  /**
   * Creates a new OrderResponseMatcher.
   *
   * @param commAdapterName The name of the comm adapter
   * @param sendOrderCallback The callback for sending the next order.
   * @param sendInstantActionsCallback The callback for sending instant actions.
   * @param orderAcceptedCallback The callback for when the order is accepted by the vehicle.
   * @param orderRejectedCallback The callback for when the vehicle rejects an order.
   */
  public MessageResponseMatcher(@Nonnull String commAdapterName,
                                @Nonnull Consumer<Order> sendOrderCallback,
                                @Nonnull Consumer<InstantActions> sendInstantActionsCallback,
                                @Nonnull Consumer<OrderAssociation> orderAcceptedCallback,
                                @Nonnull Consumer<OrderAssociation> orderRejectedCallback) {
    this.commAdapterName = requireNonNull(commAdapterName, "commAdapterName");
    this.sendOrderCallback = requireNonNull(sendOrderCallback, "sendOrderCallback");
    this.sendInstantActionsCallback
        = requireNonNull(sendInstantActionsCallback, "sendInstantActionsCallback");
    this.orderAcceptedCallback = requireNonNull(orderAcceptedCallback, "orderAcceptedCallback");
    this.orderRejectedCallback = requireNonNull(orderRejectedCallback, "orderRejectedCallback");
  }

  public void enqueueCommand(Order order, MovementCommand command) {
    LOG.debug("{}: Enqueuing order: {}", commAdapterName, order);
    enqueueRequest(new OrderAssociation(order, command));
  }

  public void enqueueAction(InstantActions action) {
    LOG.debug("{}: Enqueuing instant action: {}", commAdapterName, action);
    enqueueRequest(action);
  }

  private void enqueueRequest(Object request) {
    requests.add(request);
    if (requests.size() > 1) {
      LOG.debug("{}: Not sending enqueued request yet, due to unacknowledged previous request.",
                commAdapterName);
      return;
    }

    sendNextOrder();
  }

  /**
   * Clears all orders for which the {@link MessageResponseMatcher} is waiting for acknowledgement
   * from the vehicle.
   */
  public void clear() {
    requests.clear();
  }

  public void onStateMessage(@Nonnull State state) {
    requireNonNull(state, "state");

    if (requests.isEmpty()) {
      return;
    }
    if (vehicleRejectedOrder(state)) {
      Object request = requests.peek();
      if (request instanceof OrderAssociation) {
        orderRejectedCallback.accept((OrderAssociation) request);
      }

      LOG.warn("{}: Vehicle indicates order rejection. Last request sent to it was: {}",
               commAdapterName,
               request);
      return;
    }

    Object request = requests.peek();
    if (requestAccepted(request, state)) {
      requests.poll();
      if (request instanceof OrderAssociation) {
        OrderAssociation order = (OrderAssociation) request;
        LOG.debug("{}: Vehicle acknowledged order: {}", commAdapterName, order);
        orderAcceptedCallback.accept(order);
      }
      else if (request instanceof InstantActions) {
        InstantActions actions = (InstantActions) request;
        LOG.debug("{}: Vehicle acknowledged instant actions: {}", commAdapterName, actions);
      }
    }

    sendNextOrder();
  }

  private boolean vehicleRejectedOrder(State state) {
    return state.getErrors().stream().anyMatch(this::isOrderRejectionWarning);
  }

  private boolean isOrderRejectionWarning(ErrorEntry error) {
    switch (error.getErrorType()) {
      case "validationError":
      case "noRouteError":
      case "orderError":
      case "orderUpdateError":
        return true;
      default:
        return false;
    }
  }

  private boolean requestAccepted(Object request, State state) {
    if (request instanceof OrderAssociation) {
      return orderAccepted(((OrderAssociation) request).getOrder(), state);
    }
    else if (request instanceof InstantActions) {
      return instantActionsAccepted((InstantActions) request, state);
    }
    else {
      LOG.warn("{}: Unrecognized request of type {}.",
               commAdapterName,
               request.getClass().getName());
      return false;
    }
  }

  private void sendNextOrder() {
    if (requests.isEmpty()) {
      return;
    }

    LOG.debug("{}: Sending order to comm adapter: {}", commAdapterName, requests.peek());
    Object request = requests.peek();
    if (request instanceof OrderAssociation) {
      sendOrderCallback.accept(((OrderAssociation) request).getOrder());
    }
    else if (request instanceof InstantActions) {
      sendInstantActionsCallback.accept((InstantActions) request);
    }
    else {
      LOG.warn("{}: Cannot send request. Unrecognized request of type {}.",
               commAdapterName,
               request.getClass().getName());
    }
  }

  private boolean orderAccepted(Order order, State state) {
    return Objects.equals(state.getOrderId(), order.getOrderId())
        && Objects.equals(state.getOrderUpdateId(), order.getOrderUpdateId());
  }

  private boolean instantActionsAccepted(InstantActions instantAction, State state) {
    return instantAction.getActions().stream()
        .allMatch(action -> actionAccepted(action, state));
  }

  private boolean actionAccepted(Action action, State state) {
    return state.getActionStates().stream()
        .anyMatch(actionState -> actionState.getActionId().equals(action.getActionId()));
  }
}
