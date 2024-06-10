/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping;

import com.google.inject.assistedinject.Assisted;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import static org.opentcs.commadapter.vehicle.vda5050.common.PropertyExtractions.getPropertyInteger;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.common.Action;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Edge;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Node;
import org.opentcs.commadapter.vehicle.vda5050.v1_1.message.order.Order;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;
import static org.opentcs.util.Assertions.checkArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps {@link MovementCommand}s from openTCS to an {@link Order} message understood by the vehicle.
 */
public class OrderMapper {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OrderMapper.class);

  /**
   * A reference to the attached vehicle.
   */
  private final TCSObjectReference<Vehicle> vehicleReference;
  /**
   * An object service.
   */
  private final TCSObjectService objectService;
  /**
   * Predicate to test if an action is executable by the vehicle.
   */
  private final Predicate<String> vehicleActionsFilter;
  /**
   * The last order that was mapped.
   */
  private Order lastMappedOrder;

  /**
   * Creates a new instance.
   *
   * @param vehicleReference A reference to the attached vehicle.
   * @param isActionExecutable A predicate to test if an action is executable.
   * @param objectService An object service.
   */
  @Inject
  public OrderMapper(@Assisted @Nonnull TCSObjectReference<Vehicle> vehicleReference,
                     @Assisted @Nonnull Predicate<String> isActionExecutable,
                     @Nonnull TCSObjectService objectService) {
    this.vehicleReference = requireNonNull(vehicleReference, "vehicleReference");
    this.vehicleActionsFilter = requireNonNull(isActionExecutable, "isActionExecutable");
    this.objectService = requireNonNull(objectService, "objectService");
  }

  /**
   * Maps the given command to an Order mapperObject.
   *
   * @param command The command to convert.
   * @return The order mapper object.
   */
  public Order toOrder(@Nonnull MovementCommand command) {
    requireNonNull(command, "command");

    return mapOrder(command, objectService.fetchObject(Vehicle.class, vehicleReference));
  }

  /**
   * Map a movement command for a vehicle to a order.
   *
   * @param command The command to map.
   * @param vehicle The vehicle to map the command for.
   * @return The mapped order.
   */
  private Order mapOrder(@Nonnull MovementCommand command, @Nonnull Vehicle vehicle) {
    requireNonNull(command, "command");
    requireNonNull(vehicle, "vehicle");

    lastMappedOrder = involvesActualMovement(command)
        ? createOrderWithMovement(command, vehicle)
        : createOrderWithoutMovement(command, vehicle);

    return lastMappedOrder;
  }

  private Order createOrderWithMovement(MovementCommand command, Vehicle vehicle) {
    Order order = createEmptyOrder(command, vehicle);

    // Create an order consisting of a source node, an edge and a destination node.
    order.getNodes().add(
        getOrCreateSourceNodeForMovement(order, command, vehicle)
    );

    order.getEdges().add(mapEdge(command, vehicle));

    order.getNodes().add(mapDestNode(
        command,
        command.getStep().getRouteIndex() * 2 + 2,
        vehicle
    ));

    // Add rest of the route as the horizon.
    mapHorizon(order, command, vehicle);

    return order;
  }

  private Order createOrderWithoutMovement(MovementCommand command, Vehicle vehicle) {
    Order order = createEmptyOrder(command, vehicle);

    // This is a movement consisting only of a destination node.
    order.getNodes().add(mapDestNode(
        command,
        0,
        vehicle
    ));

    return order;
  }

  private Order createEmptyOrder(MovementCommand command, Vehicle vehicle) {
    return new Order(getDriveOrderName(vehicle),
                     (long) command.getStep().getRouteIndex(),
                     new ArrayList<>(),
                     new ArrayList<>());
  }

  private Node getOrCreateSourceNodeForMovement(Order order,
                                                MovementCommand command,
                                                Vehicle vehicle) {
    if (isNewOrder(order)) {
      return mapInitialNodeOnRoute(command, vehicle);
    }
    else {
      // Use the destination node of the previous order message as the new source node.
      return lastMappedOrder.getNodes().get(1);
    }
  }

  private Node mapInitialNodeOnRoute(MovementCommand command, Vehicle vehicle) {
    PropertyActionsFilter actionFilter
        = new PropertyActionsFilter(vehicleActionsFilter,
                                    new ExecutableActionsTagsPredicate(command),
                                    actionString -> true,
                                    EnumSet.of(ActionTrigger.ORDER_START));

    return NodeMapping.toBaseNode(
        command.getStep().getSourcePoint(),
        0,
        vehicle,
        ActionsMapping.mapPropertyActions(command.getStep().getSourcePoint()).stream()
            .filter(actionFilter)
            .map(propertyAction -> ActionsMapping.fromPropertyAction(vehicle, propertyAction))
            .collect(Collectors.toList()),
        true
    );
  }

  private Node mapDestNode(@Nonnull MovementCommand command,
                           long sequenceId,
                           @Nonnull Vehicle vehicle) {
    return NodeMapping.toBaseNode(
        command.getStep().getDestinationPoint(),
        sequenceId,
        vehicle,
        actionsForVehicle(command, vehicle),
        false
    );
  }

  private List<Action> actionsForVehicle(@Nonnull MovementCommand command,
                                         @Nonnull Vehicle vehicle) {
    Predicate<PropertyAction> propActionFilter = createPropertyActionsFilter(command);

    return concatStreams(
        ActionsMapping.mapPropertyActions(command.getStep().getDestinationPoint()).stream()
            .filter(propActionFilter),
        Optional.ofNullable(command.getOpLocation())
            .stream()
            .flatMap(opLocation -> ActionsMapping.mapPropertyActions(opLocation).stream())
            .filter(propActionFilter),
        movementCommandPropAction(command, vehicle).stream(),
        ActionsMapping.mapPropertyActions(command).stream()
    )
        .map(propertyAction -> ActionsMapping.fromPropertyAction(vehicle, propertyAction))
        .collect(Collectors.toList());
  }

  private Predicate<PropertyAction> createPropertyActionsFilter(@Nonnull MovementCommand cmd) {
    return new PropertyActionsFilter(
        vehicleActionsFilter,
        new ExecutableActionsTagsPredicate(cmd),
        destNodeEdgeActionFilter(cmd),
        isLastStep(cmd) ? EnumSet.of(ActionTrigger.ORDER_END) : EnumSet.of(ActionTrigger.PASSING)
    );
  }

  private Predicate<String> destNodeEdgeActionFilter(@Nonnull MovementCommand cmd) {
    return involvesActualMovement(cmd)
        ? new ExecutableActionsTagsPredicate(cmd.getStep().getPath())
        : actionString -> true;
  }

  private Optional<PropertyAction> movementCommandPropAction(@Nonnull MovementCommand command,
                                                             @Nonnull Vehicle vehicle) {
    if (command.isWithoutOperation() || command.getOpLocation() == null) {
      return Optional.empty();
    }

    return ActionsMapping.fromMovementCommand(
        vehicle,
        command,
        command.getOpLocation(),
        objectService.fetchObject(LocationType.class, command.getOpLocation().getType())
    );
  }

  private Edge mapEdge(MovementCommand command,
                       Vehicle vehicle) {
    PropertyActionsFilter actionFilter = new PropertyActionsFilter(
        vehicleActionsFilter,
        new ExecutableActionsTagsPredicate(command),
        actionString -> true,
        EnumSet.allOf(ActionTrigger.class)
    );

    return EdgeMapping.toBaseEdge(
        command.getStep(),
        vehicle,
        ActionsMapping.mapPropertyActions(command.getStep().getPath()).stream()
            .filter(actionFilter)
            .map(propertyAction -> ActionsMapping.fromPropertyAction(vehicle, propertyAction))
            .collect(Collectors.toList())
    );
  }

  /**
   * Finds the current transport order and generates the order ID from it.
   *
   * @param vehicle The vehicle that is assigned to this movement.
   * @return The order ID.
   */
  private String getDriveOrderName(Vehicle vehicle) {
    checkArgument(vehicle.getTransportOrder() != null, "Vehicle does not have a transport order");

    TransportOrder transportOrder
        = objectService.fetchObject(TransportOrder.class, vehicle.getTransportOrder());

    return transportOrder.getName() + "-" + transportOrder.getCurrentDriveOrderIndex();
  }

  private boolean involvesActualMovement(MovementCommand command) {
    return command.getStep().getSourcePoint() != null && command.getStep().getPath() != null;
  }

  private boolean isLastStep(MovementCommand command) {
    return command.isFinalMovement();
  }

  private boolean isNewOrder(Order order) {
    if (lastMappedOrder == null) {
      return true;
    }
    return !order.getOrderId().equals(lastMappedOrder.getOrderId());
  }

  @SafeVarargs
  private <T> Stream<T> concatStreams(Stream<T>... streams) {
    Stream<T> result = Stream.empty();

    for (Stream<T> stream : streams) {
      result = Stream.concat(result, stream);
    }

    return result;
  }

  private void mapHorizon(Order order, MovementCommand command, Vehicle vehicle) {
    int maxRouteIndex = Math.min(
        command.getStep().getRouteIndex()
        + getPropertyInteger(ObjectProperties.PROPKEY_VEHICLE_MAX_STEPS_HORIZON, vehicle)
            .orElse(command.getRoute().getSteps().size()),
        command.getRoute().getSteps().size()
    );

    for (int i = command.getStep().getRouteIndex() + 1; i < maxRouteIndex; i++) {
      Step step = command.getRoute().getSteps().get(i);

      order.getEdges().add(mapHorizonEdge(command, step, vehicle));

      order.getNodes().add(mapHorizonNode(
          command,
          step,
          step.getRouteIndex() * 2 + 2,
          vehicle
      ));
    }
  }

  private Edge mapHorizonEdge(MovementCommand command,
                              Step step,
                              Vehicle vehicle) {
    PropertyActionsFilter actionFilter = new PropertyActionsFilter(
        vehicleActionsFilter,
        new ExecutableActionsTagsPredicate(command),
        actionString -> true,
        EnumSet.allOf(ActionTrigger.class)
    );

    return EdgeMapping.toHorizonEdge(
        step,
        ActionsMapping.mapPropertyActions(step.getPath()).stream()
            .filter(actionFilter)
            .map(propertyAction -> ActionsMapping.fromPropertyAction(vehicle, propertyAction))
            .collect(Collectors.toList())
    );
  }

  private Node mapHorizonNode(@Nonnull MovementCommand command,
                              Step step,
                              long sequenceId,
                              @Nonnull Vehicle vehicle) {
    return NodeMapping.toHorizonNode(
        step.getDestinationPoint(),
        sequenceId,
        vehicle,
        horizonActionsForVehicle(
            command,
            vehicle,
            step,
            step.getRouteIndex() == command.getRoute().getSteps().size() - 1
        )
    );
  }

  private List<Action> horizonActionsForVehicle(@Nonnull MovementCommand command,
                                                @Nonnull Vehicle vehicle,
                                                Step step,
                                                boolean isLastStep) {
    Predicate<PropertyAction> propActionFilter = new PropertyActionsFilter(
        vehicleActionsFilter,
        new ExecutableActionsTagsPredicate(command),
        involvesActualMovement(command)
        ? new ExecutableActionsTagsPredicate(step.getPath())
        : actionString -> true,
        isLastStep ? EnumSet.of(ActionTrigger.ORDER_END) : EnumSet.of(ActionTrigger.PASSING)
    );

    List<PropertyAction> propertyActions
        = ActionsMapping.mapPropertyActions(step.getDestinationPoint());
    if (isLastStep) {
      if (command.getFinalDestinationLocation() != null) {
        propertyActions.addAll(
            ActionsMapping.mapPropertyActions(command.getFinalDestinationLocation())
        );
      }
      horizonMovementCommandPropAction(command, vehicle)
          .ifPresent(action -> propertyActions.add(action));
    }

    return propertyActions.stream()
        .filter(propActionFilter)
        .map(propertyAction -> ActionsMapping.fromPropertyAction(vehicle, propertyAction))
        .collect(Collectors.toList());
  }

  private Optional<PropertyAction> horizonMovementCommandPropAction(
      @Nonnull MovementCommand command,
      @Nonnull Vehicle vehicle) {
    if (command.getFinalOperation().equals(MovementCommand.NO_OPERATION)) {
      return Optional.empty();
    }

    if (command.getFinalDestinationLocation() == null) {
      return Optional.empty();
    }

    return ActionsMapping.fromMovementCommand(
        vehicle,
        command,
        command.getFinalDestinationLocation(),
        objectService.fetchObject(
            LocationType.class,
            command.getFinalDestinationLocation().getType()
        )
    );
  }
}
