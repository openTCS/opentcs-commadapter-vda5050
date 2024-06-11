/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.controlcenter.AdapterPanelComponentsFactory;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.controlcenter.CommAdapterPanelFactoryImpl;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.controlcenter.action.prefill.PrefillDialogFactory;
import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;

/**
 * A custom Guice module for project-specific configuration.
 */
public class ControlCenterInjectionModuleImpl
    extends
      ControlCenterInjectionModule {

  /**
   * Creates a new instance.
   */
  public ControlCenterInjectionModuleImpl() {
  }

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(AdapterPanelComponentsFactory.class));
    install(new FactoryModuleBuilder().build(PrefillDialogFactory.class));

    commAdapterPanelFactoryBinder().addBinding().to(CommAdapterPanelFactoryImpl.class);
  }
}
