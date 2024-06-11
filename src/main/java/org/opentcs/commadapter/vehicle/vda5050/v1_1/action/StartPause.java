/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.action;

import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.BlockingType;

/**
 * The VDA5050 {@code startPause} action.
 */
public class StartPause
    extends
      Action {

  /**
   * This action's action type.
   */
  public static final String ACTION_TYPE = "startPause";

  public StartPause(
      @Nonnull
      String actionId,
      @Nonnull
      BlockingType blockingType
  ) {
    super(ACTION_TYPE, actionId, blockingType);
  }
}
