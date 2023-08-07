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
import static org.opentcs.commadapter.vehicle.vda5050.common.Conversions.toRelativeConvexAngle;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getProperty;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getPropertyDouble;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_THETA;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_XY;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_MAP_ID;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.NodePosition;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Node;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Function to map {@link Point}s from openTCS movement commands to a VDA5050 node.
 */
public class NodeMapping {

  /**
   * Prevents unwanted instantiation.
   */
  private NodeMapping() {
  }

  /**
   * Maps a {@link Point} to a VDA5050 node.
   *
   * @param point The point to map.
   * @param sequenceId The sequence ID for the node to be created.
   * @param vehicle The vehicle to mape the point for.
   * @param actions The actions for this node.
   * @param extendDeviationToIncludeVehicle Whether the node's deviation should be extended to
   * include the vehicle's position.
   * @return A mapped Node.
   */
  public static Node toNode(Point point,
                            long sequenceId,
                            Vehicle vehicle,
                            List<Action> actions,
                            boolean extendDeviationToIncludeVehicle) {
    requireNonNull(point, "point");
    requireNonNull(vehicle, "vehicle");
    requireNonNull(actions, "actions");

    Node node = new Node(
        point.getName(),
        sequenceId,
        true,
        actions
    );
    node.setNodePosition(toNodePosition(point, vehicle, extendDeviationToIncludeVehicle));
    return node;
  }

  /**
   * Maps the given point to a node position, filled with data taken from both the point and the
   * vehicle.
   *
   * @param point The point.
   * @param vehicle The vehicle.
   * @param extendDeviationToIncludeVehicle Whether the node's deviation should be extended to
   * include the vehicle's position.
   * @return A node position.
   */
  public static NodePosition toNodePosition(@Nonnull Point point,
                                            @Nonnull Vehicle vehicle,
                                            boolean extendDeviationToIncludeVehicle) {
    requireNonNull(point, "point");
    requireNonNull(vehicle, "vehicle");

    NodePosition position = new NodePosition(
        point.getPosition().getX() / 1000.0,
        point.getPosition().getY() / 1000.0,
        getProperty(PROPKEY_VEHICLE_MAP_ID, point, vehicle).orElse("")
    );

    if (!Double.isNaN(point.getVehicleOrientationAngle())) {
      position.setTheta(
          toRadians(toRelativeConvexAngle(point.getVehicleOrientationAngle()))
      );
    }

    position.setAllowedDeviationXY(
        extendDeviationToIncludeVehicle
            ? extendedDeviationXY(point, vehicle)
            : regularDeviationXY(point, vehicle)
    );

    position.setAllowedDeviationTheta(
        extendDeviationToIncludeVehicle
            ? extendedDeviationTheta()
            : regularDeviationTheta(point, vehicle)
    );

    return position;
  }

  private static Double regularDeviationXY(Point point, Vehicle vehicle) {
    return getPropertyDouble(PROPKEY_VEHICLE_DEVIATION_XY, point, vehicle).orElse(null);
  }

  private static Double regularDeviationTheta(Point point, Vehicle vehicle) {
    // XXX Ensure the angle is (positive and) within 0 and 180 degrees.
    return getPropertyDouble(PROPKEY_VEHICLE_DEVIATION_THETA, point, vehicle)
        .map(value -> toRadians(value))
        .orElse(null);
  }

  private static Double extendedDeviationXY(Point point, Vehicle vehicle) {
    // Ensure the deviation range is large enough for the vehicle to accept this node.
    double deltaX = (vehicle.getPrecisePosition().getX() - point.getPosition().getX()) / 1000.0;
    double deltaY = (vehicle.getPrecisePosition().getY() - point.getPosition().getY()) / 1000.0;

    return Math.sqrt(deltaX * deltaX + deltaY * deltaY) + 0.01;
  }

  private static Double extendedDeviationTheta() {
    return Math.PI;
  }
}
