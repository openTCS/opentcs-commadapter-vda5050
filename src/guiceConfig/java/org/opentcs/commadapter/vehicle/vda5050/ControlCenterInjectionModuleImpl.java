/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050;

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
    install(new org.opentcs.commadapter.vehicle.vda5050.v1_1.ControlCenterInjectionModuleImpl());
    install(new org.opentcs.commadapter.vehicle.vda5050.v2_0.ControlCenterInjectionModuleImpl());
  }
}
