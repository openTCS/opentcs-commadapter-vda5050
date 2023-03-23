/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import static org.opentcs.commadapter.vehicle.vda5050.common.Assertions.checkInRange;

/**
 * The trajectory of an AGV described as NURBS.
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Trajectory
    implements Serializable {

  /**
   * Defines the number of control points that influence any given point on the curve.
   * Range: [1 ... infinity]
   * <p>
   * Increasing the degree increases continuity. If not defined, the default value is 1.
   */
  private Double degree;
  /**
   * Sequence of parameter values that determine where and how the control points affect the NURBS
   * curve. Range: [0.0 ... 1.0]
   * <p>
   * Has size of {@code <number of control points> + <degree> + 1}.
   */
  private List<Double> knotVector;
  /**
   * List of {@link ControlPoint} objects defining the control points of the NURBS, which include
   * the start and end point.
   */
  private List<ControlPoint> controlPoints;

  // CHECKSTYLE:OFF (Long lines because some of these parameter declarations are very long.)
  @JsonCreator
  public Trajectory(
      @Nonnull @JsonProperty(required = true, value = "degree") Double degree,
      @Nonnull @JsonProperty(required = true, value = "knotVector") List<Double> knotVector,
      @Nonnull @JsonProperty(required = true, value = "controlPoints") List<ControlPoint> controlPoints) {
    this.degree
        = checkInRange(requireNonNull(degree, "degree"), 1.0, Double.MAX_VALUE, "degree");
    requireNonNull(knotVector, "knotVector");
    knotVector.forEach(value -> {
      requireNonNull(value, "knotVector value");
      checkInRange(value, 0.0, 1.0, "knotVector value");
    });
    this.knotVector = knotVector;
    this.controlPoints = requireNonNull(controlPoints, "controlPoints");
  }
  // CHECKSTYLE:ON

  public Double getDegree() {
    return degree;
  }

  public Trajectory setDegree(@Nonnull Double degree) {
    this.degree
        = checkInRange(requireNonNull(degree, "degree"), 1.0, Double.MAX_VALUE, "degree");
    return this;
  }

  public List<Double> getKnotVector() {
    return knotVector;
  }

  public Trajectory setKnotVector(@Nonnull List<Double> knotVector) {
    requireNonNull(knotVector, "knotVector");
    knotVector.forEach(value -> {
      requireNonNull(value, "knotVector value");
      checkInRange(value, 0.0, 1.0, "knotVector value");
    });
    this.knotVector = knotVector;
    return this;
  }

  public List<ControlPoint> getControlPoints() {
    return controlPoints;
  }

  public Trajectory setControlPoints(@Nonnull List<ControlPoint> controlPoints) {
    this.controlPoints = requireNonNull(controlPoints, "controlPoints");
    return this;
  }

  @Override
  public String toString() {
    return "Trajectory{" + "degree=" + degree
        + ", knotVector=" + knotVector
        + ", controlPoints=" + controlPoints
        + '}';
  }

}
