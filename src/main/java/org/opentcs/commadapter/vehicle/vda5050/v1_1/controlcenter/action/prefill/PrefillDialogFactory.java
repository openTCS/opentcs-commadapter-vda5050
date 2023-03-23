/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.controlcenter.action.prefill;

import java.awt.Component;

/**
 * A factory for creating prefill dialog panels.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public interface PrefillDialogFactory {

  InitPositionPrefillDialog createInitPositionPrefillDialog(Component parent, boolean modal);
}
