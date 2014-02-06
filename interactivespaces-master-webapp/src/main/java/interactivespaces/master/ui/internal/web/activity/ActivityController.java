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

package interactivespaces.master.ui.internal.web.activity;

import interactivespaces.domain.basic.Activity;
import interactivespaces.master.api.MasterApiLiveActivity;
import interactivespaces.master.api.MasterApiMessageSupport;
import interactivespaces.master.api.MasterApiUtilities;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.ui.internal.web.BaseActiveSpaceMasterController;
import interactivespaces.master.ui.internal.web.UiUtilities;

import com.google.common.collect.Lists;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Spring MVC controller for activity operations.
 *
 * @author Keith M. Hughes
 */
@Controller
public class ActivityController extends BaseActiveSpaceMasterController {

  /**
   * Repository for activity entities.
   */
  private ActivityRepository activityRepository;

  /**
   * Display a list of all activities.
   *
   * @return Model and view for controller list display.
   */
  @RequestMapping("/activity/all.html")
  public ModelAndView listActivities() {
    List<Activity> activities = Lists.newArrayList(activityRepository.getAllActivities());
    Collections.sort(activities, MasterApiUtilities.ACTIVITY_BY_NAME_AND_VERSION_COMPARATOR);

    ModelAndView mav = getModelAndView();

    mav.setViewName("activity/ActivityViewAll");
    mav.addObject("activities", activities);

    return mav;
  }

  @RequestMapping(value = "/activity/{id}/view.html", method = RequestMethod.GET)
  public ModelAndView viewActivity(@PathVariable String id) {
    Activity activity = activityRepository.getActivityById(id);
    if (activity != null) {
      List<MasterApiLiveActivity> liveactivities =
          masterApiControllerManager.getUiLiveActivities(activityRepository.getLiveActivitiesByActivity(activity));
      Collections.sort(liveactivities, MasterApiUtilities.UI_LIVE_ACTIVITY_BY_NAME_COMPARATOR);

      ModelAndView mav = getModelAndView();
      mav.setViewName("activity/ActivityView");
      mav.addObject("activity", activity);
      mav.addObject("liveactivities", liveactivities);

      mav.addObject("metadata", UiUtilities.getMetadataView(activity.getMetadata()));

      return mav;
    } else {
      ModelAndView mav = getModelAndView();
      mav.setViewName("activity/ActivityNonexistent");

      return mav;
    }
  }

  @RequestMapping(value = "/activity/{id}/delete.html", method = RequestMethod.GET)
  public ModelAndView deleteActivity(@PathVariable String id) {
    ModelAndView mav = getModelAndView();

    Map<String, Object> response = masterApiActivityManager.deleteActivity(id);
    if (MasterApiMessageSupport.isSuccessResponse(response)) {
      mav.clear();
      mav.setViewName("redirect:/activity/all.html");
    } else {
      mav.setViewName("activity/ActivityNonexistent");
    }

    return mav;
  }

  @RequestMapping(value = "/activity/all.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> getAllActivities(@RequestParam(value = "filter", required = false) String filter) {
    return masterApiActivityManager.getActivitiesByFilter(filter);
  }

  @RequestMapping(value = "/activity/{id}/deploy.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> deployActivities(@PathVariable String id) {
    return masterApiControllerManager.deployAllActivityInstances(id);
  }

  @RequestMapping(value = "/activity/{id}/metadata.json", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, ? extends Object> modifyLiveActivityMetadata(@PathVariable String id,
      @RequestBody Object metadataCommandObj, HttpServletResponse response) {

    if (Map.class.isAssignableFrom(metadataCommandObj.getClass())) {
      @SuppressWarnings("unchecked")
      Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

      return masterApiActivityManager.updateActivityMetadata(id, metadataCommand);
    } else {
      return MasterApiMessageSupport.getFailureResponse(MasterApiMessageSupport.MESSAGE_SPACE_CALL_ARGS_NOMAP);
    }
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }
}
