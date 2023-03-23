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
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public enum EStop {
  
  /**
   * Auto-acknowledgeable e-stop is activated. (E.g. by bumper or protective field.)
   */
  @JsonProperty(value = "AUTOACK")
  AUTOACK,
  /**
   * E-stop has to be acknowledged manually at the vehicle.
   */
  @JsonProperty(value = "MANUAL")
  MANUAL,
  /**
   * Facility e-stop has to be acknowledged remotely.
   */
  @JsonProperty(value = "REMOTE")
  REMOTE,
  /**
   * No e-stop activated.
   */
  @JsonProperty(value = "NONE")
  NONE;
}
