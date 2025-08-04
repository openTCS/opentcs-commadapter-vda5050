// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_EXTENSION_TRIGGER;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Determines whether the deviation (of the very first node in an order) should be extended to
 * include the vehicle's position.
 */
public class DeviationExtensionTrigger {

  /**
   * The trigger that determines whether the deviation should be extended.
   */
  private final Trigger trigger;

  /**
   * Creates a new instance.
   *
   * @param vehicle The vehicle to create the trigger for.
   */
  @Inject
  public DeviationExtensionTrigger(
      @Assisted
      Vehicle vehicle
  ) {
    this.trigger = Trigger.fromVehicle(requireNonNull(vehicle, "vehicle"));
  }

  /**
   * Determines whether the deviation for the given movement command should be extended to include
   * the vehicle's position.
   *
   * @param command The movement command to check.
   * @return {@code true} if it's the movement command for the very first node of an order and the
   * deviation should be extended, {@code false} otherwise.
   */
  public boolean extendDeviation(MovementCommand command) {
    if (!isFirstMovementCommandInRoute(command)) {
      return false;
    }

    return switch (trigger) {
      case ALWAYS -> true;
      case NEVER -> false;
    };
  }

  private boolean isFirstMovementCommandInRoute(MovementCommand command) {
    return command.getStep().getRouteIndex() == 0;
  }

  /**
   * The trigger that determines whether the deviation should be extended.
   */
  private enum Trigger {

    /**
     * The deviation should always be extended.
     */
    ALWAYS,
    /**
     * The deviation should never be extended.
     */
    NEVER;

    public static Trigger fromVehicle(Vehicle vehicle) {
      return switch (vehicle.getProperty(PROPKEY_VEHICLE_DEVIATION_EXTENSION_TRIGGER)) {
        case "never" -> NEVER;
        case null, default -> ALWAYS;
      };
    }
  }
}
