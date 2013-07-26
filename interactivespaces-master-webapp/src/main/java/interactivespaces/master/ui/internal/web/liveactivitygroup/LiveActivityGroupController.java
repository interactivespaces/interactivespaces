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

package interactivespaces.master.ui.internal.web.liveactivitygroup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.space.Space;
import interactivespaces.expression.FilterExpression;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.EntityNotFoundInteractiveSpacesException;
import interactivespaces.master.server.ui.JsonSupport;
import interactivespaces.master.server.ui.UiActivityManager;
import interactivespaces.master.server.ui.UiLiveActivity;
import interactivespaces.master.ui.internal.web.BaseActiveSpaceMasterController;
import interactivespaces.master.ui.internal.web.UiUtilities;

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
 * Spring MVC controller for activity groups.
 *
 * @author Keith M. Hughes
 */
@Controller
public class LiveActivityGroupController extends BaseActiveSpaceMasterController {

  /**
   * Repository for activity entities.
   */
  private ActivityRepository activityRepository;

  /**
   * Display a list of all activities.
   *
   * @return Model and view for controller list display.
   */
  @RequestMapping("/liveactivitygroup/all.html")
  public ModelAndView listActivities() {
    List<LiveActivityGroup> groups =
        Lists.newArrayList(activityRepository.getAllLiveActivityGroups());
    Collections.sort(groups, UiUtilities.LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);

    ModelAndView mav = getModelAndView();
    mav.setViewName("liveactivitygroup/LiveActivityGroupViewAll");
    mav.addObject("liveactivitygroups", groups);

    return mav;
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/view.html", method = RequestMethod.GET)
  public ModelAndView viewActivityGroup(@PathVariable String id) {
    ModelAndView mav = getModelAndView();

    LiveActivityGroup liveActivityGroup = activityRepository.getLiveActivityGroupById(id);
    if (liveActivityGroup != null) {
      mav.setViewName("liveactivitygroup/LiveActivityGroupView");
      mav.addObject("liveactivitygroup", liveActivityGroup);
      mav.addObject("metadata", UiUtilities.getMetadataView(liveActivityGroup.getMetadata()));

      List<LiveActivity> liveActivities = Lists.newArrayList();
      for (GroupLiveActivity gla : liveActivityGroup.getActivities()) {
        liveActivities.add(gla.getActivity());
      }

      List<UiLiveActivity> liveactivities = uiControllerManager.getUiLiveActivities(liveActivities);
      Collections.sort(liveactivities, UiUtilities.UI_LIVE_ACTIVITY_BY_NAME_COMPARATOR);
      mav.addObject("liveactivities", liveactivities);

      List<Space> spaces =
          Lists.newArrayList(activityRepository.getSpacesByLiveActivityGroup(liveActivityGroup));
      Collections.sort(spaces, UiUtilities.SPACE_BY_NAME_COMPARATOR);
      mav.addObject("spaces", spaces);
    } else {
      mav.setViewName("liveactivitygroup/LiveActivityGroupNonexistent");
    }

    return mav;
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/view.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> viewActivityGroupJson(@PathVariable String id) {
    LiveActivityGroup liveActivityGroup = activityRepository.getLiveActivityGroupById(id);
    if (liveActivityGroup != null) {
      return JsonSupport.getSuccessJsonResponse(uiActivityManager
          .getLiveActivityGroupJsonData(liveActivityGroup));
    } else {
      return noSuchLiveActivityGroupResult();
    }
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/liveactivitystatus.json",
      method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> statusActivityGroup(@PathVariable String id) {
    try {
      uiControllerManager.statusLiveActivityGroup(id);

      return JsonSupport.getSimpleSuccessJsonResponse();
    } catch (EntityNotFoundInteractiveSpacesException e) {
      return noSuchLiveActivityGroupResult();
    }
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/deploy.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> deployActivityGroup(@PathVariable String id) {
    try {
      uiControllerManager.deployLiveActivityGroup(id);

      return JsonSupport.getSimpleSuccessJsonResponse();
    } catch (EntityNotFoundInteractiveSpacesException e) {
      return noSuchLiveActivityGroupResult();
    }
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/configure.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> configureActivityGroup(@PathVariable String id) {
    try {
      uiControllerManager.configureLiveActivityGroup(id);

      return JsonSupport.getSimpleSuccessJsonResponse();
    } catch (EntityNotFoundInteractiveSpacesException e) {
      return noSuchLiveActivityGroupResult();
    }
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/startup.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> startupActivityGroup(@PathVariable String id) {
    try {
      uiControllerManager.startupLiveActivityGroup(id);

      return JsonSupport.getSimpleSuccessJsonResponse();
    } catch (EntityNotFoundInteractiveSpacesException e) {
      return noSuchLiveActivityGroupResult();
    }
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/activate.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> activateActivityGroup(@PathVariable String id) {
    try {
      uiControllerManager.activateLiveActivityGroup(id);

      return JsonSupport.getSimpleSuccessJsonResponse();
    } catch (EntityNotFoundInteractiveSpacesException e) {
      return noSuchLiveActivityGroupResult();
    }
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/deactivate.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> deactivateActivityGroup(@PathVariable String id) {
    try {
      uiControllerManager.deactivateLiveActivityGroup(id);

      return JsonSupport.getSimpleSuccessJsonResponse();
    } catch (EntityNotFoundInteractiveSpacesException e) {
      return noSuchLiveActivityGroupResult();
    }
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/shutdown.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> shutdownLiveActivityGroup(@PathVariable String id) {
    try {
      uiControllerManager.shutdownLiveActivityGroup(id);

      return JsonSupport.getSimpleSuccessJsonResponse();
    } catch (EntityNotFoundInteractiveSpacesException e) {
      return noSuchLiveActivityGroupResult();
    }
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/forceshutdownliveactivities.json",
      method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> forceShutdownLiveActivityLiveActivityGroup(@PathVariable String id) {
    try {
      uiControllerManager.forceShutdownLiveActivitiesLiveActivityGroup(id);

      return JsonSupport.getSimpleSuccessJsonResponse();
    } catch (EntityNotFoundInteractiveSpacesException e) {
      return noSuchLiveActivityGroupResult();
    }
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/metadata.json", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, ? extends Object> modifyLiveActivityZGroupMetadata(@PathVariable String id,
      @RequestBody Object metadataCommandObj, HttpServletResponse response) {

    if (Map.class.isAssignableFrom(metadataCommandObj.getClass())) {
      @SuppressWarnings("unchecked")
      Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

      return uiActivityManager.updateLiveActivityGroupMetadata(id, metadataCommand);
    } else {
      return JsonSupport.getFailureJsonResponse(JsonSupport.MESSAGE_SPACE_CALL_ARGS_NOMAP);
    }
  }

  @RequestMapping(value = "/liveactivitygroup/all.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> listAllGroupsJson(
      @RequestParam(value = "filter", required = false) String filter) {
    List<Map<String, Object>> data = Lists.newArrayList();

    try {
      FilterExpression filterExpression = expressionFactory.getFilterExpression(filter);

      for (LiveActivityGroup group : activityRepository.getLiveActivityGroups(filterExpression)) {
        Map<String, Object> groupData = Maps.newHashMap();

        uiActivityManager.getBasicLiveActivityGroupJsonData(group, groupData);

        data.add(groupData);
      }

      return JsonSupport.getSuccessJsonResponse(data);
    } catch (Exception e) {
      spacesEnvironment.getLog().error("Attempt to get live activity group data failed", e);

      return JsonSupport.getFailureJsonResponse("call failed");
    }
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/delete.html", method = RequestMethod.GET)
  public String deleteActivityGroup(@PathVariable String id) {
    uiActivityManager.deleteActivityGroup(id);

    return "redirect:/liveactivitygroup/all.html";
  }

  /**
   * @return
   */
  private Map<String, Object> noSuchLiveActivityGroupResult() {
    return JsonSupport
        .getFailureJsonResponse(UiActivityManager.MESSAGE_SPACE_DOMAIN_LIVEACTIVITYGROUP_UNKNOWN);
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }
}
