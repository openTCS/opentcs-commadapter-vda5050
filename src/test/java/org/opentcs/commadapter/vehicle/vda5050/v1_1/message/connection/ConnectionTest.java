/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.connection;

import java.time.Instant;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.common.JsonBinder;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.MessageValidator;

/**
 * Unit tests for {@link Connection}.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ConnectionTest {

  private MessageValidator messageValidator;
  private JsonBinder jsonBinder;
  private Connection connection;

  @BeforeEach
  public void setUp() {
    messageValidator = new MessageValidator();
    jsonBinder = new JsonBinder();
    connection = new Connection(0L,
                                Instant.EPOCH,
                                "version",
                                "manufacturer",
                                "serial-number",
                                ConnectionState.ONLINE);
  }

  @Test
  public void validateAgainstJsonSchema() {
    messageValidator.validate(jsonBinder.toJson(connection), Connection.class);
  }

  @Test
  public void jsonSample() {
    Approvals.verify(jsonBinder.toJson(connection));
  }

}
