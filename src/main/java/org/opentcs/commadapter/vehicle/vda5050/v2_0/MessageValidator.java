// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import java.io.InputStreamReader;
import java.util.Map;
import org.opentcs.commadapter.vehicle.vda5050.common.JsonValidator;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection.Connection;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.visualization.Visualization;

/**
 * A {@link JsonValidator} for VDA5050 1.1 messages.
 */
public class MessageValidator
    extends
      JsonValidator {

  /**
   * Creates a new instance.
   *
   * @throws IllegalArgumentException If there was a problem reading the VDA5050 schemas.
   */
  public MessageValidator()
      throws IllegalArgumentException {
    super(
        Map.of(
            Connection.class,
            new InputStreamReader(
                Connection.class.getResourceAsStream(Connection.JSON_SCHEMA_PATH)
            ),
            InstantActions.class,
            new InputStreamReader(
                InstantActions.class.getResourceAsStream(InstantActions.JSON_SCHEMA_PATH)
            ),
            Order.class,
            new InputStreamReader(
                Order.class.getResourceAsStream(Order.JSON_SCHEMA_PATH)
            ),
            State.class,
            new InputStreamReader(
                State.class.getResourceAsStream(State.JSON_SCHEMA_PATH)
            ),
            Visualization.class,
            new InputStreamReader(
                Visualization.class.getResourceAsStream(Visualization.JSON_SCHEMA_PATH)
            )
        )
    );
  }

}
