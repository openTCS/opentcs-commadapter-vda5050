/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.simulation;

import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.MqttConfiguration;

/**
 * An implementation of the configuration for vehicle simulation.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class ConfigurationImpl
    implements MqttConfiguration {

  /**
   * Creates a new instance.
   */
  public ConfigurationImpl() {
  }

  @Override
  public String brokerHost() {
    return "127.0.0.1";
  }

  @Override
  public int brokerPort() {
    return 1883;
  }

  @Override
  public boolean connectionEncrypted() {
    return false;
  }

  @Override
  public String username() {
    return "";
  }

  @Override
  public String password() {
    return "";
  }

  @Override
  public String clientId() {
    return "opentcs-vda5050-driver";
  }

  @Override
  public int keepAliveInterval() {
    return 10000;
  }

  @Override
  public int reconnectInterval() {
    return 2000;
  }

}
