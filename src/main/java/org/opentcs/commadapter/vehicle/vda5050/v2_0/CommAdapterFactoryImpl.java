/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter.CommAdapterDescriptionImpl;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 */
public class CommAdapterFactoryImpl
    implements VehicleCommAdapterFactory {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CommAdapterFactoryImpl.class);

  /**
   * The components factory responsible to create all components needed for the comm adapter.
   */
  private final CommAdapterComponentsFactory componentsFactory;
  /**
   * This component's initialized flag.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param componentsFactory The factory to create components specific to the comm adapter.
   */
  @Inject
  public CommAdapterFactoryImpl(CommAdapterComponentsFactory componentsFactory) {
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
  }

  @Override
  public void initialize() {
    if (initialized) {
      LOG.debug("Already initialized.");
      return;
    }
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      LOG.debug("Not initialized.");
      return;
    }
    initialized = false;
  }

  @Override
  public VehicleCommAdapterDescription getDescription() {
    return new CommAdapterDescriptionImpl();
  }

  @Override
  public boolean providesAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    
    if(vehicle.getProperty(PROPKEY_VEHICLE_INTERFACE_NAME) == null){
      return false;
    }
    
    if(vehicle.getProperty(PROPKEY_VEHICLE_MANUFACTURER) == null){
      return false;
    }
    
    if(vehicle.getProperty(PROPKEY_VEHICLE_SERIAL_NUMBER) == null){
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
