/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;

/**
 * Binds JSON strings to objects and vice versa.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class JsonBinder {

  /**
   * Maps between objects and their JSON representations.
   */
  private final ObjectMapper objectMapper
      = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  /**
   * Creates a new instance.
   */
  public JsonBinder() {
  }

  /**
   * Maps the given JSON string to an object.
   *
   * @param <T> The type of object to map to.
   * @param jsonString The JSON string.
   * @param clazz The type of object to map to.
   * @return The object created from the JSON string.
   * @throws IllegalArgumentException In case there was a problem mapping the given object from
   * JSON.
   */
  public <T> T fromJson(String jsonString, Class<T> clazz)
      throws IllegalArgumentException {
    try {
      return objectMapper.readValue(jsonString, clazz);
    }
    catch (IOException exc) {
      throw new IllegalArgumentException("Could not parse JSON input", exc);
    }
  }

  /**
   * Maps the given object to a JSON string.
   *
   * @param object The object to be mapped.
   * @return The JSON string representation of the object.
   * @throws IllegalArgumentException In case there was a problem mapping the given object to JSON.
   */
  public String toJson(Object object)
      throws IllegalArgumentException {
    try {
      return objectMapper
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(object);
    }
    catch (JsonProcessingException exc) {
      throw new IllegalArgumentException("Could not produce JSON output", exc);
    }
  }
}
