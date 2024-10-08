/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_VERSION;

import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.opentcs.commadapter.vehicle.vda5050.Vda5050CommAdapterFactory;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 */
public class CommAdapterFactory
    implements
      Vda5050CommAdapterFactory {

  /**
   * Annotation to identify this implementation.
   */
  @Qualifier
  @Target({PARAMETER})
  @Retention(RUNTIME)
  public @interface V2dot0 {
  }

  /**
   * The components factory responsible to create all components needed for the comm adapter.
   */
  private final CommAdapterComponentsFactory componentsFactory;

  /**
   * Creates a new instance.
   *
   * @param componentsFactory The factory to create components specific to the comm adapter.
   */
  @Inject
  public CommAdapterFactory(CommAdapterComponentsFactory componentsFactory) {
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
  }

  @Override
  public boolean providesAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    if (vehicle.getProperty(PROPKEY_VEHICLE_VERSION) == null) {
      return false;
    }

    if (!vehicle.getProperty(PROPKEY_VEHICLE_VERSION).trim().contains("2.0")) {
      return false;
    }

    if (vehicle.getProperty(PROPKEY_VEHICLE_INTERFACE_NAME) == null) {
      return false;
    }

    if (vehicle.getProperty(PROPKEY_VEHICLE_MANUFACTURER) == null) {
      return false;
    }

    if (vehicle.getProperty(PROPKEY_VEHICLE_SERIAL_NUMBER) == null) {
      return false;
    }

    return true;
  }

  @Override
  public VehicleCommAdapter getAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    if (!providesAdapterFor(vehicle)) {
      return null;
    }

    return componentsFactory.createCommAdapterImpl(vehicle);
  }
}
