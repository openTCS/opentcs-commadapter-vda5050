/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.action;

import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;

/**
 * The VDA5050 {@code stateRequest} action.
 */
public class StateRequest
    extends
      Action {

  /**
   * This action's action type.
   */
  public static final String ACTION_TYPE = "stateRequest";

  public StateRequest(
      @Nonnull
      String actionId,
      @Nonnull
      BlockingType blockingType
  ) {
    super(ACTION_TYPE, actionId, blockingType);
  }
}
