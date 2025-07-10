// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.commands;

import org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterImpl;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 *
 * Adapter command for sending an instant action.
 */
public class SendInstantActions
    implements
      AdapterCommand {

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
