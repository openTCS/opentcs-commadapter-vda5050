/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter.commands;

import org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterImpl;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 *
 * Adapter command for sending an instant action.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class SendInstantActions
    implements AdapterCommand {

  /**
   * The instant action object.
   */
  private final InstantActions instantActions;

  public SendInstantActions(InstantActions instantActions) {
    this.instantActions = instantActions;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (!(adapter instanceof CommAdapterImpl)) {
      return;
    }

    ((CommAdapterImpl) adapter).sendInstantAction(instantActions);
  }

}
