/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.connection.Connection;

/**
 * Unit tests for {@link MessageValidator}.
 */
public class MessageValidatorTest {

  private MessageValidator messageValidator;

  @BeforeEach
  public void setUp() {
    messageValidator = new MessageValidator();
  }

  @Test
  public void acceptValidMessage() {
    messageValidator.validate(
        validConnectionMessage(),
        Connection.class
    );
  }

  @Test
  public void throwOnInvalidMessage() {
    assertThrows(
        IllegalArgumentException.class,
        () -> messageValidator.validate(
            connectionMessageWithNullConnectionState(),
            Connection.class
        )
    );
  }

  @Test
  public void throwOnEmptyMessage() {
    assertThrows(
        IllegalArgumentException.class,
        () -> messageValidator.validate(
            "",
            Connection.class
        )
    );
  }

  private static String validConnectionMessage() {
    return "{\n"
        + "  \"headerId\" : 0,\n"
        + "  \"timestamp\" : \"1970-01-01T00:00:00Z\",\n"
        + "  \"version\" : \"version\",\n"
        + "  \"manufacturer\" : \"manufacturer\",\n"
        + "  \"serialNumber\" : \"serial-number\",\n"
        + "  \"connectionState\" : \"ONLINE\"\n"
        + "}";
  }

  private static String connectionMessageWithNullConnectionState() {
    return "{\n"
        + "  \"headerId\" : 0,\n"
        + "  \"timestamp\" : \"1970-01-01T00:00:00Z\",\n"
        + "  \"version\" : \"version\",\n"
        + "  \"manufacturer\" : \"manufacturer\",\n"
        + "  \"serialNumber\" : \"serial-number\",\n"
        + "  \"connectionState\" : null\n"
        + "}";
  }

}
