/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.opentcs.data.model.Path;
import org.opentcs.util.gui.StringListCellRenderer;

/**
 * A {@link ListCellRenderer} for {@link Path}s.
 * <p>
 * Extends {@link StringListCellRenderer} by additionally setting a tool tip text to account for
 * long path names.
 */
public class PathRenderer
    extends
      StringListCellRenderer<Path> {

  public PathRenderer() {
    super(path -> path.getName());
  }

  @Override
  public Component getListCellRendererComponent(
      JList<? extends Path> list,
      Path value,
      int index,
      boolean isSelected,
      boolean cellHasFocus
  ) {
    JLabel label = (JLabel) super.getListCellRendererComponent(
        list,
        value,
        index,
        isSelected,
        cellHasFocus
    );
    label.setToolTipText(label.getText());
    return label;
  }
}
