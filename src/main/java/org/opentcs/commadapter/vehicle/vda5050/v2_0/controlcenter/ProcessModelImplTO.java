/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter;

import org.opentcs.commadapter.vehicle.vda5050.v2_0.ProcessModelImpl;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection.Connection;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.visualization.Visualization;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;

/**
 * A serializable representation of a {@link ProcessModelImpl}.
 * This TO can be sent to other applications responsible for displaying the state of the vehicle,
 * like the control center or the plant overview.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class ProcessModelImplTO
    extends VehicleProcessModelTO {

  /**
   * The vehicle reference.
   */
  private TCSObjectReference<Vehicle> vehicleRef;
  /**
   * The current/most recent state reported by the vehicle.
   */
  private State currentState;
  /**
   * The previous state reported by the vehicle.
   */
  private State previousState;
  /**
   * The last order sent to the vehicle.
   */
  private Order lastOrderSent;
  /**
   * The last instant actions sent to the vehicle.
   */
  private InstantActions lastInstantActionsSent;
  /**
   * The time to wait between periodic state request telegrams.
   */
  private int stateRequestInterval;
  /**
   * How long (in ms) we tolerate not hearing from the vehicle before we consider communication
   * dead.
   */
  private int vehicleIdleTimeout;
  /**
   * Indicates whether the vehicle has not been heard of recently.
   */
  private boolean vehicleIdle;
  /**
   * Whether to close the connection if the vehicle is considered dead.
   */
  private boolean disconnectingOnVehicleIdle;
  /**
   * Whether to reconnect automatically when the vehicle connection times out.
   */
  private boolean reconnectingOnConnectionLoss;
  /**
   * The delay before reconnecting (in ms).
   */
  private int reconnectDelay;
  /**
   * Whether logging should be enabled or not.
   */
  private boolean loggingEnabled;
  /**
   * Whether the comm adapter is connected to the mqtt broker.
   */
  private boolean brokerConnected;
  /**
   * The prefix used for MQTT topic names.
   */
  private String topicPrefix;
  /**
   * The current/most recent reported connection message.
   */
  private Connection currentConnection;
  /**
   * The current/most recent reported visualization message.
   */
  private Visualization currentVisualisation;

  /**
   * Creates a new instance.
   */
  public ProcessModelImplTO() {
  }

  /**
   * Returns the vehicle reference.
   *
   * @return The vehicle reference
   */
  public TCSObjectReference<Vehicle> getVehicleRef() {
    return vehicleRef;
  }

  /**
   * Sets the vehicle reference.
   *
   * @param vehicleRef The vehicle reference
   * @return This
   */
  public ProcessModelImplTO setVehicleRef(TCSObjectReference<Vehicle> vehicleRef) {
    this.vehicleRef = vehicleRef;
    return this;
  }

  /**
   * Returns the current/most recent state reported by the vehicle.
   *
   * @return The current/most recent state reported by the vehicle
   */
  public State getCurrentState() {
    return currentState;
  }

  /**
   * Sets the current/most recent state reported by the vehicle.
   *
   * @param currentState The current/most recent state reported by the vehicle.
   * @return This
   */
  public ProcessModelImplTO setCurrentState(
      State currentState) {
    this.currentState = currentState;
    return this;
  }

  /**
   * Returns the previous state reported by the vehicle.
   *
   * @return The previous state reported by the vehicle
   */
  public State getPreviousState() {
    return previousState;
  }

  /**
   * Sets the previous state reported by the vehicle.
   *
   * @param previousState The previous state reported by the vehicle
   * @return This
   */
  public ProcessModelImplTO setPreviousState(
      State previousState) {
    this.previousState = previousState;
    return this;
  }

  /**
   * Returns the last order sent to the vehicle.
   *
   * @return The last order sent to the vehicle
   */
  public Order getLastOrderSent() {
    return lastOrderSent;
  }

  /**
   * Sets the last order sent to the vehicle.
   *
   * @param lastOrderSent The last order sent to the vehicle
   * @return This
   */
  public ProcessModelImplTO setLastOrderSent(Order lastOrderSent) {
    this.lastOrderSent = lastOrderSent;
    return this;
  }

  /**
   * Returns the last instant actions sent to the vehicle.
   *
   * @return The last instant actions sent to the vehicle
   */
  public InstantActions getLastInstantActionsSent() {
    return lastInstantActionsSent;
  }

  /**
   * Sets the last instant actions sent to the vehicle.
   *
   * @param lastInstantActionsSent The last instant actions sent to the vehicle
   * @return This
   */
  public ProcessModelImplTO setLastInstantActionsSent(InstantActions lastInstantActionsSent) {
    this.lastInstantActionsSent = lastInstantActionsSent;
    return this;
  }

  /**
   * Returns the time to wait between periodic state request telegrams.
   *
   * @return The time to wait between periodic state request telegrams
   */
  public int getStateRequestInterval() {
    return stateRequestInterval;
  }

  /**
   * Sets the time to wait between periodic state request telegrams.
   *
   * @param stateRequestInterval The time to wait between periodic state request telegrams
   * @return This
   */
  public ProcessModelImplTO setStateRequestInterval(int stateRequestInterval) {
    this.stateRequestInterval = stateRequestInterval;
    return this;
  }

  /**
   * Returns how long (in ms) we tolerate not hearing from the vehicle before we consider
   * communication dead.
   *
   * @return How long (in ms) we tolerate not hearing from the vehicle before we consider
   * communication dead
   */
  public int getVehicleIdleTimeout() {
    return vehicleIdleTimeout;
  }

  /**
   * Sets how long (in ms) we tolerate not hearing from the vehicle before we consider communication
   * dead.
   *
   * @param vehicleIdleTimeout How long (in ms) we tolerate not hearing from the vehicle before
   * we consider communication dead
   * @return This
   */
  public ProcessModelImplTO setVehicleIdleTimeout(int vehicleIdleTimeout) {
    this.vehicleIdleTimeout = vehicleIdleTimeout;
    return this;
  }

  /**
   * Returns whether the vehicle has not been heard of recently.
   *
   * @return Whether the vehicle has not been heard of recently
   */
  public boolean isVehicleIdle() {
    return vehicleIdle;
  }

  /**
   * Sets whether the vehicle has not been heard of recently.
   *
   * @param vehicleIdle Whether the vehicle has not been heard of recently
   * @return This
   */
  public ProcessModelImplTO setVehicleIdle(boolean vehicleIdle) {
    this.vehicleIdle = vehicleIdle;
    return this;
  }

  /**
   * Returns whether to close the connection if the vehicle is considered dead.
   *
   * @return Whether to close the connection if the vehicle is considered dead
   */
  public boolean isDisconnectingOnVehicleIdle() {
    return disconnectingOnVehicleIdle;
  }

  /**
   * Sets whether to close the connection if the vehicle is considered dead.
   *
   * @param disconnectingOnVehicleIdle Whether to close the connection if the vehicle is
   * considered dead
   * @return This
   */
  public ProcessModelImplTO setDisconnectingOnVehicleIdle(boolean disconnectingOnVehicleIdle) {
    this.disconnectingOnVehicleIdle = disconnectingOnVehicleIdle;
    return this;
  }

  /**
   * Returns whether to reconnect automatically when the vehicle connection times out.
   *
   * @return Whether to reconnect automatically when the vehicle connection times out
   */
  public boolean isReconnectingOnConnectionLoss() {
    return reconnectingOnConnectionLoss;
  }

  /**
   * Sets whether to reconnect automatically when the vehicle connection times out.
   *
   * @param reconnectingOnConnectionLoss Whether to reconnect automatically when the vehicle
   * connection times out
   * @return This
   */
  public ProcessModelImplTO setReconnectingOnConnectionLoss(boolean reconnectingOnConnectionLoss) {
    this.reconnectingOnConnectionLoss = reconnectingOnConnectionLoss;
    return this;
  }

  /**
   * Returns the delay before reconnecting (in ms).
   *
   * @return The delay before reconnecting (in ms)
   */
  public int getReconnectDelay() {
    return reconnectDelay;
  }

  /**
   * Sets the delay before reconnecting (in ms).
   *
   * @param reconnectDelay The delay before reconnecting (in ms)
   * @return This
   */
  public ProcessModelImplTO setReconnectDelay(int reconnectDelay) {
    this.reconnectDelay = reconnectDelay;
    return this;
  }

  /**
   * Returns whether logging should be enabled or not.
   *
   * @return Whether logging should be enabled or not
   */
  public boolean isLoggingEnabled() {
    return loggingEnabled;
  }

  /**
   * Sets whether logging should be enabled or not.
   *
   * @param loggingEnabled Whether logging should be enabled or not
   * @return This
   */
  public ProcessModelImplTO setLoggingEnabled(boolean loggingEnabled) {
    this.loggingEnabled = loggingEnabled;
    return this;
  }

  /**
   * Returns whether the broker is connected or not.
   *
   * @return Whether the broker is connected or not
   */
  public boolean isBrokerConnected() {
    return brokerConnected;
  }

  /**
   * Sets whether the broker is connected or not.
   *
   * @param brokerConnected Whether the broker is connected or not
   * @return This
   */
  public ProcessModelImplTO setBrokerConnected(boolean brokerConnected) {
    this.brokerConnected = brokerConnected;
    return this;
  }

  /**
   * Returns the prefix used for MQTT topic names.
   *
   * @return The prefix used for MQTT topic names.
   */
  public String getTopicPrefix() {
    return topicPrefix;
  }

  /**
   * Sets the prefix used for MQTT topic names.
   *
   * @param topicPrefix The prefix used for MQTT topic names.
   * @return This
   */
  public ProcessModelImplTO setTopicPrefix(String topicPrefix) {
    this.topicPrefix = topicPrefix;
    return this;
  }

  /**
   * Return the current connection message.
   *
   * @return the current connection message.
   */
  public Connection getCurrentConnection() {
    return currentConnection;
  }

  /**
   * Sets the current connection message.
   *
   * @param connection the current connection message.
   * @return This
   */
  public ProcessModelImplTO setCurrentConnection(Connection connection) {
    currentConnection = connection;
    return this;
  }

  /**
   * Return the current visualization message.
   *
   * @return the current visualization message.
   */
  public Visualization getCurrentVisualization() {
    return currentVisualisation;
  }

  /**
   * Sets the current visualization message.
   *
   * @param visualization the current visualization message.
   * @return This
   */
  public ProcessModelImplTO setCurrentVisualization(Visualization visualization) {
    currentVisualisation = visualization;
    return this;
  }

}
