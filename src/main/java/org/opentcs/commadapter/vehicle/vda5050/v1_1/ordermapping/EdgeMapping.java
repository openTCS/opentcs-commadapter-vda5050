/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping;

import static java.lang.Math.toRadians;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getProperty;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getPropertyDouble;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_PATH_ORIENTATION_FORWARD;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_PATH_ORIENTATION_REVERSE;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_PATH_ROTATION_ALLOWED_FORWARD;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_PATH_ROTATION_ALLOWED_REVERSE;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Edge;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;

/**
 * Functions to map a {@link Route.Step} from openTCS movement commands to a VDA5050 edge.
 */
public class EdgeMapping {

  /**
   * Prevents unwanted instantiation.
   */
  private EdgeMapping() {
  }

  /**
   * Maps a route step to an edge.
   *
   * @param step The step to map.
   * @param vehicle the vehicle to map the for.
   * @param actions The actions for this edge.
   * @return The mapped edge
   */
  public static Edge toEdge(Route.Step step,
                            Vehicle vehicle,
                            List<Action> actions) {
    requireNonNull(step, "step");
    requireNonNull(vehicle, "vehicle");
    requireNonNull(actions, "actions");

    Edge edge = new Edge(
        step.getPath().getName(),
        step.getRouteIndex() * 2L + 1,
        true,
        step.getSourcePoint().getName(),
        step.getDestinationPoint().getName(),
        actions
    );

    edge.setMaxSpeed(maxSpeed(step));
    edge.setOrientation(edgeOrientation(step));
    edge.setRotationAllowed(rotationAllowed(step));

    return edge;
  }

  @Nonnull
  private static Double maxSpeed(Route.Step step) {
    if (step.getVehicleOrientation() == Vehicle.Orientation.BACKWARD) {
      return step.getPath().getMaxReverseVelocity() / 1000.0;
    }
    else {
      return step.getPath().getMaxVelocity() / 1000.0;
    }
  }

  @Nullable
  private static Double edgeOrientation(Route.Step step) {
    if (step.getVehicleOrientation() == Vehicle.Orientation.BACKWARD) {
      return getPropertyDouble(PROPKEY_PATH_ORIENTATION_REVERSE, step.getPath())
          .map(value -> toRadians(value))
          .orElse(null);
    }
    else {
      return getPropertyDouble(PROPKEY_PATH_ORIENTATION_FORWARD, step.getPath())
          .map(value -> toRadians(value))
          .orElse(null);
    }
  }

  @Nullable
  private static Boolean rotationAllowed(Route.Step step) {
    if (step.getVehicleOrientation() == Vehicle.Orientation.BACKWARD) {
      return getProperty(PROPKEY_PATH_ROTATION_ALLOWED_REVERSE, step.getPath())
          .map(value -> Boolean.valueOf(value))
          .orElse(null);
    }
    else {
      return getProperty(PROPKEY_PATH_ROTATION_ALLOWED_FORWARD, step.getPath())
          .map(value -> Boolean.valueOf(value))
          .orElse(null);
    }
  }
}
