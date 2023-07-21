/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.common;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.opentcs.data.TCSObject;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for extracting key-value properties from {@link TCSObject}s and
 * {@link MovementCommand}s.
 */
public class PropertyExtractions {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PropertyExtractions.class);

  /**
   * Prevents instantiation.
   */
  private PropertyExtractions() {
  }

  /**
   * Searches objects for a property with the key.
   *
   * @param key The property key to search for
   * @param objects The TCSObjects to search through.
   * @return The value of the first occurance of the property of {@link Optional#EMPTY} if no
   * property with the given key is found.
   */
  public static Optional<String> getProperty(String key, TCSObject<?>... objects) {
    return Stream.of(objects)
        .filter(object -> object.getProperties().containsKey(key))
        .findFirst()
        .map(object -> object.getProperty(key));
  }

  /**
   * Searches objects for a property and tries to parse it as an integer.
   *
   * @param key The property key to search for
   * @param objects The objects to search through.
   * @return The value of the first occurance of the property, or {@link Optional#EMPTY}, if no
   * property with the given key is found.
   */
  public static Optional<Integer> getPropertyInteger(String key, TCSObject<?>... objects) {
    return Stream.of(objects)
        .filter(object -> object.getProperties().containsKey(key))
        .map(object -> {
          try {
            return Integer.valueOf(object.getProperty(key));
          }
          catch (NumberFormatException e) {
            LOG.error("Property '{}' for TCSObject {} cannot be parsed as an Integer.",
                      key,
                      object.getName());
            return null;
          }
        })
        .filter(Objects::nonNull)
        .findFirst();
  }

  /**
   * Searches objects for a property and tries to parse it as a double.
   *
   * @param key The property key to search for
   * @param objects The objects to search through.
   * @return The value of the first occurance of the property, or {@link Optional#EMPTY}, if no
   * property with the given key is found.
   */
  public static Optional<Double> getPropertyDouble(String key, TCSObject<?>... objects) {
    return Stream.of(objects)
        .filter(object -> object.getProperties().containsKey(key))
        .map(object -> {
          try {
            return Double.valueOf(object.getProperty(key));
          }
          catch (NumberFormatException e) {
            LOG.error("Property '{}' for TCSObject {} cannot be parsed as an Integer.",
                      key,
                      object.getName());
            return null;
          }
        })
        .filter(Objects::nonNull)
        .findFirst();
  }

  /**
   * Searches a movement command or its destination location (if any) for a property.
   *
   * @param key The property key to search for.
   * @param command The movement command to search through.
   * @return The value of the first occurance of the property of {@link Optional#EMPTY} if no
   * property with the given key is found.
   */
  public static Optional<String> getProperty(String key, MovementCommand command) {
    return Optional.ofNullable(command.getProperties().get(key))
        .or(command.getOpLocation() != null
            ? () -> getProperty(key, command.getOpLocation())
            : Optional::empty
        );
  }

  /**
   * Searches a movement command or its destination location (if any) for a property and tries to
   * parse it as a float.
   *
   * @param key The property key to search for.
   * @param command The movement command to search through.
   * @return The value of the first occurance of the property of {@link Optional#EMPTY} if no
   * property with the given key is found.
   */
  public static Optional<Float> getPropertyFloat(String key, MovementCommand command) {
    Optional<String> propertyValue = getProperty(key, command);

    if (propertyValue.isEmpty()) {
      return Optional.empty();
    }

    try {
      return Optional.of(Float.valueOf(propertyValue.get()));
    }
    catch (NumberFormatException e) {
      LOG.warn("Property '{}' for MovementCommand {} or its location cannot be parsed as a float.",
               key,
               command);
    }

    return Optional.empty();
  }

}
