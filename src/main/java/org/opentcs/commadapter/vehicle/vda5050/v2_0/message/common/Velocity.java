/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 * The AGV's velocity in vehicle coordinates.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Velocity
    implements
      Serializable {

  /**
   * [Optional] The AGV's velocity in its x direction.
   */
  private Double vx;
  /**
   * [Optional] The AGV's velocity in its y direction.
   */
  private Double vy;
  /**
   * [Optional] The AGV's turning speed around its z axis.
   */
  private Double omega;

  public Velocity() {
  }

  public Double getVx() {
    return vx;
  }

  public Velocity setVx(Double vx) {
    this.vx = vx;
    return this;
  }

  public Double getVy() {
    return vy;
  }

  public Velocity setVy(Double vy) {
    this.vy = vy;
    return this;
  }

  public Double getOmega() {
    return omega;
  }

  public Velocity setOmega(Double omega) {
    this.omega = omega;
    return this;
  }

  @Override
  public String toString() {
    return "Velocity{" + "vx=" + vx + ", vy=" + vy + ", omega=" + omega + '}';
  }

}
