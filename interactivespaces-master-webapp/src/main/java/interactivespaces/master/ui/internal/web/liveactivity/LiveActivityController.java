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

import interactivespaces.domain.basic.LiveActivityGroup;
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
 * Spring MVC controller for working with live activities.
 *
 * @author Keith M. Hughes
 */
@Controller
public class LiveActivityController extends BaseActiveSpaceMasterController {

  /**
   * Repository for activity entities.
   */
  private ActivityRepository activityRepository;

  /**
   * Regexp pattern for finding HTTP links.
   */
  private static final String HTTP_LINK_REGEXP = "http://\\S+";

  /**
   * Format string to use when replacing HTTP links during rewrites.
   */
  private static final String HTTP_LINK_REPLACEMENT = "<a href='$0'>$0</a>";

  /**
   * Display a list of all installed activities.
   *
   * @return Model and view for controller list display.
   */
  @RequestMapping("/liveactivity/all.html")
  public ModelAndView listActivities() {
    List<MasterApiLiveActivity> liveactivities = masterApiControllerManager.getAllUiLiveActivities();
    Collections.sort(liveactivities, MasterApiUtilities.UI_LIVE_ACTIVITY_BY_NAME_COMPARATOR);

    ModelAndView mav = getModelAndView();
    mav.setViewName("liveactivity/LiveActivityViewAll");
    mav.addObject("liveactivities", liveactivities);

    return mav;
  }

  @RequestMapping(value = "/liveactivity/{id}/view.html", method = RequestMethod.GET)
  public ModelAndView viewActivity(@PathVariable String id) {
    ModelAndView mav = getModelAndView();

    MasterApiLiveActivity liveactivity = masterApiControllerManager.getUiLiveActivity(id);
    if (liveactivity != null) {
      mav.setViewName("liveactivity/LiveActivityView");
      mav.addObject("liveactivity", liveactivity);
      mav.addObject("metadata", UiUtilities.getMetadataView(liveactivity.getActivity().getMetadata()));
      mav.addObject("runtimeStateDetail", rewriteRuntimeStateDetail(liveactivity.getActive().getRuntimeStateDetail()));

      List<LiveActivityGroup> groups =
          Lists.newArrayList(activityRepository.getLiveActivityGroupsByLiveActivity(liveactivity.getActivity()));
      Collections.sort(groups, MasterApiUtilities.LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);
      mav.addObject("liveactivitygroups", groups);

    } else {
      mav.setViewName("liveactivity/LiveActivityNonexistent");
    }

    return mav;
  }

  @RequestMapping(value = "/liveactivity/all.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> listActivitiesJson(@RequestParam(value = "filter", required = false) String filter) {
    return masterApiActivityManager.getLiveActivitiesByFilter(filter);
  }

  @RequestMapping(value = "/liveactivity/{id}/view.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> viewLiveActivityJson(@PathVariable String id) {
    return masterApiActivityManager.getLiveActivityView(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/deploy.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> deployLiveActivity(@PathVariable String id) {
    return masterApiControllerManager.deployLiveActivity(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/configure.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> configureLiveActivity(@PathVariable String id) {
    return masterApiControllerManager.configureLiveActivity(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/configuration.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> getLiveActivityConfiguration(@PathVariable String id) {
    return masterApiActivityManager.getLiveActivityConfiguration(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/configuration.json", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, ? extends Object> setLiveActivityConfiguration(@PathVariable String id, @RequestBody Object config,
      HttpServletResponse response) {

    if (Map.class.isAssignableFrom(config.getClass())) {
      @SuppressWarnings("unchecked")
      Map<String, String> map = (Map<String, String>) config;

      return masterApiActivityManager.configureLiveActivity(id, map);
    } else {
      return MasterApiMessageSupport.getFailureResponse(MasterApiMessageSupport.MESSAGE_SPACE_CALL_ARGS_NOMAP);
    }

  }

  @RequestMapping(value = "/liveactivity/{id}/metadata.json", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, ? extends Object> modifyLiveActivityMetadata(@PathVariable String id,
      @RequestBody Object metadataCommandObj, HttpServletResponse response) {

    if (Map.class.isAssignableFrom(metadataCommandObj.getClass())) {
      @SuppressWarnings("unchecked")
      Map<String, Object> metadataCommand = (Map<String, Object>) metadataCommandObj;

      return masterApiActivityManager.updateLiveActivityMetadata(id, metadataCommand);
    } else {
      return MasterApiMessageSupport.getFailureResponse(MasterApiMessageSupport.MESSAGE_SPACE_CALL_ARGS_NOMAP);
    }
  }

  @RequestMapping(value = "/liveactivity/{id}/startup.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> startupActivity(@PathVariable String id) {
    return masterApiControllerManager.startupLiveActivity(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/activate.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> activateActivity(@PathVariable String id) {
    return masterApiControllerManager.activateLiveActivity(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/deactivate.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> deactivateActivity(@PathVariable String id) {
    return masterApiControllerManager.deactivateLiveActivity(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/shutdown.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> shutdownActivity(@PathVariable String id) {
    return masterApiControllerManager.shutdownLiveActivity(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/status.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> statusLiveActivity(@PathVariable String id) {
    return masterApiControllerManager.statusLiveActivity(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/cleantmpdata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> cleanTempDataActivity(@PathVariable String id) {
    return masterApiControllerManager.cleanLiveActivityTempData(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/cleanpermanentdata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> cleanPermanentDataActivity(@PathVariable String id) {
    return masterApiControllerManager.cleanLiveActivityPermanentData(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/delete.html", method = RequestMethod.GET)
  public ModelAndView deleteActivity(@PathVariable String id) {
    ModelAndView mav = getModelAndView();

    Map<String, Object> response = masterApiActivityManager.deleteLiveActivity(id);
    if (MasterApiMessageSupport.isSuccessResponse(response)) {
      mav.clear();
      mav.setViewName("redirect:/liveactivity/all.html");
    } else {
      mav.setViewName("liveactivity/LiveActivityNonexistent");
    }

    return mav;
  }

  @RequestMapping(value = "/liveactivity/{id}/remotedelete.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> remoteDeleteActivity(@PathVariable String id) {
    return masterApiControllerManager.deleteLiveActivity(id);
  }

  /**
   * Rewrite the given detail string, linkifying any embedded http links.
   *
   * @param detail
   *          detail message to rewrite
   * @return rewritten detail message
   */
  private String rewriteRuntimeStateDetail(String detail) {
    if (detail == null) {
      return null;
    }
    return detail.replaceAll(HTTP_LINK_REGEXP, HTTP_LINK_REPLACEMENT);
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }
}
