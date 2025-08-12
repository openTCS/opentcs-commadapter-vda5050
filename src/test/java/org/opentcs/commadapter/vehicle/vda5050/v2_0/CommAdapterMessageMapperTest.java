// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mock;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_DESCRIPTION;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_ACTION_TYPE;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_BLOCKING_TYPE;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_BLOCKING_TYPE;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_DESCRIPTION;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_PARAMETER_PREFIX;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_TYPE;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_ORDER_PARAM_EDGE;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_ORDER_PARAM_ORDER_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_ORDER_PARAM_ORDER_UPDATE_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.CommAdapterMessages.SEND_ORDER_PARAM_SOURCE_NODE;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Edge;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Node;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping.NodeMapping;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterMessage;
import org.opentcs.util.MapValueExtractor;

/**
 * Tests for {@link CommAdapterMessageMapper}.
 */
class CommAdapterMessageMapperTest {

  private CommAdapterMessageMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new CommAdapterMessageMapper(
        new Vehicle("vehicle-1"),
        new MapValueExtractor(),
        mock(TCSObjectService.class),
        mock(NodeMapping.class)
    );
  }

  @Test
  void mapToOrder() {
    VehicleCommAdapterMessage message = new VehicleCommAdapterMessage(
        CommAdapterMessages.SEND_ORDER_TYPE,
        Map.ofEntries(
            Map.entry(SEND_ORDER_PARAM_ORDER_ID, "order-id"),
            Map.entry(SEND_ORDER_PARAM_ORDER_UPDATE_ID, "7"),
            Map.entry(SEND_ORDER_PARAM_SOURCE_NODE, "source-node"),
            Map.entry(SEND_ORDER_PARAM_DESTINATION_NODE, "destination-node"),
            Map.entry(SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_TYPE, "action-type"),
            Map.entry(SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_ID, "action-id"),
            Map.entry(SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_DESCRIPTION, "action-description"),
            Map.entry(
                SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_BLOCKING_TYPE,
                BlockingType.HARD.name()
            ),
            Map.entry(
                SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_PARAMETER_PREFIX + "action-param-1",
                "value-1"
            ),
            Map.entry(
                SEND_ORDER_PARAM_DESTINATION_NODE_ACTION_PARAMETER_PREFIX + "action-param-2",
                "value-2"
            ),
            Map.entry(SEND_ORDER_PARAM_EDGE, "edge")
        )
    );

    var result = mapper.toOrder(message);

    assertThat(result)
        .hasValueSatisfying(order -> {
          assertThat(order.getOrderId()).isEqualTo("order-id");
          assertThat(order.getOrderUpdateId()).isEqualTo(7L);
          assertThat(order.getNodes())
              .hasSize(2)
              .extracting(Node::getNodeId, Node::getSequenceId, Node::isReleased)
              .containsExactly(
                  tuple("source-node", 0L, true),
                  tuple("destination-node", 1L, true)
              );
          assertThat(order.getEdges())
              .hasSize(1)
              .extracting(Edge::getEdgeId, Edge::getSequenceId, Edge::isReleased)
              .containsExactly(tuple("edge", 0L, true));

          assertThat(order.getNodes().getLast().getActions()).hasSize(1);
          Action destinationAction = order.getNodes().getLast().getActions().getFirst();
          assertThat(destinationAction.getActionType()).isEqualTo("action-type");
          assertThat(destinationAction.getActionId()).isEqualTo("action-id");
          assertThat(destinationAction.getActionDescription()).isEqualTo("action-description");
          assertThat(destinationAction.getBlockingType()).isEqualTo(BlockingType.HARD);
          assertThat(destinationAction.getActionParameters())
              .hasSize(2)
              .extracting(ActionParameter::getKey, ActionParameter::getValue)
              .contains(
                  tuple("action-param-1", "value-1"),
                  tuple("action-param-2", "value-2")
              );
        });
  }

  @Test
  void mapToAction() {
    VehicleCommAdapterMessage message = new VehicleCommAdapterMessage(
        CommAdapterMessages.SEND_INSTANT_ACTION_TYPE,
        Map.of(
            SEND_INSTANT_ACTION_PARAM_ACTION_TYPE, "action-type",
            SEND_INSTANT_ACTION_PARAM_ACTION_ID, "action-id",
            SEND_INSTANT_ACTION_PARAM_ACTION_DESCRIPTION, "action-description",
            SEND_INSTANT_ACTION_PARAM_BLOCKING_TYPE, BlockingType.HARD.name(),
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + "action-param-1", "value-1",
            SEND_INSTANT_ACTION_PARAM_PARAMETER_PREFIX + "action-param-2", "value-2"
        )
    );

    var result = mapper.toAction(message);

    assertThat(result)
        .hasValueSatisfying(action -> {
          assertThat(action.getActionType()).isEqualTo("action-type");
          assertThat(action.getActionId()).isEqualTo("action-id");
          assertThat(action.getActionDescription()).isEqualTo("action-description");
          assertThat(action.getBlockingType()).isEqualTo(BlockingType.HARD);
          assertThat(action.getActionParameters())
              .extracting(ActionParameter::getKey, ActionParameter::getValue)
              .contains(
                  tuple("action-param-1", "value-1"),
                  tuple("action-param-2", "value-2")
              );
        });
  }
}
