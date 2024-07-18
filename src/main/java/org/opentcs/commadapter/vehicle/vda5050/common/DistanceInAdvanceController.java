/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.common;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkInRange;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import java.util.List;
import javax.annotation.Nonnull;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Checks if the comm adapter can accept the next command based on the distance.
 */
public class DistanceInAdvanceController {
  /**
   * The maximum distance (in mm) that may be covered by the movement commands the comm adapter
   * receives in advance.
   */
  private final long maxDistanceInAdvance;

  @Inject
  public DistanceInAdvanceController(
      @Assisted
      long maxDistanceInAdvance
  ) {
    this.maxDistanceInAdvance = checkInRange(maxDistanceInAdvance, 1, Long.MAX_VALUE);
  }

  public boolean canAcceptNextCommand(
      @Nonnull
      List<MovementCommand> queuedCommands
  ) {
    requireNonNull(queuedCommands, "queuedCommands");
    long distanceCovered = queuedCommands.stream()
        .map(MovementCommand::getStep)
        .filter(step -> step.getPath() != null)
        .mapToLong(step -> step.getPath().getLength())
        .sum();

    return distanceCovered < maxDistanceInAdvance;
  }
}
