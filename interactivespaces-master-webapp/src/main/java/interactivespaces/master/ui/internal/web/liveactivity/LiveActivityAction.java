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

package interactivespaces.master.ui.internal.web.liveactivity;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleLiveActivity;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.ControllerRepository;
import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;
import interactivespaces.master.ui.internal.web.WebSupport;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;

/**
 * Webflow action for live activities.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityAction extends BaseSpaceMasterController {

  /**
   * Repository for activities.
   */
  private ActivityRepository activityRepository;

  /**
   * Repository for controllers.
   */
  private ControllerRepository controllerRepository;

  /**
   * Get a new controller model.
   *
   * @return
   */
  public LiveActivityForm newLiveActivity() {
    return new LiveActivityForm();
  }

  /**
   * Add entities to the flow context needed by the new entity page.
   *
   * @param context
   *          The Webflow context.
   */
  public void addNeededEntities(RequestContext context) {
    MutableAttributeMap viewScope = context.getViewScope();
    addGlobalModelItems(viewScope);

    viewScope.put("activities",
        WebSupport.getActivitySelections(activityRepository.getAllActivities()));
    viewScope.put("controllers",
        WebSupport.getControllerSelections(controllerRepository.getAllSpaceControllers()));
  }

  /**
   * Save the new live activity.
   *
   * @param form
   *          the live activity form
   */
  public void saveLiveActivity(LiveActivityForm form) {
    SimpleLiveActivity liveactivity = form.getLiveActivity();

    LiveActivity finalLiveActivity = activityRepository.newLiveActivity();
    finalLiveActivity.setName(liveactivity.getName());
    finalLiveActivity.setDescription(liveactivity.getDescription());

    SpaceController controller =
        controllerRepository.getSpaceControllerById(form.getControllerId());
    finalLiveActivity.setController(controller);

    Activity activity = activityRepository.getActivityById(form.getActivityId());
    finalLiveActivity.setActivity(activity);

    activityRepository.saveLiveActivity(finalLiveActivity);

    // So the ID gets copied out of the flow.
    liveactivity.setId(finalLiveActivity.getId());
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * @param controllerRepository
   *          the controllerRepository to set
   */
  public void setControllerRepository(ControllerRepository controllerRepository) {
    this.controllerRepository = controllerRepository;
  }

  /**
   * Form bean to keep all info about the installed activity.
   *
   * @author Keith M. Hughes
   */
  public static class LiveActivityForm implements Serializable {
    /**
     * Info about the installed activity.
     */
    private SimpleLiveActivity liveActivity = new SimpleLiveActivity();

    /**
     * The ID of the controller.
     */
    private String controllerId;

    /**
     * The ID of the activity.
     */
    private String activityId;

    /**
     * @return the liveActivity
     */
    public SimpleLiveActivity getLiveActivity() {
      return liveActivity;
    }

    /**
     * @param liveActivity
     *          the liveActivity to set
     */
    public void setLiveActivity(SimpleLiveActivity liveActivity) {
      this.liveActivity = liveActivity;
    }

    /**
     * @return the controllerId
     */
    public String getControllerId() {
      return controllerId;
    }

    /**
     * @param controllerId
     *          the controllerId to set
     */
    public void setControllerId(String controllerId) {
      this.controllerId = controllerId;
    }

    /**
     * @return the activityId
     */
    public String getActivityId() {
      return activityId;
    }

    /**
     * @param activityId
     *          the activityId to set
     */
    public void setActivityId(String activityId) {
      this.activityId = activityId;
    }
  }
}
