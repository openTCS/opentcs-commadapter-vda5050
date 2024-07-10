/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getProperty;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getPropertyInteger;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_VEHICLE_MAX_STEPS_BASE;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_ERRORS_FATAL;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_ERRORS_WARNING;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_INFORMATION_DEBUG;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_INFORMATION_INFO;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_INTERFACE_NAME;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_LENGTH_LOADED;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_LENGTH_UNLOADED;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MANUFACTURER;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_MIN_VISU_INTERVAL;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_PAUSED;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_RECHARGE_OPERATION;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_SERIAL_NUMBER;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.StateMappings.toLoadHandlingDevices;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.StateMappings.toVehicleLength;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.StateMappings.toVehicleState;

import com.google.inject.assistedinject.Assisted;
import java.beans.PropertyChangeEvent;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.commadapter.vehicle.vda5050.CommAdapterConfiguration;
import org.opentcs.commadapter.vehicle.vda5050.CommAdapterConfiguration.ConfigIntegrationLevel;
import org.opentcs.commadapter.vehicle.vda5050.CommAdapterConfiguration.ConfigOperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.common.JsonBinder;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.ConnectionEventListener;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.IncomingMessage;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.MqttClientManager;
import org.opentcs.commadapter.vehicle.vda5050.common.mqtt.QualityOfService;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.controlcenter.ProcessModelImplTO;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.Header;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.AgvPosition;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.common.BlockingType;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection.Connection;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection.ConnectionState;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.instantactions.InstantActions;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.order.Order;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.ErrorLevel;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.InfoLevel;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.OperatingMode;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.visualization.Visualization;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping.ExecutableActionsTagsPredicate;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping.OrderMapper;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping.UnsupportedPropertiesExtractor;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The communication adapter implementation for VDA5050.
 */
