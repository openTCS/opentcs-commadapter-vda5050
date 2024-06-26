/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * Holds the information reference (e.g. orderId, orderUpdateId, actionId) as key-value pairs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfoReference
    implements
      Serializable {

  /**
   * The reference key.
   */
  private String referenceKey;
  /**
   * The reference value.
   */
  private String referenceValue;

  @JsonCreator
  public InfoReference(
      @Nonnull
      @JsonProperty(required = true, value = "referenceKey")
      String referenceKey,
      @Nonnull
      @JsonProperty(required = true, value = "referenceValue")
      String referenceValue
  ) {
    this.referenceKey = requireNonNull(referenceKey, "referenceKey");
    this.referenceValue = requireNonNull(referenceValue, "referenceValue");
  }

  public String getReferenceKey() {
    return referenceKey;
  }

  public InfoReference setReferenceKey(
      @Nonnull
      String referenceKey
  ) {
    this.referenceKey = requireNonNull(referenceKey, "referenceKey");
    return this;
  }

  public String getReferenceValue() {
    return referenceValue;
  }

  public InfoReference setReferenceValue(
      @Nonnull
      String referenceValue
  ) {
    this.referenceValue = requireNonNull(referenceValue, "referenceValue");
    return this;
  }

  @Override
  public String toString() {
    return "InfoReference{" + "referenceKey=" + referenceKey
        + ", referenceValue=" + referenceValue
        + '}';
  }

}
