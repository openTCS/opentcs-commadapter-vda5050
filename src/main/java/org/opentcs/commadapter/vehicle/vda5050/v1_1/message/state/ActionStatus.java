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
 * Defines the different state of an {@link ActionState}.
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public enum ActionStatus {

  /**
   * Waiting.
   */
  @JsonProperty(value = "WAITING")
  WAITING,
  /**
   * Initializing.
   */
  @JsonProperty(value = "INITIALIZING")
  INITIALIZING,
  /**
   * Running.
   */
  @JsonProperty(value = "RUNNING")
  RUNNING,
  /**
   * Finished.
   */
  @JsonProperty(value = "FINISHED")
  FINISHED,
  /**
   * Failed.
   */
  @JsonProperty(value = "FAILED")
  FAILED;
}
