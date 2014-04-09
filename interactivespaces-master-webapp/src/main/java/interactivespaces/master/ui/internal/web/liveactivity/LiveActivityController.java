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

import interactivespaces.master.api.MasterApiMessage;
import interactivespaces.master.api.MasterApiMessageSupport;
import interactivespaces.master.ui.internal.web.BaseActiveSpaceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
   * Display a list of all installed activities.
   *
   * @return Model and view for controller list display.
   */
  @RequestMapping("/liveactivity/all.html")
  public ModelAndView listActivities() {
    Map<String, Object> response = masterApiActivityManager.getLiveActivitiesByFilter(null);

    ModelAndView mav = getModelAndView();
    mav.setViewName("liveactivity/LiveActivityViewAll");
    mav.addObject("liveactivities", response.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA));
    mav.addObject("canCreateLiveActivities", masterApiActivityManager.canCreateLiveActivities());

    return mav;
  }

  @RequestMapping(value = "/liveactivity/{id}/view.html", method = RequestMethod.GET)
  public ModelAndView viewActivity(@PathVariable String id) {
    Map<String, Object> response = masterApiActivityManager.getLiveActivityFullView(id);

    ModelAndView mav = getModelAndView();
    if (MasterApiMessageSupport.isSuccessResponse(response)) {
      mav.setViewName("liveactivity/LiveActivityView");
      mav.addAllObjects(MasterApiMessageSupport.getResponseDataMap(response));
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
    return masterApiSpaceControllerManager.deployLiveActivity(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/configure.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> configureLiveActivity(@PathVariable String id) {
    return masterApiSpaceControllerManager.configureLiveActivity(id);
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
      return MasterApiMessageSupport.getFailureResponse(MasterApiMessage.MESSAGE_SPACE_CALL_ARGS_NOMAP);
    }

  }

  @RequestMapping(value = "/liveactivity/{id}/metadata.json", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, ? extends Object> modifyLiveActivityMetadata(@PathVariable String id,
      @RequestBody Object metadataCommand, HttpServletResponse response) {

    return masterApiActivityManager.updateMetadataLiveActivity(id, metadataCommand);
  }

  @RequestMapping(value = "/liveactivity/{id}/startup.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> startupActivity(@PathVariable String id) {
    return masterApiSpaceControllerManager.startupLiveActivity(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/activate.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> activateActivity(@PathVariable String id) {
    return masterApiSpaceControllerManager.activateLiveActivity(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/deactivate.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> deactivateActivity(@PathVariable String id) {
    return masterApiSpaceControllerManager.deactivateLiveActivity(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/shutdown.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> shutdownActivity(@PathVariable String id) {
    return masterApiSpaceControllerManager.shutdownLiveActivity(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/status.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> statusLiveActivity(@PathVariable String id) {
    return masterApiSpaceControllerManager.statusLiveActivity(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/cleantmpdata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> cleanTempDataActivity(@PathVariable String id) {
    return masterApiSpaceControllerManager.cleanLiveActivityTempData(id);
  }

  @RequestMapping(value = "/liveactivity/{id}/cleanpermanentdata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> cleanPermanentDataActivity(@PathVariable String id) {
    return masterApiSpaceControllerManager.cleanLiveActivityPermanentData(id);
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
    return masterApiSpaceControllerManager.deleteLiveActivity(id);
  }
}
