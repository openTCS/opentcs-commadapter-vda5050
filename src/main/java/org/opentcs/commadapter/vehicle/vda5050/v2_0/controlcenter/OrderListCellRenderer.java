/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter;

import java.awt.Component;
import java.time.Instant;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Edge;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;

/**
 * Renders orders when displayed in a list.
 */
public class OrderListCellRenderer
    extends
      DefaultListCellRenderer {

  /**
   * A prototype for the list to compute its preferred size.
   */
  public static final Order PROTOTYPE_ORDER
      = new Order(0L, Instant.EPOCH, "", "", "", "", 0L, List.of(), List.of());

  /**
   * Creates a new instance.
   */
  public OrderListCellRenderer() {
  }

  @Override
  public Component getListCellRendererComponent(
      JList<?> list,
      Object value,
      int index,
      boolean isSelected,
      boolean cellHasFocus
  ) {
    Component component = super.getListCellRendererComponent(
        list,
        value,
        index,
        isSelected,
        cellHasFocus
    );

    if (value instanceof Order) {
      Order order = (Order) value;
      JLabel label = (JLabel) component;

      StringBuilder sb = new StringBuilder();
      sb.append("ID: ");
      sb.append(order.getOrderId());
      sb.append(", UID: ");
      sb.append(order.getOrderUpdateId());
      sb.append(", Path: ");
      sb.append(extractFirstEdge(order));

      String labelText = sb.toString();
      label.setText(labelText);
      label.setToolTipText(labelText);
    }

    return component;
  }

  private String extractFirstEdge(Order order) {
    return order.getEdges().stream()
        .findFirst()
        .map(Edge::getEdgeId)
        .orElse("-");
  }
}
