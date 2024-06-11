/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * An order is associated with a movement command.
 * A movement command consists of a movement to a position and a set of actions to perform.
 * The set of actions may be empty.
 */
public class OrderAssociation {

  /**
   * The order that is produced by a movement command.
   */
  private final Order order;
  /**
   * The command that produced an order.
   */
  private final MovementCommand command;

  /**
   * Creates a new instance.
   *
   * @param order The order to track.
   * @param command The movement command to track.
   */
  public OrderAssociation(
      @Nonnull
      Order order,
      @Nonnull
      MovementCommand command
  ) {
    this.order = order;
    this.command = command;
  }

  @Nonnull
  public Order getOrder() {
    return order;
  }

  @Nonnull
  public MovementCommand getCommand() {
    return command;
  }

  @Override
  public String toString() {
    return "OrderAssociation{"
        + "order=" + order
        + ", command=" + command
        + "}";
  }
}
