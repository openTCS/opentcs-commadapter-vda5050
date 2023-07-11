/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1;

import static java.lang.Math.toRadians;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_THETA;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_DEVIATION_XY;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.AgvPosition;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.BatteryState;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.EStop;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.SafetyState;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.state.State;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;

/**
 * Tests the vehicle position resolver.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class VehiclePositionResolverTest {

  private VehiclePositionResolver positionResolver;

  private Vehicle vehicle;

  private Point point1;

  private Point point2;

  private State state;

  private TCSObjectService objectService;

  @BeforeEach
  public void setup() {
    point1 = new Point("point_01");
    point1 = point1.withPosition(new Triple(200, 100, 0));

    point2 = new Point("point_02");
    point2 = point2.withPosition(new Triple(123, 456, 0));

    vehicle = new Vehicle("vehicle-0001");
    Map<String, String> vehicleProperties = new HashMap<>();
    vehicleProperties.put(PROPKEY_VEHICLE_DEVIATION_XY, "0.1");
    vehicleProperties.put(PROPKEY_VEHICLE_DEVIATION_THETA, "15");
    vehicle = vehicle.withProperties(vehicleProperties);

    objectService = mock(TCSObjectService.class);
    setupObjectService(objectService);
    positionResolver = new VehiclePositionResolver(vehicle.getReference(), objectService);

    state = new State("",
                      0L,
                      "",
                      0L,
                      new ArrayList<>(),
                      new ArrayList<>(),
                      false,
                      false,
                      new ArrayList<>(),
                      new BatteryState(100.0, false),
                      OperatingMode.AUTOMATIC,
                      new ArrayList<>(),
                      new ArrayList<>(),
                      new SafetyState(EStop.AUTOACK, Boolean.FALSE));
  }

  private void setupObjectService(TCSObjectService objectService) {
    when(objectService.fetchObject(Vehicle.class, vehicle.getReference())).thenReturn(vehicle);
    when(objectService.fetchObject(Point.class, point1.getReference())).thenReturn(point1);
    when(objectService.fetchObject(Point.class, point1.getName())).thenReturn(point1);
    when(objectService.fetchObject(Point.class, point2.getReference())).thenReturn(point2);
    when(objectService.fetchObject(Point.class, point2.getName())).thenReturn(point2);
    Set<Point> points = new HashSet<>(Arrays.asList(point1, point2));
    when(objectService.fetchObjects(Point.class)).thenReturn(points);
  }

  @Test
  public void useLastNodeId() {
    state.setLastNodeId(point1.getName());
    String newPosition = positionResolver.resolveVehiclePosition(null, state);

    assertThat(newPosition, is(point1.getName()));
  }

  @Test
  public void useLastNodeIdAlways() {
    state.setLastNodeId(point1.getName());
    String newPosition = positionResolver.resolveVehiclePosition(point2.getName(), state);

    assertThat(newPosition, is(point1.getName()));
  }

  @Test
  public void useAgvPositionToFindPosition() {
    state.setAgvPosition(new AgvPosition(
        point1.getPosition().getX() / 1000.0,
        point1.getPosition().getY() / 1000.0,
        0.0,
        "",
        true
    ));
    String newPosition = positionResolver.resolveVehiclePosition(null, state);

    assertThat(newPosition, is(point1.getName()));
  }

  @Test
  public void useCurrentPositionIfPositionIsStillCorrect() {
    state.setAgvPosition(new AgvPosition(
        point1.getPosition().getX() / 1000.0,
        point1.getPosition().getY() / 1000.0,
        0.0,
        "",
        true
    ));
    String newPosition = positionResolver.resolveVehiclePosition(point1.getName(), state);

    assertThat(newPosition, is(point1.getName()));
  }

  @Test
  public void positionShouldBeNullIfAgvPositionIsNotFound() {
    state.setAgvPosition(new AgvPosition(
        0.0,
        0.0,
        0.0,
        "",
        true
    ));
    String newPosition = positionResolver.resolveVehiclePosition("current position", state);

    assertThat(newPosition, is("current position"));
  }

  @Test
  public void positionShouldBeNullIfLastNodeIdIsNullAndAgvPositionIsNull() {
    String newPosition = positionResolver.resolveVehiclePosition("current position", state);

    assertThat(newPosition, is("current position"));
  }

  @ParameterizedTest
  @ValueSource(doubles = {0, -35, 19.9, 50.1, 360, -360})
  public void shouldNotUsePositionIfOrientationIsWrong(double angle) {
    point1 = point1.withVehicleOrientationAngle(angle);
    setupObjectService(objectService);

    state.setAgvPosition(new AgvPosition(
        point1.getPosition().getX() / 1000.0,
        point1.getPosition().getY() / 1000.0,
        toRadians(35),
        "",
        true
    ));
    String newPosition = positionResolver.resolveVehiclePosition(null, state);

    assertThat(newPosition, is(nullValue()));
  }

  @ParameterizedTest
  @CsvSource({"0.09,0.0", "0.0,0.09", "0.05,0.05"})
  public void deriveLogicalPositionFromAgvPositionIfWithinDeviationXY(double deviationX,
                                                                      double deviationY) {
    state.setAgvPosition(new AgvPosition(
        point1.getPosition().getX() / 1000.0 + deviationX,
        point1.getPosition().getY() / 1000.0 + deviationY,
        0.0,
        "",
        true
    ));
    String newPosition = positionResolver.resolveVehiclePosition(null, state);

    assertThat(newPosition, is(point1.getName()));
  }

  @ParameterizedTest
  @CsvSource({"0.11,0.0", "0.0,0.11", "0.09,0.09"})
  public void dontDeriveLogicalPositionFromAgvPositionIfOutsideDeviationXY(double deviationX,
                                                                           double deviationY) {
    state.setAgvPosition(new AgvPosition(
        point1.getPosition().getX() / 1000.0 + deviationX,
        point1.getPosition().getY() / 1000.0 + deviationY,
        0.0,
        "",
        true
    ));
    String newPosition = positionResolver.resolveVehiclePosition(null, state);

    assertThat(newPosition, is(nullValue()));
  }

  @ParameterizedTest
  @ValueSource(doubles = {35, 20.1, 49.9})
  public void usePositionIfOrientationIsCorrect(double angle) {
    point1 = point1.withVehicleOrientationAngle(angle);
    setupObjectService(objectService);

    state.setAgvPosition(new AgvPosition(
        point1.getPosition().getX() / 1000.0,
        point1.getPosition().getY() / 1000.0,
        toRadians(35),
        "",
        true
    ));
    String newPosition = positionResolver.resolveVehiclePosition(null, state);

    assertThat(newPosition, is(point1.getName()));
  }
}
