/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;

/**
 * Describes the state of an action already processed, currently being process or to be processed
 * by an AGV.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionState
    implements Serializable {

  /**
   * Unique action ID.
   */
  private String actionId;
  /**
   * Type of the action.
   * <p>
   * Only for informational or visualization purposes.
   */
  private String actionType;
  /**
   * [Optional] Additional information on the current action.
   */
  private String actionDescription;
  /**
   * Status of this action.
   */
  private ActionStatus actionStatus;
  /**
   * [Optional] Description of the result.
   * <p>
   * E.g. the result of a RFID-read.
   */
  private String resultDescription;

  @JsonCreator
  public ActionState(
      @Nonnull @JsonProperty(required = true, value = "actionId") String actionId,
      @Nonnull @JsonProperty(required = true, value = "actionType") String actionType,
      @Nonnull @JsonProperty(required = true, value = "actionStatus") ActionStatus actionStatus) {
    this.actionId = requireNonNull(actionId, "actionId");
    this.actionType = requireNonNull(actionType, "actionType");
    this.actionStatus = requireNonNull(actionStatus, "actionStatus");
  }

  public String getActionId() {
    return actionId;
  }

  public ActionState setActionId(@Nonnull String actionId) {
    this.actionId = requireNonNull(actionId, "actionId");
    return this;
  }

  public String getActionDescription() {
    return actionDescription;
  }

  public ActionState setActionDescription(String actionDescription) {
    this.actionDescription = actionDescription;
    return this;
  }

  public ActionStatus getActionStatus() {
    return actionStatus;
  }

  public ActionState setActionStatus(@Nonnull ActionStatus actionStatus) {
    this.actionStatus = requireNonNull(actionStatus, "actionStatus");
    return this;
  }

  public String getResultDescription() {
    return resultDescription;
  }

  public ActionState setResultDescription(String resultDescription) {
    this.resultDescription = resultDescription;
    return this;
  }

  public String getActionType() {
    return actionType;
  }

  public ActionState setActionType(@Nonnull String actionType) {
    this.actionType = requireNonNull(actionType, "actionType");
    return this;
  }

  @Override
  public String toString() {
    return "ActionState{" + "actionId=" + actionId
        + ", actionType=" + actionType
        + ", actionDescription=" + actionDescription
        + ", actionStatus=" + actionStatus
        + ", resultDescription=" + resultDescription
        + '}';
  }

}
