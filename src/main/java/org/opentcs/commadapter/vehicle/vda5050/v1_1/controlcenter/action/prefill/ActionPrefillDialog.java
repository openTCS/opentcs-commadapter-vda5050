/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.controlcenter.action.prefill;

import static org.opentcs.commadapter.vehicle.vda5050.v1_1.I18nCommAdapter.BUNDLE_PATH;

import java.awt.Component;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * Defines methods for an action prefill dialog.
 */
public abstract class ActionPrefillDialog
    extends
      JDialog {

  /**
   * The resource bundle.
   */
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_PATH);

  public ActionPrefillDialog(Component parentComponent, boolean modal) {
    super(
        JOptionPane.getFrameForComponent(parentComponent),
        BUNDLE.getString("actionPrefillDialog.title"),
        modal
    );
  }

  /**
   * Show the dialog and return the configured action parameters.
   *
   * @return The configured action parameters.
   */
  public abstract Optional<Map<String, String>> showAndGetResult();
}
