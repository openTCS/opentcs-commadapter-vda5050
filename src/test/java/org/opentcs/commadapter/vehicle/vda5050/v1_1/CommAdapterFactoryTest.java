/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.opentcs.data.model.Vehicle;

/**
 */
public class CommAdapterFactoryTest {

  private CommAdapterFactory commAdapterFactory;

  @BeforeEach
  public void setUp() {
    commAdapterFactory = new CommAdapterFactory(mock(CommAdapterComponentsFactory.class));
  }

  @Test
  public void provideAdapterForVehicleWithAllProperties() {
    assertTrue(commAdapterFactory.providesAdapterFor(
        new Vehicle("Some vehicle")
            .withProperty(ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME, "openTCS")
            .withProperty(ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER, "iml")
            .withProperty(ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER, "0001")
    ));
  }

  @Test
  public void provideAdapterForVehicleMissingInterfaceName() {
    assertFalse(commAdapterFactory.providesAdapterFor(
        new Vehicle("Some vehicle")
            .withProperty(ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER, "iml")
            .withProperty(ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER, "0001")
    ));
  }

  @Test
  public void provideAdapterForVehicleMissingManufacturer() {
    assertFalse(commAdapterFactory.providesAdapterFor(
        new Vehicle("Some vehicle")
            .withProperty(ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME, "openTCS")
            .withProperty(ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER, "0001")
    ));
  }

  @Test
  public void provideAdapterForVehicleMissingSerialNumber() {
    assertFalse(commAdapterFactory.providesAdapterFor(
        new Vehicle("Some vehicle")
            .withProperty(ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME, "openTCS")
            .withProperty(ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER, "iml")
    ));
  }
}
