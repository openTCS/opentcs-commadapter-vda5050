/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.common;

/**
 * Expresses the extent to which an optional parameter in a messsage is supported by a vehicle.
 */
public enum OptionalParameterSupport {
  /**
   * The optional parameter is supported by the vehicle.
   */
  SUPPORTED,
  /**
   * The optional parameter is not supported by the vehicle.
   */
  NOT_SUPPORTED,
  /**
   * The optional parameter required by the vehicle.
   */
  REQUIRED
}
