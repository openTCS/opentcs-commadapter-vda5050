/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.common;

/**
 * Mathematical functions dealing with angles.
 */
public class AngleMath {

  private AngleMath() {
  }

  /**
   * Returns the absolute difference between two angles (in the range from 0 to 180 degrees).
   *
   * @param angle1 The first angle, in degrees.
   * @param angle2 The second angle, in degrees.
   * @return The difference.
   */
  public static double angleBetween(double angle1, double angle2) {
    double difference = Math.abs(angle1 - angle2) % 360;
    if (difference > 180) {
      return Math.abs(difference - 360);
    }
    return difference;
  }
}
