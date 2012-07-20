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

package interactivespaces.master.ui.internal.web.spacecontroller;

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.master.server.services.ControllerRepository;

/**
 * The webflow action for space controller operations.
 * 
 * @author Keith M. Hughes
 */
public class SpaceControllerAction {
	
	/**
	 * Repository for controllers.
	 */
	private ControllerRepository controllerRepository;

	/**
	 * Get a new controller model.
	 * 
	 * @return
	 */
	public SimpleSpaceController newSpaceController() {
		return new SimpleSpaceController();
	}

	/**
	 * Save the new controller.
	 * 
	 * @param controller
	 */
	public void saveSpaceController(SimpleSpaceController controller) {
		SpaceController finalController = controllerRepository
				.newSpaceController(controller);

		controllerRepository.saveSpaceController(finalController);

		// So the ID gets copied out of the flow.
		controller.setId(finalController.getId());
	}

	/**
	 * @param controllerRepository
	 *            the controllerRepository to set
	 */
	public void setControllerRepository(
			ControllerRepository controllerRepository) {
		this.controllerRepository = controllerRepository;
	}

}
