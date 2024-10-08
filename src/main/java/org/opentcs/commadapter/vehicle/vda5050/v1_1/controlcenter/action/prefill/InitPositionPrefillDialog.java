/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.controlcenter.action.prefill;

import static java.lang.Math.toRadians;
import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.common.AngleMath.toRelativeConvexAngle;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getProperty;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_POINT_MAP_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.action.InitPosition.PARAMKEY_LAST_NODE_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.action.InitPosition.PARAMKEY_MAP_ID;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.action.InitPosition.PARAMKEY_THETA;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.action.InitPosition.PARAMKEY_X;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.action.InitPosition.PARAMKEY_Y;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.model.Point;
import org.opentcs.util.Comparators;
import org.opentcs.util.gui.StringListCellRenderer;

/**
 * A prefill dialog for InitPosition actions.
 */
public class InitPositionPrefillDialog
    extends
      ActionPrefillDialog {

  /**
   * Wether or not the dialog was closed with ok.
   */
  private boolean wasClosedWithOK;

  /**
   * Creates new instance.
   *
   * @param servicePortal Service portal
   * @param parentComponent The parent component
   * @param modal Wether or not this dialog is modal
   */
  @Inject
  @SuppressWarnings("this-escape")
  public InitPositionPrefillDialog(
      KernelServicePortal servicePortal,
      @Assisted
      Component parentComponent,
      @Assisted
      boolean modal
  ) {
    super(parentComponent, modal);

    initComponents();
    initComboBox(requireNonNull(servicePortal, "servicePortal").getVehicleService());
  }

  private void initComboBox(VehicleService vehicleService) {
    vehicleService.fetchObjects(Point.class)
        .stream()
        .sorted(Comparators.objectsByName())
        .forEach(p -> {
          pointComboBox.addItem(p);
        });
  }

  @Override
  public Optional<Map<String, String>> showAndGetResult() {
    this.setVisible(true);

    if (!wasClosedWithOK) {
      return Optional.empty();
    }

    Point p = (Point) pointComboBox.getSelectedItem();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(PARAMKEY_X, String.valueOf(p.getPose().getPosition().getX() / 1000.0));
    parameters.put(PARAMKEY_Y, String.valueOf(p.getPose().getPosition().getY() / 1000.0));
    if (!Double.isNaN(p.getPose().getOrientationAngle())) {
      parameters.put(
          PARAMKEY_THETA,
          String.valueOf(toRadians(toRelativeConvexAngle(p.getPose().getOrientationAngle())))
      );
    }
    Optional<String> mapID = getProperty(PROPKEY_POINT_MAP_ID, p);
    if (mapID.isPresent()) {
      parameters.put(PARAMKEY_MAP_ID, mapID.get());
    }
    parameters.put(PARAMKEY_LAST_NODE_ID, p.getName());
    return Optional.of(parameters);
  }

  // FORMATTER:OFF
  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    initPositionPrefillPanel = new javax.swing.JPanel();
    selectPointLabel = new javax.swing.JLabel();
    pointComboBox = new javax.swing.JComboBox<>();
    cancelButton = new javax.swing.JButton();
    okButton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setMinimumSize(new java.awt.Dimension(247, 100));
    getContentPane().setLayout(new java.awt.GridBagLayout());

    initPositionPrefillPanel.setLayout(new java.awt.GridBagLayout());

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/commadapter/vehicle/vda5050/v1_1/Bundle"); // NOI18N
    selectPointLabel.setText(bundle.getString("initPositionPrefillDialog.label_selectPoint.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    initPositionPrefillPanel.add(selectPointLabel, gridBagConstraints);

    pointComboBox.setRenderer(new StringListCellRenderer<Point>(point -> point.getName()));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    initPositionPrefillPanel.add(pointComboBox, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
    getContentPane().add(initPositionPrefillPanel, gridBagConstraints);

    cancelButton.setText(bundle.getString("initPositionPrefillDialog.button_cancel.text")); // NOI18N
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cancelButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 3);
    getContentPane().add(cancelButton, gridBagConstraints);

    okButton.setText(bundle.getString("initPositionPrefillDialog.button_ok.text")); // NOI18N
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        okButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 0);
    getContentPane().add(okButton, gridBagConstraints);

    pack();
  }// </editor-fold>//GEN-END:initComponents
  // CHECKSTYLE:ON
  // FORMATTER:ON

  private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    wasClosedWithOK = true;
    dispose();
  }//GEN-LAST:event_okButtonActionPerformed

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    wasClosedWithOK = false;
    dispose();
  }//GEN-LAST:event_cancelButtonActionPerformed

  // FORMATTER:OFF
  // CHECKSTYLE:OFF
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cancelButton;
  private javax.swing.JPanel initPositionPrefillPanel;
  private javax.swing.JButton okButton;
  private javax.swing.JComboBox<Point> pointComboBox;
  private javax.swing.JLabel selectPointLabel;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
  // FORMATTER:ON
}
