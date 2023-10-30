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
import java.util.Optional;
import javax.annotation.Nonnull;
import static org.opentcs.commadapter.vehicle.vda5050.common.AngleMath.toRelativeConvexAngle;
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
   * Maps a {@link Point} to a VDA5050 base node.
   * The node has the release flag set to true.
   *
   * @param point The point to map.
   * @param sequenceId The sequence ID for the node to be created.
   * @param vehicle The vehicle to map the point for.
   * @param actions The actions for this node.
   * @param extendDeviationToIncludeVehicle Whether the node's deviation should be extended to
   * include the vehicle's position.
   * @return A mapped Node.
   */
  public static Node toBaseNode(Point point,
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
   * Maps a {@link Point} to a VDA5050 horizon node.
   * The node has the release flag set to false.
   *
   * @param point The point to map.
   * @param sequenceId The sequence ID for the node to be created.
   * @param vehicle The vehicle to map the point for.
   * @param actions The actions for this node.
   * include the vehicle's position.
   * @return A mapped Node.
   */
  public static Node toHorizonNode(Point point,
                                   long sequenceId,
                                   Vehicle vehicle,
                                   List<Action> actions) {
    requireNonNull(point, "point");
    requireNonNull(vehicle, "vehicle");

    Node node = new Node(
        point.getName(),
        sequenceId,
        false,
        actions
    );
    node.setNodePosition(toNodePosition(point, vehicle, false));
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
        point.getPose().getPosition().getX() / 1000.0,
        point.getPose().getPosition().getY() / 1000.0,
        getProperty(PROPKEY_VEHICLE_MAP_ID, point, vehicle).orElse("")
    );

    if (!Double.isNaN(point.getPose().getOrientationAngle())) {
      position.setTheta(
          toRadians(toRelativeConvexAngle(point.getPose().getOrientationAngle()))
      );
    }

    position.setAllowedDeviationXY(
        extendDeviationToIncludeVehicle
            ? extendedDeviationXY(point, vehicle)
            : regularDeviationXY(point, vehicle).orElse(null)
    );

    position.setAllowedDeviationTheta(
        extendDeviationToIncludeVehicle
            ? extendedDeviationTheta()
            : regularDeviationTheta(point, vehicle).orElse(null)
    );

    return position;
  }

  private static Optional<Double> regularDeviationXY(Point point, Vehicle vehicle) {
    return getPropertyDouble(PROPKEY_VEHICLE_DEVIATION_XY, point, vehicle);
  }

  private static Optional<Double> regularDeviationTheta(Point point, Vehicle vehicle) {
    // XXX Ensure the angle is (positive and) within 0 and 180 degrees.
    return getPropertyDouble(PROPKEY_VEHICLE_DEVIATION_THETA, point, vehicle)
        .map(value -> toRadians(value));
  }

  private static Double extendedDeviationXY(Point point, Vehicle vehicle) {
    // Ensure the deviation range is large enough for the vehicle to accept this node.
    double deltaX
        = (vehicle.getPrecisePosition().getX() - point.getPose().getPosition().getX()) / 1000.0;
    double deltaY
        = (vehicle.getPrecisePosition().getY() - point.getPose().getPosition().getY()) / 1000.0;

    return Math.max(
        Math.sqrt(deltaX * deltaX + deltaY * deltaY) + 0.01,
        regularDeviationXY(point, vehicle).orElse(0.0)
    );
  }

  private static Double extendedDeviationTheta() {
    return Math.PI;
  }
}
