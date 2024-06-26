/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping;

import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_EXECUTABLE_ACTIONS_TAGS;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.opentcs.data.TCSObject;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * A predicate to test if an action is executable by a specific vehicle.
 */
public class ExecutableActionsTagsPredicate
    implements
      Predicate<String> {

  /**
   * List of executable actions this vehicle can process.
   */
  private final Set<String> executableActions = new HashSet<>();

  /**
   * Creates an executable actions predicate from a TCSObject.
   *
   * @param obj The TCSObject to extract executable actions from.
   */
  public ExecutableActionsTagsPredicate(TCSObject<?> obj) {
    this(obj.getProperties());
  }

  /**
   * Creates an executable actions predicate from a movement command.
   *
   * @param command The movement command to extract executable actions from.
   */
  public ExecutableActionsTagsPredicate(MovementCommand command) {
    this(command.getProperties());
  }

  /**
   * Creates an executable actions predicate from a set of properties.
   *
   * @param properties The properties to extract executable actions from.
   */
  public ExecutableActionsTagsPredicate(Map<String, String> properties) {
    String value = properties.get(PROPKEY_EXECUTABLE_ACTIONS_TAGS);
    if (value != null) {
      executableActions.addAll(
          Stream.of(value.split("\\|"))
              .map(action -> action.strip())
              .collect(Collectors.toSet())
      );
    }
  }

  @Override
  public boolean test(String subject) {
    if (executableActions.isEmpty()) {
      return true;
    }
    if (executableActions.contains("*")) {
      return true;
    }
    return executableActions.contains(subject);
  }
}
