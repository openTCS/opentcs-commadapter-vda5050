/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.opentcs.commadapter.vehicle.vda5050.common.Assertions.checkInRange;

/**
 */
public class AssertionsTest {

  @Test
  public void valueInsideRange() {
    assertThat(checkInRange(0.5, 0.0, 1.0), closeTo(0.5, 0.001));
  }

  @Test
  public void valueOutsideRange() {
    assertThrows(IllegalArgumentException.class, () -> checkInRange(5.0, 0.0, 1.0));
  }

  @Test
  public void shouldPassForBorderCases() {
    assertThat(checkInRange(0.0, 0.0, 1.0), closeTo(0.0, 0.001));
    assertThat(checkInRange(1.0, 0.0, 1.0), closeTo(1.0, 0.001));
  }

}
