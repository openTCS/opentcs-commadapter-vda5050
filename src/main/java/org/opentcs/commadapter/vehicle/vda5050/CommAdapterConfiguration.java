/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050;

import java.util.List;
import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the version 1.1 communication adapter.
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
@ConfigurationPrefix(CommAdapterConfiguration.PREFIX)
public interface CommAdapterConfiguration {

  /**
   * This configuration's prefix.
   */
  String PREFIX = "commadapter.vehicle.vda5050";

  @ConfigurationEntry(
      type = "List of VDA5050 version numbers",
      description = "See driver documentation.",
      orderKey = "0_enable")
  List<String> enabledVersions();
}
