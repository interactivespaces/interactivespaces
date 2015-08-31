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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.support.DomainValidationResult.DomainValidationResultType;
import interactivespaces.domain.support.SpaceControllerHostIdValidator;
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
   * The validator for host IDs.
   */
  private SpaceControllerHostIdValidator hostIdValidator = new SpaceControllerHostIdValidator();

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
    String registeringUuid = registeringController.getUuid();
    SpaceController existingController = spaceControllerRepository.getSpaceControllerByUuid(registeringUuid);
    if (existingController == null) {
      if (log.isInfoEnabled()) {
        log.formatInfo("Controller %s was unrecognized. Creating new record.", registeringUuid);
        log.formatInfo("\tName: %s\n\tDescription: %s\n\tHost ID: %s", registeringController.getName(),
            registeringController.getDescription(), registeringController.getHostId());
      }

      String hostId = registeringController.getHostId();
      if (hostIdValidator.validate(hostId).getResultType() == DomainValidationResultType.ERRORS) {
        throw SimpleInteractiveSpacesException.newFormattedException(
            "Space controller registration with invalid host id %s", hostId);
      }

      SpaceController newController =
          spaceControllerRepository.newSpaceController(registeringUuid, registeringController);

      spaceControllerRepository.saveSpaceController(newController);
    } else {
      String existingHostId = existingController.getHostId();
      String registeringHostId = registeringController.getHostId();
      if (!registeringHostId.equals(existingHostId)) {
        if (hostIdValidator.validate(registeringHostId).getResultType() == DomainValidationResultType.ERRORS) {
          throw SimpleInteractiveSpacesException.newFormattedException(
              "Space controller registration with invalid host id %s", registeringHostId);
        }

        log.formatInfo(
            "Changing space controller host ID\tName: %s\n\tDescription: %s\n\tOld Host ID: %s\tNew Host ID: %s",
            registeringController.getName(), registeringController.getDescription(), existingHostId, registeringHostId);

        existingController.setHostId(registeringHostId);

        spaceControllerRepository.saveSpaceController(existingController);
      }
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
