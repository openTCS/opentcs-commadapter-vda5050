/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.action;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.ActionParameter;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.BlockingType;

/**
 * The VDA5050 {@code logReport} action.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LogReport
    extends Action {

  /**
   * This action's action type.
   */
  public static final String ACTION_TYPE = "logReport";
  /**
   * The key of the (VDA5050) {@code reason} parameter for this action.
   */
  public static final String PARAMKEY_REASON = "reason";

  public LogReport(@Nonnull String actionId,
                   @Nonnull BlockingType blockingType,
                   @Nonnull String paramValueReason) {
    super(ACTION_TYPE, actionId, blockingType);

    List<ActionParameter> actionParameters = new ArrayList<>();
    requireNonNull(paramValueReason, "paramValueReason");
    actionParameters.add(new ActionParameter(PARAMKEY_REASON, paramValueReason));
    setActionParameters(actionParameters);
  }
}
