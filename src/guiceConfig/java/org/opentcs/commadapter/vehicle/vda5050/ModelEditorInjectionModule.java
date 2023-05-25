/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050;

import org.opentcs.customizations.plantoverview.PlantOverviewInjectionModule;

public class ModelEditorInjectionModule
    extends PlantOverviewInjectionModule {

  /**
   * Creates a new instance.
   */
  public ModelEditorInjectionModule() {
  }

  @Override
  protected void configure() {
    install(new org.opentcs.commadapter.vehicle.vda5050.v1_1.ModelEditorInjectionModule());
    install(new org.opentcs.commadapter.vehicle.vda5050.v2_0.ModelEditorInjectionModule());
  }
}
