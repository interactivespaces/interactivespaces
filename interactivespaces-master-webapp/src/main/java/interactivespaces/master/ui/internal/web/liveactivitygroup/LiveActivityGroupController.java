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

import interactivespaces.master.api.master.MasterApiMessageSupport;
import interactivespaces.master.api.messages.MasterApiMessages;
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
 * Spring MVC controller for activity groups.
 *
 * @author Keith M. Hughes
 */
@Controller
public class LiveActivityGroupController extends BaseActiveSpaceMasterController {

  /**
   * Display a list of all activities.
   *
   * @return Model and view for controller list display.
   */
  @RequestMapping("/liveactivitygroup/all.html")
  public ModelAndView listActivities() {
    Map<String, Object> response = masterApiActivityManager.getLiveActivityGroupsByFilter(null);

    ModelAndView mav = getModelAndView();
    mav.setViewName("liveactivitygroup/LiveActivityGroupViewAll");
    mav.addObject("liveactivitygroups", response.get(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_DATA));

    return mav;
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/view.html", method = RequestMethod.GET)
  public ModelAndView viewActivityGroup(@PathVariable String id) {
    ModelAndView mav = getModelAndView();

    Map<String, Object> response = masterApiActivityManager.getLiveActivityGroupFullView(id);
    if (MasterApiMessageSupport.isSuccessResponse(response)) {
      mav.setViewName("liveactivitygroup/LiveActivityGroupView");

      mav.addAllObjects(MasterApiMessageSupport.getResponseDataMap(response));
    } else {
      mav.setViewName("liveactivitygroup/LiveActivityGroupNonexistent");
    }

    return mav;
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/view.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> viewActivityGroupJson(@PathVariable String id) {
    return masterApiActivityManager.getLiveActivityGroupView(id);
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/liveactivitystatus.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> statusActivityGroup(@PathVariable String id) {
    return masterApiSpaceControllerManager.statusLiveActivityGroup(id);
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/deploy.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> deployActivityGroup(@PathVariable String id) {
    return masterApiSpaceControllerManager.deployLiveActivityGroup(id);
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/configure.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> configureActivityGroup(@PathVariable String id) {
    return masterApiSpaceControllerManager.configureLiveActivityGroup(id);
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/startup.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> startupActivityGroup(@PathVariable String id) {
    return masterApiSpaceControllerManager.startupLiveActivityGroup(id);
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/activate.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> activateActivityGroup(@PathVariable String id) {
    return masterApiSpaceControllerManager.activateLiveActivityGroup(id);
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/deactivate.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> deactivateActivityGroup(@PathVariable String id) {
    return masterApiSpaceControllerManager.deactivateLiveActivityGroup(id);
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/shutdown.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> shutdownLiveActivityGroup(@PathVariable String id) {
    return masterApiSpaceControllerManager.shutdownLiveActivityGroup(id);
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/forceshutdownliveactivities.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> forceShutdownLiveActivityLiveActivityGroup(@PathVariable String id) {
    return masterApiSpaceControllerManager.forceShutdownLiveActivitiesLiveActivityGroup(id);
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/metadata.json", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, ? extends Object> modifyLiveActivityGroupMetadata(@PathVariable String id,
      @RequestBody Object metadataCommand, HttpServletResponse response) {
    return masterApiActivityManager.updateMetadataLiveActivityGroup(id, metadataCommand);
  }

  @RequestMapping(value = "/liveactivitygroup/all.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> listAllGroupsJson(@RequestParam(value = "filter", required = false) String filter) {
    return masterApiActivityManager.getLiveActivityGroupsByFilter(filter);
  }

  @RequestMapping(value = "/liveactivitygroup/{id}/delete.html", method = RequestMethod.GET)
  public String deleteActivityGroup(@PathVariable String id) {
    masterApiActivityManager.deleteLiveActivityGroup(id);

    return "redirect:/liveactivitygroup/all.html";
  }
}
