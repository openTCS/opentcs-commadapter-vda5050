/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter.action;

import java.util.Map;
import java.util.TreeMap;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.CancelOrder;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.DetectObject;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.Drop;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.FinePositioning;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.InitPosition;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.LogReport;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.Pick;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.StartCharging;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.StartPause;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.StateRequest;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.StopCharging;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.StopPause;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.action.WaitForTrigger;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;

/**
 * A {@link ComboBoxModel} for {@link Action}s that provides a selection of predefined actions as
 * templates.
 */
public class ActionComboBoxModel
    extends
      DefaultComboBoxModel<Action> {

  /**
   * The actions (including their action parameters) to provide as templates.
   */
  private static final Map<String, Action> ACTION_TEMPLATES = new TreeMap<>(
      Map.ofEntries(
          Map.entry(CancelOrder.ACTION_TYPE, new CancelOrder("", BlockingType.NONE)),
          Map.entry(DetectObject.ACTION_TYPE, new DetectObject("", BlockingType.NONE, "")),
          Map.entry(
              Drop.ACTION_TYPE, new Drop("", BlockingType.NONE, "", "", "", "", "", 0F, 0F, "")
          ),
          Map.entry(
              FinePositioning.ACTION_TYPE, new FinePositioning("", BlockingType.NONE, "", "")
          ),
          Map.entry(
              InitPosition.ACTION_TYPE,
              new InitPosition("", BlockingType.NONE, "", "", "", "", "")
          ),
          Map.entry(LogReport.ACTION_TYPE, new LogReport("", BlockingType.NONE, "")),
          Map.entry(
              Pick.ACTION_TYPE, new Pick("", BlockingType.NONE, "", "", "", "", "", 0F, 0F, "")
          ),
          Map.entry(StartCharging.ACTION_TYPE, new StartCharging("", BlockingType.NONE)),
          Map.entry(StartPause.ACTION_TYPE, new StartPause("", BlockingType.NONE)),
          Map.entry(StateRequest.ACTION_TYPE, new StateRequest("", BlockingType.NONE)),
          Map.entry(StopCharging.ACTION_TYPE, new StopCharging("", BlockingType.NONE)),
          Map.entry(StopPause.ACTION_TYPE, new StopPause("", BlockingType.NONE)),
          Map.entry(WaitForTrigger.ACTION_TYPE, new WaitForTrigger("", BlockingType.NONE, ""))
      )
  );

  public ActionComboBoxModel() {
    addAll(ACTION_TEMPLATES.values());
  }

  @Override
  public void setSelectedItem(Object anObject) {
    if (anObject instanceof ActionEditor.EditorAction) {
      // If an action type is entered manually into the combo box, check if there's an action
      // template for it. In case one exists, select it for a better user experience.
      Action action = (Action) anObject;
      if (ACTION_TEMPLATES.containsKey(action.getActionType())) {
        super.setSelectedItem(ACTION_TEMPLATES.get(action.getActionType()));
        return;
      }
    }

    super.setSelectedItem(anObject);
  }
}
