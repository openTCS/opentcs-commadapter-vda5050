/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.controlcenter.commands;

import static java.util.Objects.requireNonNull;

import org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterImpl;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Order;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command for sending a {@link Order} to a vehicle.
 */
public class SendOrderCommand
    implements
      AdapterCommand {

  /**
   * The Order to send.
   */
  private final Order order;

  /**
   * Creates a new instance.
   *
   * @param order The request to send.
   */
  public SendOrderCommand(Order order) {
    this.order = requireNonNull(order, "order");
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (!(adapter instanceof CommAdapterImpl)) {
      return;
    }

    CommAdapterImpl exampleAdapter = (CommAdapterImpl) adapter;
    exampleAdapter.sendOrder(order);
  }
}
