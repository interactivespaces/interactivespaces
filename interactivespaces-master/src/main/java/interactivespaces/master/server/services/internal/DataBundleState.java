// Copyright 2013 Google Inc. All Rights Reserved.

package interactivespaces.master.server.services.internal;

/**
 * Enum that covers the various states the data capture framework can
 * be in.
 *
 * @author peringknife@google.com (Trevor Pering)
 */
public enum DataBundleState {
  NO_REQUEST("space.controller.dataBundle.state.none"),
  CAPTURE_REQUESTED("space.controller.dataBundle.state.capture.requested"),
  CAPTURE_RECEIVED("space.controller.dataBundle.state.capture.received"),
  CAPTURE_ERROR("space.controller.dataBundle.state.capture.error"),
  RESTORE_REQUESTED("space.controller.dataBundle.state.restore.requested"),
  RESTORE_RECEIVED("space.controller.dataBundle.state.restore.received"),
  RESTORE_ERROR("space.controller.dataBundle.state.restore.error");

  /**
   * Description message.
   */
  private String description;

  /**
   * Create data bundle state enum.
   *
   * @param description description message
   */
  DataBundleState(String description) {
    this.description = description;
  }

  /**
   * Return the user-facing description for this state.
   *
   * @return data bundle state description
   */
  public String getDescription() {
    return description;
  }
}
