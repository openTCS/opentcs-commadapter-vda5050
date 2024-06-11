/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter;

import static java.util.Objects.requireNonNull;

import java.awt.Component;
import javax.annotation.Nonnull;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.opentcs.commadapter.vehicle.vda5050.common.JsonBinder;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A non-modal dialog that shows the JSON representation of {@link State} message.
 */
public class StateMessageDialog
    extends
      JDialog {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(StateMessageDialog.class);
  /**
   * Binds JSON strings to objects and vice versa.
   */
  private final JsonBinder jsonBinder = new JsonBinder();
  /**
   * The state to show the JSON representation for.
   */
  private final State state;

  /**
   * Creates a new instance.
   *
   * @param parentComponent The parent component.
   * @param state The state to show the JSON representation for.
   */
  public StateMessageDialog(
      Component parentComponent,
      @Nonnull
      State state
  ) {
    super(JOptionPane.getFrameForComponent(parentComponent), false);
    this.state = requireNonNull(state, "state");

    initComponents();
    initGuiContent();
  }

  private void initGuiContent() {
    try {
      textAreaState.setText(jsonBinder.toJson(state));
    }
    catch (IllegalArgumentException e) {
      LOG.error("An error occured while serializing the state message.", e);
    }
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

    scrollPane = new javax.swing.JScrollPane();
    textAreaState = new javax.swing.JTextArea();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/commadapter/vehicle/vda5050/v2_0/Bundle"); // NOI18N
    setTitle(bundle.getString("stateMessageDialog.title")); // NOI18N

    scrollPane.setViewportView(textAreaState);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 738, Short.MAX_VALUE)
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents
  // CHECKSTYLE:ON
  // FORMATTER:ON

  // FORMATTER:OFF
  // CHECKSTYLE:OFF
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JScrollPane scrollPane;
  private javax.swing.JTextArea textAreaState;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
  // FORMATTER:ON
}
