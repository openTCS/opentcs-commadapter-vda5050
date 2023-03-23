/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.connection;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the different state of a connection between an AGV and a message broker.
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public enum ConnectionState {

  /**
   * Connection between AGV and broker is active.
   */
  @JsonProperty(value = "ONLINE")
  ONLINE,
  /**
   * Connection between AGV and broker has gone offline in a coordinated way.
   */
  @JsonProperty(value = "OFFLINE")
  OFFLINE,
  /**
   * Connection between AGV and broker has unexpectedly ended.
   */
  @JsonProperty(value = "CONNECTIONBROKEN")
  CONNECTIONBROKEN;
}
