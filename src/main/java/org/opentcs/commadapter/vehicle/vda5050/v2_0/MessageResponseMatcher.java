// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ErrorEntry;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matches a state messages with sent order messages to confirm their delivery.
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
  private final Queue<Object> requests = new ArrayDeque<>();
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
   * Flag indicating whether this comm adapter may currently send requests to the vehicle.
   * If false, all enqueued requests will stay in the queue until the flag becomes true.
   */
  private boolean sendingAllowed;

  /**
   * Creates a new OrderResponseMatcher.
   *
   * @param commAdapterName The name of the comm adapter
   * @param sendOrderCallback The callback for sending the next order.
   * @param sendInstantActionsCallback The callback for sending instant actions.
   * @param orderAcceptedCallback The callback for when the order is accepted by the vehicle.
   * @param orderRejectedCallback The callback for when the vehicle rejects an order.
   */
  public MessageResponseMatcher(
      @Nonnull
      String commAdapterName,
      @Nonnull
      Consumer<Order> sendOrderCallback,
      @Nonnull
      Consumer<InstantActions> sendInstantActionsCallback,
      @Nonnull
      Consumer<OrderAssociation> orderAcceptedCallback,
      @Nonnull
      Consumer<OrderAssociation> orderRejectedCallback
  ) {
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
      LOG.debug(
          "{}: Not sending enqueued request yet, due to unacknowledged previous request.",
          commAdapterName
      );
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

  public void onStateMessage(
      @Nonnull
      State state
  ) {
    requireNonNull(state, "state");

    sendingAllowed = state.getOperatingMode() == OperatingMode.AUTOMATIC
        || state.getOperatingMode() == OperatingMode.SEMIAUTOMATIC;

    Object currentRequest = requests.peek();
    if (currentRequest == null) {
      return;
    }

    if (vehicleRejectedOrder(state)) {
      if (currentRequest instanceof OrderAssociation) {
        orderRejectedCallback.accept((OrderAssociation) currentRequest);
      }

      LOG.warn(
          "{}: Vehicle indicates order rejection. Last request sent to it was: {}",
          commAdapterName,
          currentRequest
      );
    }
    else if (requestAccepted(currentRequest, state)) {
      requests.poll();
      if (currentRequest instanceof OrderAssociation) {
        OrderAssociation order = (OrderAssociation) currentRequest;
        LOG.debug("{}: Vehicle acknowledged order: {}", commAdapterName, order);
        orderAcceptedCallback.accept(order);
      }
      else if (currentRequest instanceof InstantActions) {
        InstantActions actions = (InstantActions) currentRequest;
        LOG.debug("{}: Vehicle acknowledged instant actions: {}", commAdapterName, actions);
      }
      sendNextOrder();
    }
    else {
      sendNextOrder();
    }
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
      LOG.warn(
          "{}: Unrecognized request of type {}.",
          commAdapterName,
          request.getClass().getName()
      );
      return false;
    }
  }

  /**
   * Send the first request in the queue to the vehicle.
   */
  private void sendNextOrder() {
    if (!sendingAllowed) {
      LOG.debug("{}: Cannot send next order. Sending is currently disallowed", commAdapterName);
      return;
    }

    if (requests.isEmpty()) {
      LOG.debug("{}: Cannot send next order. No request to send", commAdapterName);
      return;
    }

    Object request = requests.peek();
    LOG.debug("{}: Sending order to comm adapter: {}", commAdapterName, request);
    if (request instanceof OrderAssociation) {
      sendOrderCallback.accept(((OrderAssociation) request).getOrder());
    }
    else if (request instanceof InstantActions) {
      sendInstantActionsCallback.accept((InstantActions) request);
    }
    else {
      LOG.warn(
          "{}: Cannot send request. Unrecognized request of type {}.",
          commAdapterName,
          request.getClass().getName()
      );
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
