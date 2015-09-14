/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package interactivespaces.master.server.services.internal;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.container.data.SpaceControllerInformationValidator;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.logging.ExtendedLog;
import interactivespaces.master.server.remote.master.RemoteMasterServer;
import interactivespaces.master.server.remote.master.RemoteMasterServerListener;
import interactivespaces.master.server.services.SpaceControllerRepository;

/**
 * A bridge between the {@link RemoteMasterServer} and the master.
 *
 * @author Keith M. Hughes
 */
public class RemoteMasterServerBridge implements RemoteMasterServerListener {

  /**
   * The repository for controllers.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * The remote master server this is bridging.
   */
  private RemoteMasterServer remoteMasterServer;

  /**
   * The logger for this bridge.
   */
  private ExtendedLog log;

  /**
   * The validator for space controller information.
   */
  private SpaceControllerInformationValidator spaceControllerInformationValidator =
      new SpaceControllerInformationValidator();

  /**
   * Start the bridge up.
   */
  public void startup() {
    remoteMasterServer.addListener(this);
  }

  /**
   * Shut the bridge down.
   */
  public void shutdown() {
    remoteMasterServer.removeListener(this);
  }

  @Override
  public void onControllerRegistration(SpaceController registeringController) {
    SpaceController existingController =
        spaceControllerRepository.getSpaceControllerByUuid(registeringController.getUuid());
    if (existingController == null) {
      handleNeverBeforeSeenSpaceController(registeringController);
    } else {
      handleExistingSpaceController(registeringController, existingController);
    }
  }

  /**
   * Handle a space controller that has never been seen before.
   *
   * @param registeringController
   *          the information about the registering controller
   */
  private void handleNeverBeforeSeenSpaceController(SpaceController registeringController) {
    // If it is a new controller, an instance must be created and stored in the space controller repository.

    String registeringUuid = registeringController.getUuid();
    if (log.isInfoEnabled()) {
      log.formatInfo("Controller %s was unrecognized. Creating new record.", registeringUuid);
      log.formatInfo("\tName: %s\n\tDescription: %s\n\tHost ID: %s", registeringController.getName(),
          registeringController.getDescription(), registeringController.getHostId());
    }

    checkSpaceControllerInfo(registeringController);

    SpaceController newController =
        spaceControllerRepository.newSpaceController(registeringUuid, registeringController);

    spaceControllerRepository.saveSpaceController(newController);
  }

  /**
   * Handle an existing space controller.
   *
   * @param registeringController
   *          the information coming in
   * @param existingController
   *          the existing controller pulled from the space controller repository
   */
  private void
      handleExistingSpaceController(SpaceController registeringController, SpaceController existingController) {
    // if the space controller exists, check to see if the host ID has changed. if it has, update the controller from
    // the repository.

    if (shouldRecordBeUpdated(registeringController, existingController)) {
      String registeringHostId = registeringController.getHostId();
      String registeringName = registeringController.getName();
      log.formatInfo("Changing space controller data: Old Name: %s\tNew Name: %s\tOld Host ID: %s\tNew Host ID: %s",
          existingController.getName(), registeringName, existingController.getHostId(), registeringHostId);

      existingController.setHostId(registeringHostId);
      existingController.setName(registeringName);

      spaceControllerRepository.saveSpaceController(existingController);
    }
  }

  /**
   * Should a controller record be updated?
   *
   * @param registeringController
   *          the incoming controller information
   * @param existingController
   *          the existing controller information
   *
   * @return {@code true} if the existing controller record should be updated
   */
  private boolean shouldRecordBeUpdated(SpaceController registeringController, SpaceController existingController) {
    return !existingController.getHostId().equals(registeringController.getHostId())
        || !existingController.getName().equals(registeringController.getName());
  }

  /**
   * Check to see if the incoming controller host ID information is valid.
   *
   * @param registeringController
   *          the controller information
   *
   * @throws InteractiveSpacesException
   *           the hostID was invalid
   */
  private void checkSpaceControllerInfo(SpaceController registeringController) throws InteractiveSpacesException {
    StringBuilder errorBuilder =
        spaceControllerInformationValidator.checkControllerInfoForErrors(registeringController, log);
    if (errorBuilder.length() != 0) {
      throw new SimpleInteractiveSpacesException(errorBuilder.toString());
    }
  }

  /**
   * @param spaceControllerRepository
   *          the controllerRepository to set
   */
  public void setSpaceControllerRepository(SpaceControllerRepository spaceControllerRepository) {
    this.spaceControllerRepository = spaceControllerRepository;
  }

  /**
   * @param remoteMasterServer
   *          the remoteMasterServer to set
   */
  public void setRemoteMasterServer(RemoteMasterServer remoteMasterServer) {
    this.remoteMasterServer = remoteMasterServer;
  }

  /**
   * @param log
   *          the log to set
   */
  public void setLog(ExtendedLog log) {
    this.log = log;
  }
}
