/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
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
