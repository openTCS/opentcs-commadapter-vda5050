/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.common;

/**
 * The different conditions when a movement command is considered completed.
 */
public enum MovementCommandCompletedCondition {

  /**
   * A movement command is considered completed when its associated edge state has disappeared from
   * the vehicle state.
   */
  EDGE,
  /**
   * A movement command is considered completed when its associated edge state and node state have
   * both disappeared from the vehicle state.
   */
  EDGE_AND_NODE;
}
