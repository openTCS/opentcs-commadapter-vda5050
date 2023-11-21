/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import java.util.function.Predicate;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping.OrderMapper;
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
  OrderMapper createOrderMapper(TCSObjectReference<Vehicle> vehicleReference,
                                Predicate<String> isActionExecutable);

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
}
