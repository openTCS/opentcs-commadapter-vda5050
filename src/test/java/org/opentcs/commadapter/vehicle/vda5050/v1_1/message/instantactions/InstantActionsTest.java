/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.instantactions;

import java.time.Instant;
import java.util.List;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.common.JsonBinder;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.MessageValidator;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.BlockingType;

/**
 * Unit tests for {@link InstantActions}.
 */
public class InstantActionsTest {

  private MessageValidator messageValidator;
  private JsonBinder jsonBinder;
  private InstantActions instantActions;

  @BeforeEach
  public void setUp() {
    messageValidator = new MessageValidator();
    jsonBinder = new JsonBinder();

    instantActions
        = new InstantActions(
            0L,
            Instant.EPOCH,
            "version",
            "manufacturer",
            "serial-number",
            List.of(
                new Action("some-action-type", "action1", BlockingType.NONE)
                    .setActionDescription("action-description")
                    .setActionParameters(
                        List.of(
                            new ActionParameter("some-key", "some-value"),
                            new ActionParameter("some-other-key", "some-other-value")
                        )
                    ),
                new Action("some-other-action-type", "action2", BlockingType.SOFT)
                    .setActionDescription("action-description")
                    .setActionParameters(
                        List.of(
                            new ActionParameter("some-key", "some-value"),
                            new ActionParameter("some-other-key", "some-other-value")
                        )
                    )
            )
        );
  }

  @Test
  public void validateAgainstJsonSchema() {
    messageValidator.validate(jsonBinder.toJson(instantActions), InstantActions.class);
  }

  @Test
  public void jsonSample() {
    Approvals.verify(jsonBinder.toJson(instantActions));
  }

}
