/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public enum InfoLevel {

  /**
   * Used for visualization.
   */
  @JsonProperty(value = "INFO")
  INFO,
  /**
   * Used for debugging.
   */
  @JsonProperty(value = "DEBUG")
  DEBUG;
}
