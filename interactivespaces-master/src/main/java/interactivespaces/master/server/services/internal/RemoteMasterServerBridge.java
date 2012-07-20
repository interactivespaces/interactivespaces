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

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.server.remote.master.RemoteMasterServer;
import interactivespaces.master.server.remote.master.RemoteMasterServerListener;
import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.services.ControllerRepository;

import org.apache.commons.logging.Log;

/**
 * A bridge between the {@link RemoteMasterServer} and the master.
 * 
 * @author Keith M. Hughes
 */
public class RemoteMasterServerBridge implements RemoteMasterServerListener {

	/**
	 * The repository for controllers.
	 */
	private ControllerRepository controllerRepository;

	/**
	 * The active controller manager.
	 */
	private ActiveControllerManager activeControllerManager;

	/**
	 * The remote master server this is bridging.
	 */
	private RemoteMasterServer remoteMasterServer;

	/**
	 * The logger for this bridge.
	 */
	private Log log;

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
		SpaceController existingController = controllerRepository
				.getSpaceControllerByUuid(registeringUuid);
		if (existingController == null) {
			if (log.isInfoEnabled()) {
				log.info(String.format(
						"Controller %s was unrecognized. Creating new record.",
						registeringUuid));
				log.info(String.format(
						"\tName: %s\n\tDescription: %s\n\tHost ID: %s",
						registeringController.getName(),
						registeringController.getDescription(),
						registeringController.getHostId()));
			}

			SpaceController newController = controllerRepository
					.newSpaceController(registeringUuid, registeringController);

			controllerRepository.saveSpaceController(newController);
		}
	}

	/**
	 * @param controllerRepository
	 *            the controllerRepository to set
	 */
	public void setControllerRepository(
			ControllerRepository controllerRepository) {
		this.controllerRepository = controllerRepository;
	}

	/**
	 * @param activeControllerManager
	 *            the activeControllerManager to set
	 */
	public void setActiveControllerManager(
			ActiveControllerManager activeControllerManager) {
		this.activeControllerManager = activeControllerManager;
	}

	/**
	 * @param remoteMasterServer
	 *            the remoteMasterServer to set
	 */
	public void setRemoteMasterServer(RemoteMasterServer remoteMasterServer) {
		this.remoteMasterServer = remoteMasterServer;
	}

	/**
	 * @param log
	 *            the log to set
	 */
	public void setLog(Log log) {
		this.log = log;
	}
}
