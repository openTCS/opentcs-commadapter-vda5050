/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.ActionStatus;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.ErrorEntry;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.ErrorLevel;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.Load;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.State;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;

/**
 * Utility methods for mapping vehicle state information.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StateMappings {

  /**
   * Prevents instantiation.
   */
  private StateMappings() {
  }

  /**
   * Returns a vehicle state derived from the given state message.
   *
   * @param state The state message.
   * @return A vehicle state derived from the given state message.
   */
  public static Vehicle.State toVehicleState(State state) {
    requireNonNull(state, "state");

    for (ErrorEntry error : state.getErrors()) {
      if (error.getErrorLevel() == ErrorLevel.FATAL) {
        return Vehicle.State.ERROR;
      }
    }

    if (state.getOperatingMode() != OperatingMode.AUTOMATIC) {
      return Vehicle.State.UNAVAILABLE;
    }

    if (state.getBatteryState().isCharging()) {
      return Vehicle.State.CHARGING;
    }

    if (state.isDriving()
        || hasPendingMovement(state)
        || hasPendingAction(state)) {
      return Vehicle.State.EXECUTING;
    }

    return Vehicle.State.IDLE;
  }

  /**
   * Returns a list of load handling devices derived from the given state message.
   *
   * @param state The state message.
   * @return A list of load handling devices derived from the given state message.
   */
  public static List<LoadHandlingDevice> toLoadHandlingDevices(State state) {
    requireNonNull(state, "state");

    List<LoadHandlingDevice> lhds = new ArrayList<>();
    if (state.getLoads() != null) {
      int index = 0;
      for (Load load : state.getLoads()) {
        String label = "LHD-" + index++;
        if (load.getLoadPosition() != null && !load.getLoadPosition().isBlank()) {
          label = load.getLoadPosition();
        }
        lhds.add(new LoadHandlingDevice(label, true));
      }
    }
    return lhds;
  }

  public static String toErrorPropertyValue(State state, ErrorLevel errorLevel) {
    requireNonNull(state, "state");
    requireNonNull(errorLevel, "errorLevel");

    return String.join(
        ", ",
        state.getErrors().stream()
            .filter(error -> error.getErrorLevel() == errorLevel)
            .map(error -> error.getErrorType())
            .distinct()
            .sorted()
            .collect(Collectors.toList())
    );
  }

  private static boolean hasPendingMovement(State state) {
    return !state.getNodeStates().isEmpty() || !state.getEdgeStates().isEmpty();
  }

  private static boolean hasPendingAction(State state) {
    return state.getActionStates().stream()
        .anyMatch(action -> (action.getActionStatus() != ActionStatus.FAILED
                             && action.getActionStatus() != ActionStatus.FINISHED));
  }
}
