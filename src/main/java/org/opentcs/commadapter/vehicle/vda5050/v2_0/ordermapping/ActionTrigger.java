/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping;

/**
 * When an action should be executed at a point.
 */
public enum ActionTrigger {
  /**
   * Action should be executed when the vehicle is passing the element.
   */
  PASSING,
  /**
   * Action should be exectued when it is at the start of an order.
   */
  ORDER_START,
  /**
   * Action should be executed when it is at the end of an order.
   */
  ORDER_END;
}
