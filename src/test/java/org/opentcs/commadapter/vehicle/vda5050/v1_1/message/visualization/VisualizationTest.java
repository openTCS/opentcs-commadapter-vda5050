/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.visualization;

import java.time.Instant;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.common.JsonBinder;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.MessageValidator;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.AgvPosition;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Velocity;

/**
 * Unit tests for {@link Visualization}.
 */
public class VisualizationTest {

  private MessageValidator messageValidator;
  private JsonBinder jsonBinder;
  private Visualization visualization;

  @BeforeEach
  public void setUp() {
    messageValidator = new MessageValidator();
    jsonBinder = new JsonBinder();

    visualization
        = new Visualization(0L,
                            Instant.EPOCH,
                            "version",
                            "manufacturer",
                            "serial-number")
            .setAgvPosition(
                new AgvPosition(1.2, 3.4, 1.2, "some-map-id", true)
                    .setDeviationRange(0.1)
                    .setLocalizationScore(0.2)
                    .setMapDescription("some-map-description")
            )
            .setVelocity(
                new Velocity()
                    .setVx(0.1)
                    .setVy(0.2)
                    .setOmega(0.3)
            );
  }

  @Test
  public void validateAgainstJsonSchema() {
    messageValidator.validate(jsonBinder.toJson(visualization), Visualization.class);
  }

  @Test
  public void jsonSample() {
    Approvals.verify(jsonBinder.toJson(visualization));
  }
}
