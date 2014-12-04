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
import interactivespaces.master.server.services.SpaceControllerRepository;
import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

/**
 * The webflow action for space controller operations.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerAction extends BaseSpaceMasterController {

  /**
   * Repository for space controllers.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * Get a new space controller model.
   *
   * @return a new space controller model
   */
  public SimpleSpaceController newSpaceController() {
    return new SimpleSpaceController();
  }

  /**
   * Add entities to the flow context needed by the new entity page.
   *
   * @param context
   *          the Webflow context
   */
  public void addNeededEntities(RequestContext context) {
    MutableAttributeMap viewScope = context.getViewScope();
    addGlobalModelItems(viewScope);
  }

  /**
   * Save the new space controller.
   *
   * @param controller
   *          the controller form
   */
  public void saveSpaceController(SimpleSpaceController controller) {
    SpaceController finalController = spaceControllerRepository.newSpaceController(controller);

    spaceControllerRepository.saveSpaceController(finalController);

    // So the ID gets copied out of the flow.
    controller.setId(finalController.getId());
  }

  /**
   * Set the space controller repository to use.
   *
   * @param spaceControllerRepository
   *          the space controller repository
   */
  public void setSpaceControllerRepository(SpaceControllerRepository spaceControllerRepository) {
    this.spaceControllerRepository = spaceControllerRepository;
  }
}
