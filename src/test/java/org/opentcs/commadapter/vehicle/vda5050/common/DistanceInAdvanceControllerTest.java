/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.MovementCommand;

class DistanceInAdvanceControllerTest {

  private DistanceInAdvanceController controller;

  @BeforeEach
  public void setUp() {
    controller = new DistanceInAdvanceController(5000L);
  }

  @Test
  public void shouldAcceptCommandsWhileLessThanMaxDistanceInAdvance() {
    assertTrue(
        controller.canAcceptNextCommand(
            List.of(
                createCommandWithLength(2000),
                createCommandWithLength(1000)
            )
        )
    );
  }

  @Test
  public void shouldNotAcceptCommandsWhileGreaterThanMaxDistanceInAdvance() {
    assertFalse(
        controller.canAcceptNextCommand(
            List.of(
                createCommandWithLength(4000),
                createCommandWithLength(2000)
            )
        )
    );
  }

  private MovementCommand createCommandWithLength(long length) {
    Point srcPoint = new Point("1");
    Point destPoint = new Point("2");
    Path path = new Path("1 -- 2", srcPoint.getReference(), destPoint.getReference())
        .withLength(length);

    return new DummyMovementCommand(
        new Route.Step(
            path,
            srcPoint,
            destPoint,
            Vehicle.Orientation.FORWARD,
            0
        )
    );
  }

  private class DummyMovementCommand
      implements
        MovementCommand {

    private final Route.Step step;

    private DummyMovementCommand(Route.Step step) {
      this.step = step;
    }

    @Override
    public Route getRoute() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Route.Step getStep() {
      return step;
    }

    @Override
    public String getOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isWithoutOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Location getOpLocation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isFinalMovement() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Point getFinalDestination() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Location getFinalDestinationLocation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getFinalOperation() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, String> getProperties() {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
}