public class CommAdapterImpl
    extends
      BasicVehicleCommAdapter
    implements
      ConnectionEventListener {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CommAdapterImpl.class);
  /**
   * Major interface version.
   */
  private static final int VERSION_MAJOR = 2;
  /**
   * Minor interface version.
   */
  private static final int VERSION_MINOR = 0;
  /**
   * Patch version.
   */
  private static final int VERSION_PATCH = 0;
  /**
   * Maps movement commands from openTCS to the telegrams sent to the attached vehicle.
   */
  private OrderMapper orderMapper;
  /**
   * Manages the client's connection to an MQTT broker.
   */
  private final MqttClientManager clientManager;
  /**
   * Matches a state messages with sent order messages to confirm their delivery.
   */
  private final MessageResponseMatcher messageResponseMatcher;
  /**
   * Manages the completion of movement commands.
   */
  private final MovementCommandManager movementCommandManager;
  /**
   * The manager for the vehicle position.
   */
  private VehiclePositionResolver vehiclePositionResolver;
  /**
   * Factory for creating adapter components.
   */
  private final CommAdapterComponentsFactory componentsFactory;
  /**
   * The minimum visualizationInterval.
   */
  private final int minVisualizationInterval;
  /**
   * The vehicle's length when loaded.
   */
  private final int vehicleLengthLoaded;
  /**
   * The vehicle's length when unloaded.
   */
  private final int vehicleLengthUnloaded;

  private final MessageValidator messageValidator;
  /**
   * Binds JSON strings to objects and vice versa.
   */
  private final JsonBinder jsonBinder;
  /**
   * Header id counter for message topics.
   */
  private final Map<String, Long> headerIdCounter = new HashMap<>();
  /**
   * Serial number of the vehicle.
   */
  private final String vehicleSerialNumber;
  /**
   * Manufacturer of the vehicle.
   */
  private final String vehicleManufacturer;
  /**
   * Interface name of the vehicle.
   */
  private final String vehicleInterfaceName;
  /**
   * Timestamp of the last visualization message.
   */
  private long lastVisualizationMessageTimestamp;
  /**
   * Predicate to test if an action is executable by the vehicle.
   */
  private final ExecutableActionsTagsPredicate isActionExecutable;
  /**
   * The comm adapter configuration.
   */
  private final CommAdapterConfiguration configuration;

  /**
   * Creates a new instance.
   *
   * @param vehicle The attached vehicle.
   * @param kernelExecutor The kernel's executor service.
   * @param componentsFactory A factory for our components.
   * @param clientManager The MQTT client manager to use.
   * @param messageValidator Validates messages against JSON schemas.
   * @param jsonBinder Binds JSON strings to objects and vice versa.
   * @param configuration The adapter configuration.
   * @param unsupportedPropertiesExtractor Extracts unsupported optional fields from the vehicle.
   */
  @Inject
  public CommAdapterImpl(
      @Assisted
      Vehicle vehicle,
      @KernelExecutor
      ScheduledExecutorService kernelExecutor,
      CommAdapterComponentsFactory componentsFactory,
      MqttClientManager clientManager,
      MessageValidator messageValidator,
      JsonBinder jsonBinder,
      CommAdapterConfiguration configuration,
      UnsupportedPropertiesExtractor unsupportedPropertiesExtractor
  ) {
    super(
        new ProcessModelImpl(vehicle),
        getPropertyInteger(PROPKEY_VEHICLE_MAX_STEPS_BASE, vehicle).orElse(2) + 1,
        getPropertyInteger(PROPKEY_VEHICLE_MAX_STEPS_BASE, vehicle).orElse(2),
        getProperty(PROPKEY_VEHICLE_RECHARGE_OPERATION, vehicle)
            .orElse(DestinationOperations.CHARGE),
        kernelExecutor
    );
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
    this.minVisualizationInterval
        = getPropertyInteger(PROPKEY_VEHICLE_MIN_VISU_INTERVAL, vehicle).orElse(500);
    this.vehicleLengthLoaded
        = getPropertyInteger(PROPKEY_VEHICLE_LENGTH_LOADED, vehicle).orElse(vehicle.getLength());
    this.vehicleLengthUnloaded
        = getPropertyInteger(PROPKEY_VEHICLE_LENGTH_UNLOADED, vehicle).orElse(vehicle.getLength());
    this.clientManager = requireNonNull(clientManager, "clientManager");
    this.messageValidator = requireNonNull(messageValidator, "messageValidator");
    this.jsonBinder = requireNonNull(jsonBinder, "jsonBinder");
    this.configuration = requireNonNull(configuration, "configuration");
    requireNonNull(unsupportedPropertiesExtractor, "unsupportedPropertiesExtractor");

    movementCommandManager = componentsFactory.createMovementCommandManager(vehicle);
    this.jsonBinder.setFilter(
        componentsFactory.createUnsupportedPropertiesFilter(
            vehicle, unsupportedPropertiesExtractor
        )
    );

    messageResponseMatcher = new MessageResponseMatcher(
        this.getName(),
        this::sendOrder,
        this::sendInstantAction,
        this::orderAccepted,
        this::orderRejected
    );

    vehicleSerialNumber = vehicle.getProperty(PROPKEY_VEHICLE_SERIAL_NUMBER);
    vehicleManufacturer = vehicle.getProperty(PROPKEY_VEHICLE_MANUFACTURER);
    vehicleInterfaceName = vehicle.getProperty(PROPKEY_VEHICLE_INTERFACE_NAME);

    getProcessModel().setTopicPrefix(
        vehicleInterfaceName
            + "/" + "v" + VERSION_MAJOR
            + "/" + vehicleManufacturer
            + "/" + vehicleSerialNumber
    );

    this.isActionExecutable = new ExecutableActionsTagsPredicate(vehicle);
  }

  @Override
  public void initialize() {
    super.initialize();
    orderMapper = componentsFactory.createOrderMapper(
        getProcessModel().getVehicleReference(),
        isActionExecutable
    );
    vehiclePositionResolver = componentsFactory.createVehiclePositionResolver(
        getProcessModel().getVehicleReference()
    );
  }

  @Override
  public void terminate() {
    super.terminate();
  }

  @Override
  public synchronized void enable() {
    if (isEnabled()) {
      return;
    }

    super.enable();

    clientManager.registerConnectionEventListener(this);
    clientManager.subscribe(
        getProcessModel().getTopicPrefix() + "/connection", QualityOfService.AT_LEAST_ONCE, this
    );
    clientManager.subscribe(
        getProcessModel().getTopicPrefix() + "/state", QualityOfService.AT_MOST_ONCE, this
    );
    clientManager.subscribe(
        getProcessModel().getTopicPrefix() + "/visualization", QualityOfService.AT_MOST_ONCE, this
    );
    clientManager.subscribe(
        getProcessModel().getTopicPrefix() + "/factsheet", QualityOfService.AT_LEAST_ONCE, this
    );

    // The client manager may have already been connected to the broker prior to this adapter
    // instance being enabled. Therefore, we have to actively check the broker connection state.
    if (clientManager.isConnected()) {
      onConnect();
    }

    messageResponseMatcher.clear();
    movementCommandManager.clear();
  }

  @Override
  public synchronized void disable() {
    if (!isEnabled()) {
      return;
    }

    clientManager.unsubscribe(getProcessModel().getTopicPrefix() + "/connection", this);
    clientManager.unsubscribe(getProcessModel().getTopicPrefix() + "/state", this);
    clientManager.unsubscribe(getProcessModel().getTopicPrefix() + "/visualization", this);
    clientManager.unsubscribe(getProcessModel().getTopicPrefix() + "/factsheet", this);
    clientManager.unregisterConnectionEventListener(this);

    // With unregistering from the client manager, we will no longer receive any update regarding
    // the broker connection. Therefore, treat a disabled adapter instance as disconnected.
    onDisconnect();

    super.disable();
  }

  @Override
  public synchronized void clearCommandQueue() {
    super.clearCommandQueue();
    movementCommandManager.clear();
    messageResponseMatcher.clear();

    Action cancelOrderAction = new Action(
        "cancelOrder",
        UUID.randomUUID().toString(),
        BlockingType.NONE
    );
    InstantActions instantAction = new InstantActions();
    instantAction.setActions(Arrays.asList(cancelOrderAction));
    messageResponseMatcher.enqueueAction(instantAction);

  }

  @Override
  protected synchronized void connectVehicle() {
  }

  @Override
  protected synchronized void disconnectVehicle() {
  }

  @Override
  protected synchronized boolean isVehicleConnected() {
    return getProcessModel().isBrokerConnected() && getProcessModel().isCommAdapterConnected();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    super.propertyChange(evt);
    if (!(evt.getSource() instanceof ProcessModelImpl)) {
      return;
    }

    // Handling of events from the vehicle gui panels start here
  }

  @Override
  public final ProcessModelImpl getProcessModel() {
    return (ProcessModelImpl) super.getProcessModel();
  }

  @Override
  protected VehicleProcessModelTO createCustomTransferableProcessModel() {
    return new ProcessModelImplTO()
        .setVehicleRef(getProcessModel().getVehicleReference())
        .setCurrentState(getProcessModel().getCurrentState())
        .setPreviousState(getProcessModel().getPreviousState())
        .setLastOrderSent(getProcessModel().getLastOrderSent())
        .setLastInstantActionsSent(getProcessModel().getLastInstantActionsSent())
        .setVehicleIdle(getProcessModel().isVehicleIdle())
        .setBrokerConnected(getProcessModel().isBrokerConnected())
        .setTopicPrefix(getProcessModel().getTopicPrefix())
        .setCurrentConnection(getProcessModel().getCurrentConnection())
        .setCurrentVisualization(getProcessModel().getCurrentVisualization());
  }

  @Override
  public synchronized void sendCommand(MovementCommand cmd)
      throws IllegalArgumentException {
    requireNonNull(cmd, "cmd");

    Order order = orderMapper.toOrder(cmd);

    messageResponseMatcher.enqueueCommand(order, cmd);
  }

  @Override
  public synchronized ExplainedBoolean canProcess(TransportOrder order) {
    requireNonNull(order, "order");

    return canProcessList(
        order.getFutureDriveOrders().stream()
            .map(driveOrder -> driveOrder.getDestination().getOperation())
            .collect(Collectors.toList())
    );
  }

  private synchronized ExplainedBoolean canProcessList(List<String> operations) {
    requireNonNull(operations, "operations");

    boolean canProcess = true;
    String reason = "";
    if (!isEnabled()) {
      canProcess = false;
      reason = "Adapter not enabled";
    }
    if (canProcess && !isVehicleConnected()) {
      canProcess = false;
      reason = "Vehicle does not seem to be connected";
    }

    for (String operation : operations) {
      if (!isStandardOperation(operation)
          && !operation.equals(getRechargeOperation())
          && !isActionExecutable.test(operation)) {
        canProcess = false;
        reason = "Vehicle cannot process operation " + operation + ".";
        break;
      }
    }

    return new ExplainedBoolean(canProcess, reason);
  }

  private boolean isStandardOperation(String operation) {
    return operation.equals(DriveOrder.Destination.OP_NOP)
        || operation.equals(DriveOrder.Destination.OP_MOVE)
        || operation.equals(DriveOrder.Destination.OP_PARK);
  }

  @Override
  public void processMessage(Object message) {
    //Process messages sent from the kernel or a kernel extension
  }

  //ConnectionEventListener
  @Override
  public void onConnect() {
    if (!isEnabled()) {
      return;
    }
    LOG.debug("{}: Connected to broker.", getName());

    getExecutor().execute(() -> getProcessModel().setBrokerConnected(true));
  }

  @Override
  public void onFailedConnectionAttempt() {
    if (!isEnabled()) {
      return;
    }

    getExecutor().execute(() -> {
      getProcessModel().setBrokerConnected(false);
      getProcessModel().setCommAdapterConnected(false);
    });
  }

  @Override
  public void onDisconnect() {
    LOG.debug("{}: Disconnected from broker.", getName());

    getExecutor().execute(() -> {
      getProcessModel().setBrokerConnected(false);
      getProcessModel().setCommAdapterConnected(false);
      getProcessModel().setVehicleIdle(true);
      getProcessModel().setVehicleState(Vehicle.State.UNKNOWN);
    });
  }

  @Override
  public void onIdle() {
    LOG.debug("{}: Idle", getName());

    getExecutor().execute(() -> getProcessModel().setVehicleIdle(true));
  }

  @Override
  public synchronized void onIncomingMessage(IncomingMessage message) {
    requireNonNull(message, "message");

    if (message.getTopic().endsWith("/connection")) {
      try {
        messageValidator.validate(message.getMessage(), Connection.class);
        Connection connectionMessage = jsonBinder.fromJson(message.getMessage(), Connection.class);
        getExecutor().execute(() -> onConnectionMessage(connectionMessage));
      }
      catch (IllegalArgumentException ex) {
        LOG.warn("Cannot parse connection message: {}", message.getMessage(), ex);
      }
    }
    else if (message.getTopic().endsWith("/state")) {
      try {
        messageValidator.validate(message.getMessage(), State.class);
        State stateMessage = jsonBinder.fromJson(message.getMessage(), State.class);
        getExecutor().execute(() -> onStateMessage(stateMessage));
      }
      catch (IllegalArgumentException ex) {
        LOG.warn("Cannot parse state message: {}", message.getMessage(), ex);
      }
    }
    else if (message.getTopic().endsWith("/visualization")) {
      try {
        messageValidator.validate(message.getMessage(), Visualization.class);
        Visualization vis = jsonBinder.fromJson(message.getMessage(), Visualization.class);
        getExecutor().execute(() -> onVisualizationMessage(vis));
      }
      catch (IllegalArgumentException ex) {
        LOG.warn("Cannot parse visualization message: {}", message.getMessage(), ex);
      }
    }
    else if (message.getTopic().endsWith("/factsheet")) {
      LOG.info("Received factsheet from vehicle, ignoring it.");
    }
    else {
      LOG.warn(
          "Incoming message on unhandled topic '{}': {}",
          message.getTopic(),
          message.getMessage()
      );
    }
  }

  @Override
  public void onVehiclePaused(boolean paused) {
    Action pauseAction = new Action(
        paused ? "startPause" : "stopPause",
        UUID.randomUUID().toString(),
        BlockingType.NONE
    );
    InstantActions instantAction = new InstantActions();
    instantAction.setActions(Arrays.asList(pauseAction));
    messageResponseMatcher.enqueueAction(instantAction);
  }

  private void onVisualizationMessage(Visualization vis) {
    getProcessModel().setVehicleIdle(false);

    long now = System.currentTimeMillis();
    if (now - lastVisualizationMessageTimestamp < minVisualizationInterval) {
      LOG.trace(
          "Visualization message discarded - last one was {} ms ago.",
          now - lastVisualizationMessageTimestamp
      );
      return;
    }
    lastVisualizationMessageTimestamp = now;

    if (vis.getAgvPosition() != null) {
      processVehiclePosition(vis.getAgvPosition());
    }
    getProcessModel().setCurrentVisualization(vis);
  }

  private void onConnectionMessage(Connection message) {
    LOG.debug("{}: Received a new connection message: {}", getName(), message);
    getProcessModel().setVehicleIdle(false);

    if (message.getConnectionState() == ConnectionState.OFFLINE
        || message.getConnectionState() == ConnectionState.CONNECTIONBROKEN) {
      getProcessModel().setCommAdapterConnected(false);
      getProcessModel().setVehicleState(Vehicle.State.UNKNOWN);
    }
    else if (message.getConnectionState() == ConnectionState.ONLINE) {
      getProcessModel().setCommAdapterConnected(true);
      getProcessModel().setVehicleState(Vehicle.State.IDLE);
    }
    getProcessModel().setCurrentConnection(message);
  }

  private void onStateMessage(State state) {
    LOG.debug("{}: Received a new state message: {}", getName(), state);

    getProcessModel().setVehicleIdle(false);

    messageResponseMatcher.onStateMessage(state);

    // Update the vehicle's current state and remember the old one.
    getProcessModel().setPreviousState(getProcessModel().getCurrentState());
    getProcessModel().setCurrentState(state);

    String newVehiclePosition = vehiclePositionResolver.resolveVehiclePosition(
        getProcessModel().getVehiclePosition(), getProcessModel().getCurrentState()
    );
    if (!Objects.equals(newVehiclePosition, getProcessModel().getVehiclePosition())) {
      LOG.debug("{}: Vehicle is now at point {}", getName(), newVehiclePosition);
      getProcessModel().setVehiclePosition(newVehiclePosition);
    }

    if (state.getAgvPosition() != null) {
      processVehiclePosition(state.getAgvPosition());
    }

    getProcessModel().setVehicleLoadHandlingDevices(toLoadHandlingDevices(state));
    getProcessModel().setVehicleEnergyLevel(state.getBatteryState().getBatteryCharge().intValue());
    getProcessModel().setVehicleProperty(
        PROPKEY_VEHICLE_ERRORS_FATAL,
        StateMappings.toErrorPropertyValue(state, ErrorLevel.FATAL)
    );
    getProcessModel().setVehicleProperty(
        PROPKEY_VEHICLE_ERRORS_WARNING,
        StateMappings.toErrorPropertyValue(state, ErrorLevel.WARNING)
    );
    getProcessModel().setVehicleProperty(
        PROPKEY_VEHICLE_INFORMATION_INFO,
        StateMappings.toInfoPropertyValue(state, InfoLevel.INFO)
    );
    getProcessModel().setVehicleProperty(
        PROPKEY_VEHICLE_INFORMATION_DEBUG,
        StateMappings.toInfoPropertyValue(state, InfoLevel.DEBUG)
    );
    getProcessModel().setVehicleProperty(
        PROPKEY_VEHICLE_PAUSED,
        StateMappings.toPausedPropertyValue(state)
    );
    getProcessModel().setVehicleState(toVehicleState(state));
    getProcessModel().setVehicleLength(
        toVehicleLength(state, vehicleLengthUnloaded, vehicleLengthLoaded)
    );

    processVehicleOperatingMode(state);

    movementCommandManager.onStateMessage(state, this::onMovementCommandExecuted);
  }

  private void onMovementCommandExecuted(
      @Nonnull
      MovementCommand finishedCommand
  ) {
    requireNonNull(finishedCommand, "finishedCommand");

    MovementCommand oldestCommand = getSentQueue().peek();
    if (Objects.equals(finishedCommand, oldestCommand)) {
      getSentQueue().poll();
      getProcessModel().commandExecuted(oldestCommand);
    }
    else {
      LOG.warn("Not oldest movement command: {} != {}", finishedCommand, oldestCommand);
    }
  }

  private void processVehicleOperatingMode(State state) {
    if (getProcessModel().getPreviousState().getOperatingMode() == state.getOperatingMode()) {
      return;
    }

    if (configuration.onOpModeChangeDoWithdrawOrder()
        .getOrDefault(mapToConfigOperatingMode(state.getOperatingMode()), Boolean.FALSE)) {
      getProcessModel().transportOrderWithdrawalRequested(true);
    }

    if (configuration.onOpModeChangeDoResetPosition()
        .getOrDefault(mapToConfigOperatingMode(state.getOperatingMode()), Boolean.FALSE)) {
      LOG.debug(
          "{}: Resetting last known vehicle position due to op mode change to {}...",
          getName(),
          state.getOperatingMode()
      );
      getProcessModel().setVehiclePosition(null);
    }

    configuration.onOpModeChangeDoUpdateIntegrationLevel()
        .getOrDefault(
            mapToConfigOperatingMode(state.getOperatingMode()),
            ConfigIntegrationLevel.LEAVE_UNCHANGED
        )
        .toIntegrationLevel()
        .ifPresent((integrationLevel) -> {
          getExecutor().execute(() -> {
            getProcessModel().integrationLevelChangeRequested(integrationLevel);
          });
        });
  }

  private ConfigOperatingMode mapToConfigOperatingMode(OperatingMode opMode) {
    switch (opMode) {
      case AUTOMATIC:
        return ConfigOperatingMode.AUTOMATIC;
      case SEMIAUTOMATIC:
        return ConfigOperatingMode.SEMIAUTOMATIC;
      case MANUAL:
        return ConfigOperatingMode.MANUAL;
      case SERVICE:
        return ConfigOperatingMode.SERVICE;
      case TEACHIN:
        return ConfigOperatingMode.TEACHIN;
      default:
        throw new IllegalArgumentException("Unmapped operating mode " + opMode.name());
    }
  }

  private void processVehiclePosition(AgvPosition position) {
    getProcessModel().setVehiclePrecisePosition(
        new Triple(
            (long) (position.getX() * 1000.0),
            (long) (position.getY() * 1000.0),
            0
        )
    );
    getProcessModel().setVehicleOrientationAngle(Math.toDegrees(position.getTheta()));
  }

  /**
   * Sends an order to the vehicle.
   *
   * @param order the order to send.
   */
  public void sendOrder(Order order) {
    sendMessage(order, "order");
    getProcessModel().setLastOrderSent(order);
  }

  /**
   * Sends an instant action to the vehicle.
   *
   * @param instantActions the action to send.
   */
  public void sendInstantAction(InstantActions instantActions) {
    sendMessage(instantActions, "instantActions");
    getProcessModel().setLastInstantActionsSent(instantActions);
  }

  private void sendMessage(Header messageObject, String topic) {
    // increment header id for this topic
    long headerId = headerIdCounter.getOrDefault(topic, 0L);
    headerIdCounter.put(topic, headerId + 1);

    // set header for message object
    messageObject.setHeaderId(headerId);
    messageObject.setTimestamp(Instant.now());
    messageObject.setVersion(VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_PATCH);
    messageObject.setManufacturer(vehicleManufacturer);
    messageObject.setSerialNumber(vehicleSerialNumber);
    try {
      String message = jsonBinder.toJson(messageObject);
      messageValidator.validate(message, messageObject.getClass());
      LOG.debug("{}: Sending message to '{}': {}", getName(), topic, message);
      clientManager.publish(
          getProcessModel().getTopicPrefix() + "/" + topic,
          QualityOfService.AT_MOST_ONCE,
          message,
          false
      );
    }
    catch (IllegalArgumentException exc) {
      LOG.error("{}: Failed to convert to JSON {}", getName(), messageObject, exc);
    }
  }

  private void orderAccepted(OrderAssociation order) {
    movementCommandManager.enqueue(order);
  }

  private void orderRejected(OrderAssociation order) {
    // Even though the vehicle rejected the order, we still enqueue the order association with the
    // movement command manager here, anyway. We do this to enable the movement command manager
    // to fail the latest movement command / trigger withdrawal of the current transport order for
    // rejected orders, too.
    movementCommandManager.enqueue(order);

    getProcessModel().publishUserNotification(
        new UserNotification(
            getProcessModel().getName(),
            String.format(
                "Vehicle rejected VDA5050 order (ID: %s, update ID: %s)",
                order.getOrder().getOrderId(),
                order.getOrder().getOrderUpdateId()
            ),
            UserNotification.Level.IMPORTANT
        )
    );
  }
}
