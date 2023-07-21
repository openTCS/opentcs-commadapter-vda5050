/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.controlcenter;

import org.opentcs.components.kernel.services.VehicleService;

/**
 * A factory for creating various comm adapter panel specific instances.
 */
public interface AdapterPanelComponentsFactory {

  /**
   * Creates a {@link ControlPanel} representing the given process model's content.
   *
   * @param processModel The process model to represent.
   * @param vehicleService The vehicle service used for interaction with the comm adapter.
   * @return The control panel.
   */
  ControlPanel createControlPanel(ProcessModelImplTO processModel,
                                  VehicleService vehicleService);

  /**
   * Creates a {@link StatusPanel} representing the given process model's content.
   *
   * @param processModel The process model to represent.
   * @param vehicleService The vehicle service used for interaction with the comm adapter.
   * @return The status panel.
   */
  StatusPanel createStatusPanel(ProcessModelImplTO processModel,
                                VehicleService vehicleService);
}
