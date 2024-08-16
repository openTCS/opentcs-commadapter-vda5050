/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import jakarta.inject.Singleton;
import org.opentcs.commadapter.vehicle.vda5050.Vda5050CommAdapterFactory;
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
        .annotatedWith(CommAdapterFactory.V2dot0.class)
        .to(CommAdapterFactory.class);

    install(new FactoryModuleBuilder().build(CommAdapterComponentsFactory.class));
  }
}
