/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.common;

/**
 * Limits somehow related to VDA5050.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public interface Limits {

  /**
   * Maximum value representable with an unsigned 32 bit integer.
   */
  long UINT32_MAX_VALUE = 4294967295L;

}
