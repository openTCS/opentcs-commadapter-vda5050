/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import org.opentcs.commadapter.vehicle.vda5050.common.DistanceInAdvanceController;
import org.opentcs.commadapter.vehicle.vda5050.common.OptionalParameterSupport;
import org.opentcs.commadapter.vehicle.vda5050.common.UnsupportedPropertiesFilter;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping.OrderMapper;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;

/**
 * A factory for various instances specific to {@link CommAdapterImpl}.
 */
public interface CommAdapterComponentsFactory {

  /**
   * Creates a new {@link CommAdapterImpl} for the given vehicle.
   *
   * @param vehicle The vehicle.
   * @return A new communication adapter instance for the given vehicle.
   */
  CommAdapterImpl createCommAdapterImpl(Vehicle vehicle);

  /**
   * Creates a new objcet mapper.
   *
   * @param vehicleReference Reference to the vehicle.
   * @param isActionExecutable A predicate to test if an action is executable.
   * @return The new object mapper.
   */
  OrderMapper createOrderMapper(
      TCSObjectReference<Vehicle> vehicleReference,
      Predicate<String> isActionExecutable
  );

  /**
   * Creates a new vehicle position resolver.
   *
   * @param vehicleReference Reference to the vehicle.
   * @return The new vehicle position resolver.
   */
  VehiclePositionResolver createVehiclePositionResolver(
      TCSObjectReference<Vehicle> vehicleReference
  );

  /**
   * Creates a new {@link MovementCommandManager} for the given vehicle.
   *
   * @param vehicle The vehicle.
   * @return A new instance.
   */
  MovementCommandManager createMovementCommandManager(Vehicle vehicle);

  /**
   * Creates a new {@link UnsupportedPropertiesFilter}.
   *
   * @param vehicle The vehicle.
   * @param propertiesExtractor The function to extract unsupported properties from the vehicle.
   * @return A new instance.
   */
  UnsupportedPropertiesFilter createUnsupportedPropertiesFilter(
      Vehicle vehicle,
      Function<Vehicle, Map<String, OptionalParameterSupport>> propertiesExtractor
  );

  /**
   * Creates a new {@link DistanceInAdvanceController} for the given vehicle.
   *
   * @param maxDistanceInAdvance The maximum distance that may be covered in advance.
   * @return A new instance.
   */
  DistanceInAdvanceController createDistanceInAdvanceController(long maxDistanceInAdvance);
}
