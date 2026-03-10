// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.CancelOrder;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionStatus;
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
   * Queue for requests that need to be sent to the vehicle.
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
   * The maximum number of consecutive state messages that indicate a rejection of the current
   * order/message before we consider the rejection to be permanent and stop retrying.
   */
  private final int maxIgnoredRejectionsCount;
  /**
   * The number of consecutive state messages that indicate a rejection of the current order/message
   * we have received so far.
   */
  private int consecutiveRejectionsCount;
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
   * @param maxIgnoredRejectionsCount The maximum number of consecutive state messages that
   * indicate a rejection of the current order/message before we consider the rejection to be
   * permanent and stop retrying.
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
      int maxIgnoredRejectionsCount
  ) {
    this.commAdapterName = requireNonNull(commAdapterName, "commAdapterName");
    this.sendOrderCallback = requireNonNull(sendOrderCallback, "sendOrderCallback");
    this.sendInstantActionsCallback
        = requireNonNull(sendInstantActionsCallback, "sendInstantActionsCallback");
    this.orderAcceptedCallback = requireNonNull(orderAcceptedCallback, "orderAcceptedCallback");
    this.maxIgnoredRejectionsCount = maxIgnoredRejectionsCount;
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
    consecutiveRejectionsCount = 0;
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

    if (StateMappings.vehicleRejectsOrder(state)) {
      consecutiveRejectionsCount++;
    }
    else {
      consecutiveRejectionsCount = 0;
    }
    if (consecutiveRejectionsCount > maxIgnoredRejectionsCount) {
      // Don't do anything - the vehicle cannot continue processing the drive order. We will wait
      // for this to be resolved via order withdrawal and a new initial order message.
      return;
    }

    if (requestAcknowledged(currentRequest, state)) {
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

  private boolean requestAcknowledged(Object request, State state) {
    if (request instanceof OrderAssociation) {
      return orderAccepted(((OrderAssociation) request).getOrder(), state);
    }
    else if (request instanceof InstantActions) {
      return instantActionsAcknowledged((InstantActions) request, state);
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

  private boolean instantActionsAcknowledged(InstantActions instantAction, State state) {
    return instantAction.getActions().stream()
        .allMatch(action -> {
          // In case of a cancelOrder action, we actually wait for the vehicle to accept AND
          // COMPLETE the action. Not doing this can lead to situations in which we send another
          // order while the vehicle is still processing the cancelOrder, and the vehicle then
          // immediately cancelling that new order.
          if (Objects.equals(action.getActionType(), CancelOrder.ACTION_TYPE)) {
            return cancelOrderAcceptedAndCompleted(action, state);
          }
          else {
            return actionAccepted(action, state);
          }
        });
  }

  private boolean actionAccepted(Action action, State state) {
    return state.getActionStates().stream()
        .anyMatch(actionState -> actionState.getActionId().equals(action.getActionId()));
  }

  private boolean cancelOrderAcceptedAndCompleted(Action action, State state) {
    return state.getActionStates().stream()
        .filter(actionState -> actionState.getActionId().equals(action.getActionId()))
        .anyMatch(
            actionState -> actionState.getActionStatus() == ActionStatus.FINISHED
                || actionState.getActionStatus() == ActionStatus.FAILED
        );
  }
}
