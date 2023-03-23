/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.opentcs.commadapter.vehicle.vda5050.common.Conversions.toRelativeConvexAngle;

/**
 * Conversion tests.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ConversionsTest {

  @Test
  public void keepConvexAnglesAsTheyAre() {
    assertThat(toRelativeConvexAngle(0.0), is(0.0));
    assertThat(toRelativeConvexAngle(179.9), is(179.9));
    assertThat(toRelativeConvexAngle(-179.9), is(-179.9));
  }

  @Test
  public void mapLargePositiveAnglesToConvexAngles() {
    assertThat(toRelativeConvexAngle(360.0), is(0.0));
    assertThat(toRelativeConvexAngle(270.0), is(-90.0));
  }

  @Test
  public void mapLargeNegativeAnglesToConvexAngles() {
    assertThat(toRelativeConvexAngle(-360.0), is(-0.0));
    assertThat(toRelativeConvexAngle(-270.0), is(90.0));
  }
}
