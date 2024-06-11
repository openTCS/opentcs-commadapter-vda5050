/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.commadapter.vehicle.vda5050.common.mqtt;

import javax.annotation.Nonnull;

/**
 * A listener for events concerning an established connection.
 */
public interface ConnectionEventListener {

  /**
   * Called when a message from the remote peer has been received and decoded.
   *
   * @param message The incoming message.
   */
  void onIncomingMessage(
      @Nonnull
      IncomingMessage message
  );

  /**
   * Called when a connection to the remote peer has been established.
   */
  void onConnect();

  /**
   * Called when a connnection attempt to the remote peer has failed.
   */
  void onFailedConnectionAttempt();

  /**
   * Called when a connection to the remote peer has been closed (by either side).
   */
  void onDisconnect();

  /**
   * Called when the remote peer is considered idle.
   */
  void onIdle();
}
