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

package interactivespaces.master.ui.internal.web.space;

import interactivespaces.domain.space.Space;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;
import interactivespaces.master.ui.internal.web.WebSupport;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

/**
 * The webflow action for space operations.
 * 
 * @author Keith M. Hughes
 */
public class SpaceAction extends BaseSpaceMasterController {

  /**
   * Repository for activity entities.
   */
  private ActivityRepository activityRepository;

  /**
   * Get a new space model.
   * 
   * @return
   */
  public SpaceForm newSpace() {
    return new SpaceForm();
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

    viewScope.put("spaces", WebSupport.getSpaceSelections(activityRepository.getAllSpaces()));
    viewScope.put("liveactivitygroups",
        WebSupport.getLiveActivityGroupSelections(activityRepository.getAllLiveActivityGroups()));
  }

  /**
   * Save the new space.
   * 
   * @param space
   */
  public void saveSpace(SpaceForm form) {
    Space finalSpace = activityRepository.newSpace();

    form.saveSpace(finalSpace, activityRepository);

    activityRepository.saveSpace(finalSpace);

    // So the ID gets copied out of the flow.
    form.getSpace().setId(finalSpace.getId());
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }
}
