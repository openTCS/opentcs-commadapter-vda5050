/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import javax.inject.Singleton;
import org.opentcs.commadapter.vehicle.vda5050.Vda5050CommAdapterFactory;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.CommAdapterFactory.V1dot1;
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
    bind(MessageValidator.class).in(Singleton.class);

    bind(Vda5050CommAdapterFactory.class)
        .annotatedWith(V1dot1.class)
        .to(CommAdapterFactory.class);

    install(new FactoryModuleBuilder().build(CommAdapterComponentsFactory.class));
  }
}
