// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import org.opentcs.data.TCSObject;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Constants and utility methods for working with properties on {@link TCSObject}s and
 * {@link MovementCommand}s.
 */
public interface ObjectProperties {

  /**
   * Manufacturer of this vehicle.
   */
  String PROPKEY_VEHICLE_MANUFACTURER = "vda5050:manufacturer";
  /**
   * Serial number for the vehicle.
   */
  String PROPKEY_VEHICLE_SERIAL_NUMBER = "vda5050:serialNumber";
  /**
   * The interface name.
   */
  String PROPKEY_VEHICLE_INTERFACE_NAME = "vda5050:interfaceName";
  /**
   * Default map id for this vehicle.
   */
  String PROPKEY_VEHICLE_MAP_ID = "vda5050:mapId";
  /**
   * Default xy deviation for this vehicle.
   */
  String PROPKEY_VEHICLE_DEVIATION_XY = "vda5050:deviationXY";
  /**
   * Default orientation deviation for this vehicle.
   */
  String PROPKEY_VEHICLE_DEVIATION_THETA = "vda5050:deviationTheta";
  /**
   * Vehicle length when vehicle does not report any loads.
   */
  String PROPKEY_VEHICLE_LENGTH_UNLOADED = "vda5050:vehicleLengthUnloaded";
  /**
   * Vehicle length when vehicle reports at least one load.
   */
  String PROPKEY_VEHICLE_LENGTH_LOADED = "vda5050:vehicleLengthLoaded";
  /**
   * The key of the vehicle property containing the vehicle's list of fatal errors.
   */
  String PROPKEY_VEHICLE_ERRORS_FATAL = "vda5050:errors.fatal";
  /**
   * The key of the vehicle property containing the vehicle's list of warnings.
   */
  String PROPKEY_VEHICLE_ERRORS_WARNING = "vda5050:errors.warning";
  /**
   * The key of the vehicle property containing the vehicle's list of info messages.
   */
  String PROPKEY_VEHICLE_INFORMATIONS_INFO = "vda5050:information.info";
  /**
   * The key of the vehicle property containing the vehicle's list of debug messages.
   */
  String PROPKEY_VEHICLE_INFORMATIONS_DEBUG = "vda5050:information.debug";
  /**
   * The key of the vehicle property containing the vehicle's paused state.
   */
  String PROPKEY_VEHICLE_PAUSED = "vda5050:paused";
  /**
   * The key of the vehicle property containing the vehicle's recharge operation.
   */
  String PROPKEY_VEHICLE_RECHARGE_OPERATION = "vda5050:rechargeOperation";
  /**
   * The key of the vehicle property containing the vehicle's maximum number of base steps.
   */
  String PROPKEY_VEHICLE_MAX_STEPS_BASE = "vda5050:maxStepsBase";
  /**
   * The key of the vehicle property containing the vehicle's maximum number of horizon steps.
   */
  String PROPKEY_VEHICLE_MAX_STEPS_HORIZON = "vda5050:maxStepsHorizon";
  /**
   * The key of the vehicle property containing the minimum visualization interval.
   */
  String PROPKEY_VEHICLE_MIN_VISU_INTERVAL = "vda5050:minVisualizationInterval";
  /**
   * The key of the vehicle property containing the movement command completed condition.
   */
  String PROPKEY_VEHICLE_MOVEMENT_COMMAND_COMPLETED_CONDITION
      = "vda5050:movementCommandCompletedCondition";
  /**
   * The key of the vehicle property containing the distance that is added to the extended
   * deviation range of a node.
   */
  String PROPKEY_VEHICLE_EXTEDNED_DEVIATION_RANGE_PADDING = "vda5050:extendedDeviationRangePadding";
  /**
   * The prefix for vehicle properties that define the support status of optional parameters in
   * order messages.
   */
  String PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX = "vda5050:optionalParams.order";
  /**
   * The key of the vehicle property containing the maximum distance (in mm)) that should be covered
   * by the movement commands the vehicle receives in advance.
   */
  String PROPKEY_VEHICLE_MAX_DISTANCE_IN_ADVANCE = "vda5050:maxDistanceBase";
  /**
   * The key property containing a list of executable action tags.
   */
  String PROPKEY_EXECUTABLE_ACTIONS_TAGS = "vda5050:actionTags";
  /**
   * The vehicle orientation on a path/edge with forward movement.
   */
  String PROPKEY_PATH_ORIENTATION_FORWARD = "vda5050:orientation.forward";
  /**
   * The vehicle orientation on a path/edge with reverse movement.
   */
  String PROPKEY_PATH_ORIENTATION_REVERSE = "vda5050:orientation.reverse";
  /**
   * The key of the property indicating whether vehicle rotation is allowed on a path/edge with
   * forward movement.
   */
  String PROPKEY_PATH_ROTATION_ALLOWED_FORWARD = "vda5050:rotationAllowed.forward";
  /**
   * The key of the property indicating whether vehicle rotation is allowed on a path/edge with
   * reverse movement.
   */
  String PROPKEY_PATH_ROTATION_ALLOWED_REVERSE = "vda5050:rotationAllowed.reverse";
  /**
   * The prefix for properties that define actions at points, paths and locations.
   */
  String PROPKEY_CUSTOM_ACTION_PREFIX = "vda5050:action";
  /**
   * The prefix for properties that define destination actions at location types, locations and
   * transport order destinations.
   */
  String PROPKEY_CUSTOM_DEST_ACTION_PREFIX = "vda5050:destinationAction";
  /**
   * Default map id for this point.
   */
  String PROPKEY_POINT_MAP_ID = PROPKEY_VEHICLE_MAP_ID;
  /**
   * Default xy deviation for this point.
   */
  String PROPKEY_POINT_DEVIATION_XY = PROPKEY_VEHICLE_DEVIATION_XY;
  /**
   * Default orientation deviation for this point.
   */
  String PROPKEY_POINT_DEVIATION_THETA = PROPKEY_VEHICLE_DEVIATION_THETA;
}
