/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ActionStatus;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.BatteryState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.EStop;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.EdgeState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ErrorEntry;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ErrorLevel;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.Load;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.NodeState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.SafetyState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;

/**
 * Tests for state mapping utility methods.
 */
public class StateMappingsTest {

  private State state;

  @BeforeEach
  public void setUp() {
    state = createState();
  }

  @Test
  public void respectVehicleStatePrecedence() {
    assertThat(StateMappings.toVehicleState(state), is(Vehicle.State.IDLE));

    state.setDriving(true);
    assertThat(StateMappings.toVehicleState(state), is(Vehicle.State.EXECUTING));

    state.setDriving(false);
    state.setNodeStates(List.of(new NodeState("node-0", 0L, true)));
    state.setEdgeStates(List.of(new EdgeState("edge-0", 1L, true)));
    assertThat(StateMappings.toVehicleState(state), is(Vehicle.State.EXECUTING));

    state.setNodeStates(List.of());
    state.setEdgeStates(List.of());
    state.setActionStates(List.of(
        new ActionState("some-id", "some-type", ActionStatus.RUNNING)
    ));
    assertThat(StateMappings.toVehicleState(state), is(Vehicle.State.EXECUTING));

    state.setBatteryState(new BatteryState(100.0, true));
    assertThat(StateMappings.toVehicleState(state), is(Vehicle.State.CHARGING));

    state.setOperatingMode(OperatingMode.SEMIAUTOMATIC);
    assertThat(StateMappings.toVehicleState(state), is(Vehicle.State.CHARGING));

    state.setOperatingMode(OperatingMode.MANUAL);
    assertThat(StateMappings.toVehicleState(state), is(Vehicle.State.UNAVAILABLE));

    state.setErrors(List.of(
        new ErrorEntry(
            "some-error",
            ErrorLevel.WARNING
        )
    ));
    assertThat(StateMappings.toVehicleState(state), is(Vehicle.State.UNAVAILABLE));

    state.setErrors(List.of(
        new ErrorEntry(
            "some-error",
            ErrorLevel.FATAL
        )
    ));
    assertThat(StateMappings.toVehicleState(state), is(Vehicle.State.ERROR));
  }

  @Test
  public void returnAsManyLhdsAsReportedLoads() {
    state.setLoads(null);
    assertThat(StateMappings.toLoadHandlingDevices(state), is(empty()));

    state.setLoads(List.of());
    assertThat(StateMappings.toLoadHandlingDevices(state), is(empty()));

    state.setLoads(List.of(new Load(), new Load()));
    assertThat(StateMappings.toLoadHandlingDevices(state), hasSize(2));
  }

  @Test
  public void setLhdNamesAccordingToReportedLoads() {
    Load load0 = new Load();
    Load load1 = new Load();
    Load load2 = new Load();
    Load load3 = new Load();

    load1.setLoadPosition("load-position-1");
    load2.setLoadPosition("load-position-2");
    load3.setLoadPosition("load-position-3");

    state.setLoads(List.of(load0, load1, load2, load3));

    List<LoadHandlingDevice> result = StateMappings.toLoadHandlingDevices(state);
    assertThat(result, hasSize(4));
    assertThat(result.get(0).getLabel(), matchesPattern("^LHD-\\p{Digit}$"));
    assertThat(result.get(1).getLabel(), is("load-position-1"));
    assertThat(result.get(2).getLabel(), is("load-position-2"));
    assertThat(result.get(3).getLabel(), is("load-position-3"));
  }

  @Test
  public void returnUnloadedLengthForNullLoads() {
    state.setLoads(null);

    assertThat(StateMappings.toVehicleLength(state, 1000, 2000), is(1000));
  }

  @Test
  public void returnUnloadedLengthForEmptyLoads() {
    state.setLoads(List.of());

    assertThat(StateMappings.toVehicleLength(state, 1000, 2000), is(1000));
  }

  @Test
  public void returnLoadedLengthForLoads() {
    state.setLoads(List.of(new Load()));

    assertThat(StateMappings.toVehicleLength(state, 1000, 2000), is(2000));
  }

  @Test
  public void mapEmptyErrorsListToEmptyString() {
    state.setErrors(List.of());
    assertThat(StateMappings.toErrorPropertyValue(state, ErrorLevel.FATAL), is(emptyString()));

    state.setErrors(List.of(
        new ErrorEntry(
            "tire blown",
            ErrorLevel.WARNING
        )
    ));
    assertThat(StateMappings.toErrorPropertyValue(state, ErrorLevel.FATAL), is(emptyString()));
  }

  @Test
  public void mapOnlySelectedErrorLevels() {
    state.setErrors(List.of(
        new ErrorEntry("tire blown",
                       ErrorLevel.FATAL),
        new ErrorEntry("motor on fire",
                       ErrorLevel.WARNING),
        new ErrorEntry("brakes nonfunctional",
                       ErrorLevel.WARNING),
        new ErrorEntry("reactor overheated",
                       ErrorLevel.FATAL)
    ));

    String result = StateMappings.toErrorPropertyValue(state, ErrorLevel.FATAL);
    assertThat(result, is("reactor overheated, tire blown"));

    result = StateMappings.toErrorPropertyValue(state, ErrorLevel.WARNING);
    assertThat(result, is("brakes nonfunctional, motor on fire"));
  }

  @Test
  public void mapErrorTypesInLexicographicOrder() {
    state.setErrors(List.of(
        new ErrorEntry("tire blown",
                       ErrorLevel.FATAL),
        new ErrorEntry("motor on fire",
                       ErrorLevel.FATAL),
        new ErrorEntry("brakes nonfunctional",
                       ErrorLevel.FATAL),
        new ErrorEntry("reactor overheated",
                       ErrorLevel.FATAL)
    ));

    String result = StateMappings.toErrorPropertyValue(state, ErrorLevel.FATAL);
    assertThat(result, is("brakes nonfunctional, motor on fire, reactor overheated, tire blown"));
  }

  private State createState() {
    return new State("",
                     0L,
                     "",
                     0L,
                     List.of(),
                     List.of(),
                     false,
                     false,
                     List.of(),
                     new BatteryState(100.0, false),
                     OperatingMode.AUTOMATIC,
                     List.of(),
                     new SafetyState(EStop.NONE, false));
  }
}
