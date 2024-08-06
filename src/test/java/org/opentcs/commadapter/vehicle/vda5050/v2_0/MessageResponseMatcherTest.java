/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionStatus;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.BatteryState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.EStop;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ErrorEntry;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ErrorLevel;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.SafetyState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Unit tests for {@link MessageResponseMatcher}.
 */
public class MessageResponseMatcherTest {

  private MessageResponseMatcher messageResponseMatcher;

  private Consumer<Order> sendOrderCallback;
  private Consumer<InstantActions> sendInstantActionsCallback;
  private Consumer<OrderAssociation> orderAcceptedCallback;
  private Consumer<OrderAssociation> orderRejectedCallback;

  private MovementCommand dummyCommand;

  @BeforeEach
  @SuppressWarnings("unchecked")
  public void setUp() {
    sendOrderCallback = mock(Consumer.class);
    sendInstantActionsCallback = mock(Consumer.class);
    orderAcceptedCallback = mock(Consumer.class);
    orderRejectedCallback = mock(Consumer.class);
    messageResponseMatcher = new MessageResponseMatcher(
        "test",
        sendOrderCallback,
        sendInstantActionsCallback,
        orderAcceptedCallback,
        orderRejectedCallback
    );
    messageResponseMatcher.onStateMessage(newState());
    dummyCommand = new DummyMovementCommand();
  }

  @Test
  public void waitForAcknowledgementBeforeSendingNextOrder() {
    Order order1 = new Order("order1", 0L, List.of(), List.of());
    Order order2 = new Order("order2", 0L, List.of(), List.of());
    Order order3 = new Order("order3", 0L, List.of(), List.of());

    messageResponseMatcher.enqueueCommand(order1, dummyCommand);

    verify(sendOrderCallback, times(1)).accept(order1);

    messageResponseMatcher.enqueueCommand(order2, dummyCommand);
    messageResponseMatcher.enqueueCommand(order3, dummyCommand);

    verify(sendOrderCallback, times(1)).accept(order1);
    verify(sendOrderCallback, never()).accept(order2);
    verify(sendOrderCallback, never()).accept(order3);

    messageResponseMatcher.onStateMessage(stateAcceptingOrder(order1));

    verify(sendOrderCallback, times(1)).accept(order1);
    verify(sendOrderCallback, times(1)).accept(order2);
    verify(sendOrderCallback, never()).accept(order3);

    messageResponseMatcher.onStateMessage(stateAcceptingOrder(order2));

    verify(sendOrderCallback, times(1)).accept(order1);
    verify(sendOrderCallback, times(1)).accept(order2);
    verify(sendOrderCallback, times(1)).accept(order3);
  }

  @Test
  public void callOrderAcceptedCallbackWhenOrderWasAccepted() {
    Order order1 = new Order("order1", 0L, List.of(), List.of());

    messageResponseMatcher.enqueueCommand(order1, dummyCommand);
    messageResponseMatcher.onStateMessage(stateAcceptingOrder(order1));

    ArgumentCaptor<OrderAssociation> callbackCapture
        = ArgumentCaptor.forClass(OrderAssociation.class);
    verify(orderAcceptedCallback, times(1)).accept(callbackCapture.capture());

    OrderAssociation orderAssociation = callbackCapture.getValue();
    assertThat(orderAssociation.getOrder(), is(order1));
    assertThat(orderAssociation.getCommand(), is(dummyCommand));
  }

  @Test
  public void retrySendingOrderIfItIsNotAcknowleged() {
    Order orderNone = new Order("", 0L, List.of(), List.of());
    Order order1 = new Order("order1", 0L, List.of(), List.of());

    messageResponseMatcher.enqueueCommand(order1, dummyCommand);
    verify(sendOrderCallback, times(1)).accept(order1);

    messageResponseMatcher.onStateMessage(stateAcceptingOrder(orderNone));

    verify(sendOrderCallback, times(2)).accept(order1);
    verify(orderAcceptedCallback, never()).accept(any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"validationError", "noRouteError", "orderError", "orderUpdateError"})
  public void suppressOrderRepetitionOnOrderRejection(String errorType) {
    Order order = new Order("some-order", 0L, List.of(), List.of());
    State state = newState();
    state.setErrors(
        List.of(
            new ErrorEntry(
                errorType,
                ErrorLevel.WARNING
            )
        )
    );

    messageResponseMatcher.enqueueCommand(order, dummyCommand);
    messageResponseMatcher.onStateMessage(state);

    // The order should have been sent only once - not again as a reaction to the state message.
    verify(sendOrderCallback, times(1)).accept(any());
  }

