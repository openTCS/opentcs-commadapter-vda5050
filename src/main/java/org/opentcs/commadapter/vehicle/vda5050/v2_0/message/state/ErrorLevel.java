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
 * Defines various error levels.
 */
public enum ErrorLevel {

  /**
   * AGV is ready to start. (E.g. maintenance cycle expiration warning.)
   */
  @JsonProperty(value = "WARNING")
  WARNING,
  /**
   * AGV is not in running condition, user intervention required. (E.g. laser scanner is
   * contaminated.)
   */
  @JsonProperty(value = "FATAL")
  FATAL;
}
