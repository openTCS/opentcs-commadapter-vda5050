/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import com.google.inject.assistedinject.Assisted;
import static java.lang.Math.toDegrees;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getPropertyDouble;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_THETA;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_XY;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.AgvPosition;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Resolves the vehicle position from a VDA5050 state message to an openTCS point.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class VehiclePositionResolver {

  /**
   * The object service.
   */
  private final TCSObjectService objectService;
  /**
   * The vehicle to manage.
   */
  private final TCSObjectReference<Vehicle> vehicleReference;

  /**
   * Creates a new instance.
   *
   * @param vehicleReference Reference to the vehicle being managed.
   * @param objectService Object service.
   */
  @Inject
  public VehiclePositionResolver(@Nonnull @Assisted TCSObjectReference<Vehicle> vehicleReference,
                                 @Nonnull TCSObjectService objectService) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.vehicleReference = requireNonNull(vehicleReference, "vehicleReference");
  }

  /**
   * Find the correct vehicle position given a state message.
   *
   * @param currentPosition The name of the current vehicle position.
   * @param currentState The current vehicle state.
   * @return The vehicle position or null if no position can be found.
   */
  @Nullable
  public String resolveVehiclePosition(@Nullable String currentPosition,
                                       @Nonnull State currentState) {
    requireNonNull(currentState, "currentState");

    // Use lastNodeId for the position if it is set.
    if (currentState.getLastNodeId() != null && !currentState.getLastNodeId().isBlank()) {
      return currentState.getLastNodeId();
    }

    // Try to derive the point name from the AGV position.
    Vehicle vehicle = objectService.fetchObject(Vehicle.class, vehicleReference);
    String logicalPosition = findVehicleLogicalPosition(
        currentPosition,
        currentState.getAgvPosition(),
        vehicle
    );
    if (logicalPosition != null) {
      return logicalPosition;
    }

    // Use the last known position.
    return currentPosition;
  }

  private String findVehicleLogicalPosition(@Nullable String currentPosition,
                                            @Nullable AgvPosition position,
                                            @Nonnull Vehicle vehicle) {
    if (position == null) {
      return null;
    }

    if (isCurrentLogicalPositionCorrect(currentPosition, position, vehicle)) {
      return currentPosition;
    }

    for (Point p : objectService.fetchObjects(Point.class)) {
      if (isWithinDeviationXY(p, position, vehicle)
          && isWithinDeviationTheta(p, position, vehicle)) {
        return p.getName();
      }
    }
    return null;
  }

  private boolean isCurrentLogicalPositionCorrect(@Nullable String currentPosition,
                                                  @Nonnull AgvPosition position,
                                                  @Nonnull Vehicle vehicle) {
    if (currentPosition == null) {
      return false;
    }

    Point point = objectService.fetchObject(Point.class, currentPosition);
    if (point == null) {
      return false;
    }

    return isWithinDeviationXY(point, position, vehicle)
        && isWithinDeviationTheta(point, position, vehicle);
  }

  private boolean isWithinDeviationXY(Point p, AgvPosition position, Vehicle vehicle) {
    double deviationX = Math.abs(p.getPosition().getX() / 1000.0 - position.getX());
    double deviationY = Math.abs(p.getPosition().getY() / 1000.0 - position.getY());

    return Math.sqrt((deviationX * deviationX) + (deviationY * deviationY))
        <= getPropertyDouble(PROPKEY_VEHICLE_DEVIATION_XY, p, vehicle).orElse(0.0);
  }

  private boolean isWithinDeviationTheta(Point p, AgvPosition position, Vehicle vehicle) {
    if (Double.isNaN(p.getVehicleOrientationAngle())) {
      return true;
    }
    return Math.abs(angleDifference(p.getVehicleOrientationAngle(), toDegrees(position.getTheta())))
        <= getPropertyDouble(PROPKEY_VEHICLE_DEVIATION_THETA, p, vehicle).orElse(0.0);
  }

  private double angleDifference(double d1, double d2) {
    double t = Math.abs(d1 - d2) % 360;
    if (t > 180) {
      return t - 360;
    }
    return t;
  }

}
