// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_VERSION;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opentcs.components.kernel.PositionDeviationPolicy;
import org.opentcs.components.kernel.PositionDeviationPolicyFactory;
import org.opentcs.data.model.Vehicle;

/**
 * A factory for position deviation policies for VDA5050 1.1 vehicles.
 */
public class PositionDeviationPolicyFactoryImpl
    implements
      PositionDeviationPolicyFactory {

  /**
   * Creates a new instance.
   */
  public PositionDeviationPolicyFactoryImpl() {
  }

  public Optional<PositionDeviationPolicy> createPolicyFor(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle, "vehicle");

    return Optional.of(vehicle)
        .filter(v -> v.getProperty(PROPKEY_VEHICLE_INTERFACE_NAME) != null)
        .filter(v -> v.getProperty(PROPKEY_VEHICLE_MANUFACTURER) != null)
        .filter(v -> v.getProperty(PROPKEY_VEHICLE_SERIAL_NUMBER) != null)
        .filter(v -> !Objects.equals(v.getProperty(PROPKEY_VEHICLE_VERSION), "2.0"))
        .map(PositionDeviationPolicyImpl::new);
  }
}
