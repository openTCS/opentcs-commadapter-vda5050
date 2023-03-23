/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.common;

/**
 * Provides helper functions for conversions.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Conversions {

  /**
   * Prevents instantiation.
   */
  private Conversions() {
  }

  /**
   * Returns a convex angle that is equivalent to the given angle and relative to 0 degrees.
   * <p>
   * The result is an angle between -180 and 180 degrees.
   * Angles smaller than -180 degrees or larger than 180 degrees are mapped to the respective
   * equivalent convex angle.
   * For instance, an angle of -270 degrees will be mapped to 90 degrees, and an angle of 181
   * degrees will be mapped to -179 degrees.
   * </p>
   *
   * @param degrees The angle to be mapped
   * @return A convex angle that is equivalent to the given angle and relative to 0 degrees.
   */
  public static double toRelativeConvexAngle(double degrees) {
    double result = degrees % 360.0;
    if (result > 180.0) {
      result -= 360.0;
    }
    else if (result < -180.0) {
      result += 360.0;
    }
    return result;
  }
}
