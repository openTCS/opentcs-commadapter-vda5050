/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Regulates if an {@link Action} is allowed to be executed during movement and/or parallel to other
 * actions.
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public enum BlockingType {
    
  /**
   * Action can be executed in parallel with other actions and while the vehicle is driving.
   */
  @JsonProperty(value = "NONE")
  NONE,
  /**
   * Action can be executed in parallel with other actions. Vehicle must not drive.
   */
  @JsonProperty(value = "SOFT")
  SOFT,
  /**
   * Action must not be executed in parallel with other actions. Vehicle must not drive.
   */
  @JsonProperty(value = "HARD")
  HARD;
}
