/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static java.lang.Math.toDegrees;
import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getProperty;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getPropertyDouble;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_THETA;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_XY;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MAP_ID;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.commadapter.vehicle.vda5050.common.AngleMath;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.AgvPosition;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Resolves the vehicle position from a VDA5050 state message to an openTCS point.
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
  public VehiclePositionResolver(
      @Nonnull
      @Assisted
      TCSObjectReference<Vehicle> vehicleReference,
      @Nonnull
      TCSObjectService objectService
  ) {
    this.objectService = requireNonNull(objectService, "objectService");
    this.vehicleReference = requireNonNull(vehicleReference, "vehicleReference");
  }

  /**
   * Find the correct vehicle position given a state message.
   *
   * @param lastKnownPosition The name of the vehicles last known position. {@code Null} if the
   * last position is not known.
   * @param currentState The current vehicle state.
   * @return The vehicle position or null if no position can be found.
   */
  @Nullable
  public String resolveVehiclePosition(
      @Nullable
      String lastKnownPosition,
      @Nonnull
      State currentState
  ) {
    requireNonNull(currentState, "currentState");

    // Use lastNodeId for the position if it is set.
    if (currentState.getLastNodeId() != null && !currentState.getLastNodeId().isBlank()) {
      return currentState.getLastNodeId();
    }

    // Try to derive the point name from the AGV position.
    Vehicle vehicle = objectService.fetchObject(Vehicle.class, vehicleReference);
    String logicalPosition = findVehicleLogicalPosition(
        lastKnownPosition,
        currentState.getAgvPosition(),
        vehicle
    );
    if (logicalPosition != null) {
      return logicalPosition;
    }

    return lastKnownPosition;
  }

  private String findVehicleLogicalPosition(
      @Nullable
      String lastKnownPosition,
      @Nullable
      AgvPosition position,
      @Nonnull
      Vehicle vehicle
  ) {
    if (position == null) {
      return null;
    }

    if (isCurrentLogicalPositionCorrect(lastKnownPosition, position, vehicle)) {
      return lastKnownPosition;
    }

    for (Point p : objectService.fetchObjects(Point.class)) {
      if (isPointAtAGVPosition(p, position, vehicle)) {
        return p.getName();
      }
    }
    return null;
  }

  private boolean isCurrentLogicalPositionCorrect(
      @Nullable
      String lastKnownPosition,
      @Nonnull
      AgvPosition position,
      @Nonnull
      Vehicle vehicle
  ) {
    if (lastKnownPosition == null) {
      return false;
    }

    Point point = objectService.fetchObject(Point.class, lastKnownPosition);
    if (point == null) {
      return false;
    }

    return isPointAtAGVPosition(point, position, vehicle);
  }

  private boolean isPointAtAGVPosition(Point p, AgvPosition position, Vehicle vehicle) {
    return isWithinDeviationXY(p, position, vehicle)
        && isWithinDeviationTheta(p, position, vehicle)
        && isOnSameMapID(p, position, vehicle);
  }

  private boolean isOnSameMapID(Point p, AgvPosition position, Vehicle vehicle) {
    return position.getMapId().equals(getProperty(PROPKEY_VEHICLE_MAP_ID, p, vehicle).orElse(""));
  }

  private boolean isWithinDeviationXY(Point p, AgvPosition position, Vehicle vehicle) {
    double deviationX = Math.abs(p.getPose().getPosition().getX() / 1000.0 - position.getX());
    double deviationY = Math.abs(p.getPose().getPosition().getY() / 1000.0 - position.getY());

    return Math.sqrt((deviationX * deviationX) + (deviationY * deviationY)) <= getPropertyDouble(
        PROPKEY_VEHICLE_DEVIATION_XY, p, vehicle
    ).orElse(0.0);
  }

  private boolean isWithinDeviationTheta(Point p, AgvPosition position, Vehicle vehicle) {
    if (Double.isNaN(p.getPose().getOrientationAngle())) {
      return true;
    }
    return AngleMath.angleBetween(
        p.getPose().getOrientationAngle(), toDegrees(position.getTheta())
    ) <= getPropertyDouble(PROPKEY_VEHICLE_DEVIATION_THETA, p, vehicle).orElse(0.0);
  }
}
