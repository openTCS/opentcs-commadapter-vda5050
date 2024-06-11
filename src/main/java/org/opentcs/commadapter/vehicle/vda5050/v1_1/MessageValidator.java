/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import java.io.InputStreamReader;
import java.util.Map;
import org.opentcs.commadapter.vehicle.vda5050.common.JsonValidator;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.connection.Connection;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.State;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.visualization.Visualization;

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
