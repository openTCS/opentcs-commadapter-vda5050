/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v1_1.ordermapping;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.opentcs.commadapter.vehicle.vda5050.v1_1.ObjectProperties.PROPKEY_EXECUTABLE_ACTIONS_TAGS;
import org.opentcs.data.model.Vehicle;

/**
 * Tests the executable actions predicate.
 */
public class ExecutableActionsTagsPredicateTest {

  /**
   * Class under test.
   */
  private ExecutableActionsTagsPredicate predicate;

  @Test
  public void shouldEvaluateToTrueForKnownActions() {
    Vehicle vehicle = new Vehicle("")
        .withProperty(PROPKEY_EXECUTABLE_ACTIONS_TAGS, "go|stop|beep");
    predicate = new ExecutableActionsTagsPredicate(vehicle);

    assertTrue(predicate.test("go"));
    assertTrue(predicate.test("stop"));
    assertTrue(predicate.test("beep"));
  }

  @Test
  public void shouldEvaluateToTrueForKnownActionsDisregardingWhitespaces() {
    Vehicle vehicle = new Vehicle("")
        .withProperty(PROPKEY_EXECUTABLE_ACTIONS_TAGS, "    go |  stop | beep  ");
    predicate = new ExecutableActionsTagsPredicate(vehicle);

    assertTrue(predicate.test("go"));
    assertTrue(predicate.test("stop"));
    assertTrue(predicate.test("beep"));
  }

  @Test
  public void shouldEvaluateToFalseForUnknownAction() {
    Vehicle vehicle = new Vehicle("")
        .withProperty(PROPKEY_EXECUTABLE_ACTIONS_TAGS, "go|stop|beep");
    predicate = new ExecutableActionsTagsPredicate(vehicle);

    assertFalse(predicate.test("unknownAction"));
  }

  @Test
  public void shouldEvaluateToTrueIfPropertyIsMissing() {
    Vehicle vehicle = new Vehicle("");
    predicate = new ExecutableActionsTagsPredicate(vehicle);

    assertTrue(predicate.test("unknownAction"));
  }

  @Test
  public void shouldEvaluateToFalseIfPropertyIsEmpty() {
    Vehicle vehicle = new Vehicle("")
        .withProperty(PROPKEY_EXECUTABLE_ACTIONS_TAGS, "");
    predicate = new ExecutableActionsTagsPredicate(vehicle);

    assertFalse(predicate.test("unknownAction"));
  }

  @Test
  public void shouldEvaluateToTrueIfWildcardIsPresent() {
    Vehicle vehicle = new Vehicle("")
        .withProperty(PROPKEY_EXECUTABLE_ACTIONS_TAGS, "go|stop|beep|*");
    predicate = new ExecutableActionsTagsPredicate(vehicle);

    assertTrue(predicate.test("unknownAction"));
  }
}
