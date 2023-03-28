/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.MqttClientManager;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.MqttConfiguration;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KernelInjectionModuleImpl
    extends KernelInjectionModule {

  private static final Logger LOG = LoggerFactory.getLogger(KernelInjectionModuleImpl.class);

  /**
   * Creates a new instance.
   */
  public KernelInjectionModuleImpl() {
  }

  @Override
  protected void configure() {
    configureMqtt();

    CommAdapterConfiguration configuration
        = getConfigBindingProvider().get(CommAdapterConfiguration.PREFIX,
                                         CommAdapterConfiguration.class);
    bind(CommAdapterConfiguration.class).toInstance(configuration);

    List<String> trimmedEnabledVersions
        = configuration.enabledVersions().stream()
            .map(entry -> entry.trim())
            .collect(Collectors.toList());
    if (trimmedEnabledVersions.contains("1.1")) {
      LOG.info("VDA5050 1.1 communication adapter enabled.");
      install(new org.opentcs.commadapter.vehicle.vda5050.v1_1.KernelInjectionModuleImpl());
    }
    else {
      LOG.info("VDA5050 1.1 communication adapter disabled by configuration.");
    }
    if (trimmedEnabledVersions.contains("2.0")) {
      LOG.info("VDA5050 2.0 communication adapter enabled.");
      install(new org.opentcs.commadapter.vehicle.vda5050.v2_0.KernelInjectionModuleImpl());
    }
    else {
      LOG.info("VDA5050 2.0 communication adapter disabled by configuration.");
    }
  }

  private void configureMqtt() {
    bind(MqttConfiguration.class).toInstance(
        getConfigBindingProvider().get(MqttConfiguration.PREFIX, MqttConfiguration.class)
    );
    bind(MqttClientManager.class).in(Singleton.class);
  }
}
