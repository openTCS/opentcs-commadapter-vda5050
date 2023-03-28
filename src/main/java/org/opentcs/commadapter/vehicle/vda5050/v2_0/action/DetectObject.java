/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.action;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;

/**
 * The VDA5050 {@code detectObject} action.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class DetectObject
    extends Action {

  /**
   * This action's action type.
   */
  public static final String ACTION_TYPE = "detectObject";
  /**
   * The key of the (VDA5050) {@code objectType} parameter for this action.
   */
  public static final String PARAMKEY_OBJECT_TYPE = "objectType";

  public DetectObject(@Nonnull String actionId,
                      @Nonnull BlockingType blockingType,
                      @Nullable String paramValueObjectType) {
    super(ACTION_TYPE, actionId, blockingType);

    List<ActionParameter> actionParameters = new ArrayList<>();
    if (paramValueObjectType != null) {
      actionParameters.add(new ActionParameter(PARAMKEY_OBJECT_TYPE, paramValueObjectType));
    }
    if (!actionParameters.isEmpty()) {
      setActionParameters(actionParameters);
    }
  }
}
