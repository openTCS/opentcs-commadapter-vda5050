/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050;

import java.util.ResourceBundle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * The example adapter's {@link VehicleCommAdapterDescription}.
 */
public class CommAdapterDescriptionImpl
    extends VehicleCommAdapterDescription {

  /**
   * Creates a new instance.
   */
  public CommAdapterDescriptionImpl() {
  }

  @Override
  public String getDescription() {
    return ResourceBundle.getBundle("i18n/org/opentcs/commadapter/vehicle/vda5050/Bundle").
        getString("commAdapterDescriptionImpl.adapterFactoryDescription");
  }

  @Override
  public boolean isSimVehicleCommAdapter() {
    return false;
  }
}
