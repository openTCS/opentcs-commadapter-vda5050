/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.v2_0;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.Header;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.connection.Connection;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.state.State;
import org.opentcs.commadapter.vehicle.vda5050.v2_0.message.visualization.Visualization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks whether incoming messages should be accepted or ignored.
 */
public class IncomingMessageFilter {
  private static final Logger LOG = LoggerFactory.getLogger(IncomingMessageFilter.class);

  private HeaderRecord headerRecordConnection = new HeaderRecord();
  private HeaderRecord headerRecordState = new HeaderRecord();
  private HeaderRecord headerRecordVisualization = new HeaderRecord();
  private boolean online;

  /**
   * Creates a new instance.
   */
  public IncomingMessageFilter() {
  }

  /**
   * Checks whether the given message should be accepted, based on what other messages were accepted
   * previously.
   *
   * @param header The message to be checked.
   * @return {@code true} if the message should be accepted.
   */
  public boolean accept(Header header) {
    requireNonNull(header, "header");

    if (header instanceof Visualization visualization) {
      return processVisualization(visualization);
    }
    else if (header instanceof State state) {
      return processState(state);
    }
    else if (header instanceof Connection connection) {
      return processConnection(connection);
    }
    else {
      LOG.warn("Not accepting message of unhandled type: {}", header);
      return false;
    }
  }

  private boolean processConnection(Connection message) {
    switch (message.getConnectionState()) {
      case CONNECTIONBROKEN:
        online = false;

        headerRecordConnection.lastSeenTimestamp = Instant.EPOCH;
        headerRecordState.lastSeenTimestamp = Instant.EPOCH;
        headerRecordVisualization.lastSeenTimestamp = Instant.EPOCH;

        return true;
      case OFFLINE:
        if (message.getHeaderId() <= headerRecordConnection.lastSeenHeaderId
            && !message.getTimestamp().isAfter(headerRecordConnection.lastSeenTimestamp)) {
          LOG.info("Not accepting message with outdated header: {}", message);
          return false;
        }

        online = false;

        headerRecordConnection.lastSeenTimestamp = Instant.EPOCH;
        headerRecordState.lastSeenTimestamp = Instant.EPOCH;
        headerRecordVisualization.lastSeenTimestamp = Instant.EPOCH;

        return true;
      case ONLINE:
        if (message.getHeaderId() <= headerRecordConnection.lastSeenHeaderId
            && !message.getTimestamp().isAfter(headerRecordConnection.lastSeenTimestamp)) {
          LOG.info("Not accepting message with outdated header: {}", message);
          return false;
        }

        online = true;

        headerRecordConnection.lastSeenHeaderId = message.getHeaderId();
        headerRecordConnection.lastSeenTimestamp = message.getTimestamp();

        return true;
      default:
        LOG.warn("Unhandled connection state: {}", message.getConnectionState());
        return true;
    }
  }

  private boolean processState(State message) {
    if (!online) {
      LOG.info("Not accepting message while vehicle is considered offline: {}", message);
      return false;
    }

    if (message.getHeaderId() <= headerRecordState.lastSeenHeaderId
        && !message.getTimestamp().isAfter(headerRecordState.lastSeenTimestamp)) {
      LOG.info("Not accepting message with outdated header: {}", message);
      return false;
    }

    headerRecordState.lastSeenHeaderId = message.getHeaderId();
    headerRecordState.lastSeenTimestamp = message.getTimestamp();

    return true;
  }

  private boolean processVisualization(Visualization message) {
    if (!online) {
      LOG.info("Not accepting message while vehicle is considered offline: {}", message);
      return false;
    }

    if (message.getHeaderId() <= headerRecordVisualization.lastSeenHeaderId
        && !message.getTimestamp().isAfter(headerRecordVisualization.lastSeenTimestamp)) {
      LOG.info("Not accepting message with outdated header: {}", message);
      return false;
    }

    headerRecordVisualization.lastSeenHeaderId = message.getHeaderId();
    headerRecordVisualization.lastSeenTimestamp = message.getTimestamp();

    return true;
  }

  private static class HeaderRecord {
    private long lastSeenHeaderId;
    private Instant lastSeenTimestamp = Instant.EPOCH;

    private HeaderRecord() {
    }
  }
}