  @Test
  public void shouldNotSendWhenInManualModeAndOrderRejection() {
    Order order = new Order("some-order", 0L, List.of(), List.of());
    InstantActions action = new InstantActions();
    State state = stateWithOperatingMode(OperatingMode.MANUAL);
    state.setErrors(
        List.of(
            new ErrorEntry(
                "validationError",
                ErrorLevel.WARNING
            )
        )
    );

    messageResponseMatcher.onStateMessage(state);
    messageResponseMatcher.enqueueAction(action);

    verify(sendInstantActionsCallback, never()).accept(action);
  }

  @Test
  public void waitForInstantActionAckknowlegmentBeforeSendingNextOrder() {
    InstantActions action1 = new InstantActions();
    action1.setHeaderId(1L);
    InstantActions action2 = new InstantActions();
    action2.setHeaderId(2L);
    InstantActions action3 = new InstantActions();
    action3.setHeaderId(3L);

    messageResponseMatcher.enqueueAction(action1);
    messageResponseMatcher.enqueueAction(action2);
    messageResponseMatcher.enqueueAction(action3);

    verify(sendInstantActionsCallback, times(1)).accept(action1);
    verify(sendInstantActionsCallback, never()).accept(action2);
    verify(sendInstantActionsCallback, never()).accept(action3);

    messageResponseMatcher.onStateMessage(stateAcceptingInstantAction(action1));

    verify(sendInstantActionsCallback, times(1)).accept(action1);
    verify(sendInstantActionsCallback, times(1)).accept(action2);
    verify(sendInstantActionsCallback, never()).accept(action3);

    messageResponseMatcher.onStateMessage(stateAcceptingInstantAction(action2));

    verify(sendInstantActionsCallback, times(1)).accept(action1);
    verify(sendInstantActionsCallback, times(1)).accept(action2);
    verify(sendInstantActionsCallback, times(1)).accept(action3);
  }

  @ParameterizedTest
  @EnumSource(value = OperatingMode.class, names = {"TEACHIN", "MANUAL", "SERVICE"})
  public void shouldNotSendWhenNotInAutomaticModes(OperatingMode mode) {
    messageResponseMatcher.onStateMessage(stateWithOperatingMode(mode));

    Order order1 = new Order("order1", 0L, List.of(), List.of());
    messageResponseMatcher.enqueueCommand(order1, dummyCommand);

    verify(sendOrderCallback, never()).accept(any());
  }

  @ParameterizedTest
  @EnumSource(value = OperatingMode.class, names = {"AUTOMATIC", "SEMIAUTOMATIC"})
  public void shouldSendWhenInAutomaticModes(OperatingMode mode) {
    messageResponseMatcher.onStateMessage(stateWithOperatingMode(mode));

    Order order1 = new Order("order1", 0L, List.of(), List.of());
    messageResponseMatcher.enqueueCommand(order1, dummyCommand);

    verify(sendOrderCallback, times(1)).accept(order1);
  }

  @Test
  public void sendOrderWhenOperatingModeChanges() {
    messageResponseMatcher.onStateMessage(stateWithOperatingMode(OperatingMode.MANUAL));

    Order order1 = new Order("order1", 0L, List.of(), List.of());
    messageResponseMatcher.enqueueCommand(order1, dummyCommand);

    verify(sendOrderCallback, never()).accept(any());

    messageResponseMatcher.onStateMessage(stateWithOperatingMode(OperatingMode.AUTOMATIC));
    verify(sendOrderCallback, times(1)).accept(order1);
  }

  private State stateWithOperatingMode(OperatingMode mode) {
    State state = newState();
    state.setOperatingMode(mode);
    return state;
  }

  private State stateAcceptingOrder(Order order) {
    State state = newState();
    state.setOrderId(order.getOrderId());
    state.setOrderUpdateId(order.getOrderUpdateId());
    return state;
  }

  private State stateAcceptingInstantAction(InstantActions actions) {
    State state = newState();
    state.getActionStates().addAll(
        actions.getActions().stream()
            .map(
                action -> new ActionState(
                    action.getActionId(),
                    ActionStatus.WAITING
                )
                    .setActionType(action.getActionType())
            )
            .collect(Collectors.toList())
    );
    return state;
  }

  private State newState() {
    return new State(
        "",
        0L,
        "",
        0L,
        new ArrayList<>(),
        new ArrayList<>(),
        false,
        new ArrayList<>(),
        new BatteryState(100.0, false),
        OperatingMode.AUTOMATIC,
        new ArrayList<>(),
        new SafetyState(EStop.NONE, false)
    );
  }

  private class DummyMovementCommand
      implements
        MovementCommand {

    @Override
    public DriveOrder getDriveOrder() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TransportOrder getTransportOrder() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Route.Step getStep() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    @Override
    public boolean isWithoutOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Location getOpLocation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isFinalMovement() {
      throw new UnsupportedOperationException("Not supported yet.");
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
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

}
