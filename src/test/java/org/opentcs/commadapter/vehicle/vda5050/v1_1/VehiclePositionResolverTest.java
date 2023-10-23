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
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_MAP_ID;
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
    point1 = point1.withProperty(PROPKEY_VEHICLE_MAP_ID, "map_1");

    point2 = new Point("point_02");
    point2 = point2.withPosition(new Triple(123, 456, 0));
    point2 = point2.withProperty(PROPKEY_VEHICLE_MAP_ID, "map_2");

    vehicle = new Vehicle("vehicle-0001");
    Map<String, String> vehicleProperties = new HashMap<>();
    vehicleProperties.put(PROPKEY_VEHICLE_DEVIATION_XY, "0.1");
    vehicleProperties.put(PROPKEY_VEHICLE_DEVIATION_THETA, "15");
    vehicleProperties.put(PROPKEY_VEHICLE_MAP_ID, "map_vehicle");
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
  public void alwaysUseLastNodeIdIfVehicleStateContainsIt() {
    // If the vehicle state contains a lastNodeId that is not empty, that node ID should be used.
    state.setLastNodeId(point1.getName());

    assertThat(positionResolver.resolveVehiclePosition(null, state),
               is(point1.getName()));

    // Even if a last known position was passed to the resolver.
    assertThat(positionResolver.resolveVehiclePosition("last known position", state),
               is(point1.getName()));

    // Even if the vehicle is physically located at a different known point.
    state.setAgvPosition(new AgvPosition(
        point2.getPosition().getX() / 1000.0,
        point2.getPosition().getY() / 1000.0,
        0.0,
        point2.getProperty(PROPKEY_VEHICLE_MAP_ID),
        true
    ));

    assertThat(positionResolver.resolveVehiclePosition("last known position", state),
               is(point1.getName()));

    // Even if the vehicle is not physically located at any known point.
    state.setAgvPosition(new AgvPosition(0.0, 0.0, 0.0, "some map id", true));

    assertThat(positionResolver.resolveVehiclePosition("last known position", state),
               is(point1.getName()));

    // Even if the last node ID doesn't exist in the plant model.
    state.setLastNodeId("Nonexistent point");

    assertThat(positionResolver.resolveVehiclePosition("last known position", state),
               is("Nonexistent point"));
  }

  @Test
  public void useLastKnownPositionAsAFallback() {
    // If neither the lastNodeId is set nor a point can be found that matches the AGV position,
    // then the resolver should use the given last known position.
    state.setLastNodeId("");
    state.setAgvPosition(new AgvPosition(0.0, 0.0, 0.0, "some map id", true));

    assertThat(positionResolver.resolveVehiclePosition("last known position", state),
               is("last known position"));

    // Even if the last known position is null
    assertThat(positionResolver.resolveVehiclePosition(null, state),
               is(nullValue()));

    // If the reported AGV position is null, then the resolver should use the given last known
    // position, too.
    state.setAgvPosition(null);
    assertThat(positionResolver.resolveVehiclePosition("last known position", state),
               is("last known position"));

    // Even if the last known position is null
    assertThat(positionResolver.resolveVehiclePosition(null, state),
               is(nullValue()));
  }

  @ParameterizedTest
  @CsvSource({" 0.00,  0.00,   0.0",
              " 0.09,  0.00,   0.0",
              "-0.09, -0.00,   0.0",
              " 0.00,  0.09,   0.0",
              "-0.00, -0.09,   0.0",
              " 0.07,  0.07,   0.0",
              "-0.07, -0.07,   0.0",
              " 0.00,  0.00,  14.9",
              " 0.00,  0.00, -14.9",
              " 0.07,  0.07,  14.9",
              "-0.07, -0.07, -14.9"})
  public void findPointMatchingPhysicalVehiclePosition(double deviationX,
                                                       double deviationY,
                                                       double deviationTheta) {
    // If the state does not contain a lastNodeId, then the resolver should search the plant model
    // and try to find a point that matches the AGV position.
    point1 = point1.withVehicleOrientationAngle(0);
    setupObjectService(objectService);

    state.setLastNodeId("");
    state.setAgvPosition(new AgvPosition(
        point1.getPosition().getX() / 1000.0 + deviationX,
        point1.getPosition().getY() / 1000.0 + deviationY,
        toRadians(deviationTheta),
        point1.getProperty(PROPKEY_VEHICLE_MAP_ID),
        true
    ));

    assertThat(positionResolver.resolveVehiclePosition(null, state),
               is(point1.getName()));

    assertThat(positionResolver.resolveVehiclePosition(point1.getName(), state),
               is(point1.getName()));

    // Even if a last known position was passed to the resolver.
    assertThat(positionResolver.resolveVehiclePosition("last known position", state),
               is(point1.getName()));
  }

  @ParameterizedTest
  @CsvSource({"0.11,0.0", "0.0,0.11", "0.071,0.071"})
  public void shouldNotfindPositionIfOutsideDeviationRangeXY(double deviationX,
                                                             double deviationY) {
    state.setLastNodeId("");
    state.setAgvPosition(new AgvPosition(
        point1.getPosition().getX() / 1000.0 + deviationX,
        point1.getPosition().getY() / 1000.0 + deviationY,
        0.0,
        point1.getProperty(PROPKEY_VEHICLE_MAP_ID),
        true
    ));

    assertThat(positionResolver.resolveVehiclePosition(null, state),
               is(nullValue()));
  }

  @ParameterizedTest
  @ValueSource(doubles = {15.1, -15.1})
  public void shouldNotFindPositionIfOutsideDeviationRangeTheta(double deviationTheta) {
    point1 = point1.withVehicleOrientationAngle(0);
    setupObjectService(objectService);

    state.setLastNodeId("");
    state.setAgvPosition(new AgvPosition(
        point1.getPosition().getX() / 1000.0,
        point1.getPosition().getY() / 1000.0,
        toRadians(deviationTheta),
        point1.getProperty(PROPKEY_VEHICLE_MAP_ID),
        true
    ));

    assertThat(positionResolver.resolveVehiclePosition(null, state),
               is(nullValue()));
  }

  @Test
  public void shouldNotFindPositionIfMapIdIsWrong() {
    state.setLastNodeId("");
    state.setAgvPosition(new AgvPosition(
        point1.getPosition().getX() / 1000.0,
        point1.getPosition().getY() / 1000.0,
        0.0,
        "Wrong map id",
        true
    ));

    assertThat(positionResolver.resolveVehiclePosition(null, state),
               is(nullValue()));
  }
}
