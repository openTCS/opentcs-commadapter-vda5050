/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050;

import javax.inject.Singleton;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.MqttClientManager;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.MqttConfiguration;
import org.opentcs.customizations.kernel.KernelInjectionModule;

public class KernelInjectionModuleImpl
    extends
      KernelInjectionModule {

  /**
   * Creates a new instance.
   */
  public KernelInjectionModuleImpl() {
  }

  @Override
  protected void configure() {
    configureMqtt();

    bind(CommAdapterConfiguration.class)
        .toInstance(
            getConfigBindingProvider().get(
                CommAdapterConfiguration.PREFIX,
                CommAdapterConfiguration.class
            )
        );

    install(new org.opentcs.commadapter.vehicle.vda5050.v1_1.KernelInjectionModuleImpl());
    install(new org.opentcs.commadapter.vehicle.vda5050.v2_0.KernelInjectionModuleImpl());

    vehicleCommAdaptersBinder().addBinding().to(CommAdapterFactoryImpl.class);
  }

  private void configureMqtt() {
    bind(MqttConfiguration.class).toInstance(
        getConfigBindingProvider().get(MqttConfiguration.PREFIX, MqttConfiguration.class)
    );
    bind(MqttClientManager.class).in(Singleton.class);
  }
}
