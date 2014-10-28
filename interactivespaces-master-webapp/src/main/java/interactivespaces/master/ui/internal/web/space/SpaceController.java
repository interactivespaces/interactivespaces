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
 * A Spring MVC controller for spaces.
 *
 * @author Keith M. Hughes
 */
@Controller
public class SpaceController extends BaseActiveSpaceMasterController {

  /**
   * Display a list of all spaces.
   *
   * @return model and view for space list display
   */
  @RequestMapping("/space/all.html")
  public ModelAndView listActivities() {
    ModelAndView mav = getModelAndView();
    mav.setViewName("space/SpaceViewAll");

    Map<String, Object> result = masterApiActivityManager.getSpacesByFilter(null);
    mav.addObject("spaces", result.get(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_DATA));

    return mav;
  }

  @RequestMapping(value = "/space/{id}/view.html", method = RequestMethod.GET)
  public ModelAndView viewSpace(@PathVariable String id) {

    Map<String, Object> response = masterApiActivityManager.getSpaceFullView(id);

    ModelAndView mav = getModelAndView();
    if (MasterApiMessageSupport.isSuccessResponse(response)) {
      mav.setViewName("space/SpaceView");
      mav.addAllObjects(MasterApiMessageSupport.getResponseDataMap(response));
    } else {
      mav.setViewName("space/SpaceNonexistent");
    }

    return mav;
  }

  @RequestMapping(value = "/space/{id}/delete.html", method = RequestMethod.GET)
  public String deleteSpace(@PathVariable String id) {
    masterApiActivityManager.deleteSpace(id);

    return "redirect:/space/all.html";
  }

  @RequestMapping(value = "/space/{id}/liveactivities.html", method = RequestMethod.GET)
  public ModelAndView viewSpaceLiveActivities(@PathVariable String id) {
    Map<String, Object> response = masterApiActivityManager.getSpaceLiveActivityGroupView(id);

    ModelAndView mav = getModelAndView();
    if (MasterApiMessageSupport.isSuccessResponse(response)) {
      mav.setViewName("space/SpaceViewLiveActivities");
      mav.addAllObjects(MasterApiMessageSupport.getResponseDataMap(response));
    } else {
      mav.setViewName("space/SpaceNonexistent");
    }

    return mav;
  }

  @RequestMapping(value = "/space/all.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> listAllSpacesJson(@RequestParam(value = "filter", required = false) String filter) {
    return masterApiActivityManager.getSpacesByFilter(filter);
  }

  @RequestMapping(value = "/space/{id}/view.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> viewSpaceJson(@PathVariable String id) {
    return masterApiActivityManager.getSpaceView(id);
  }

  @RequestMapping(value = "/space/{id}/status.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> statusSpaceJson(@PathVariable String id) {
    return masterApiSpaceControllerManager.statusSpace(id);
  }

  @RequestMapping(value = "/space/{id}/liveactivitystatus.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> statusSpaceLiveActivities(@PathVariable String id) {
    return masterApiSpaceControllerManager.liveActivityStatusSpace(id);
  }

  @RequestMapping(value = "/space/{id}/metadata.json", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, ? extends Object> modifySpaceGroupMetadata(@PathVariable String id,
      @RequestBody Object metadataCommand, HttpServletResponse response) {
    return masterApiActivityManager.updateMetadataSpace(id, metadataCommand);
  }

  @RequestMapping(value = "/space/{id}/load.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> loadSpace(@PathVariable String id) {
    // Should check code base to see if this can go.
    return MasterApiMessageSupport.getSimpleSuccessResponse();
  }

  @RequestMapping(value = "/space/{id}/deploy.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> deploySpace(@PathVariable String id) {
    return masterApiSpaceControllerManager.deploySpace(id);
  }

  @RequestMapping(value = "/space/{id}/configure.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> configureSpace(@PathVariable String id) {
    return masterApiSpaceControllerManager.configureSpace(id);
  }

  @RequestMapping(value = "/space/{id}/startup.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> startupSpace(@PathVariable String id) {
    return masterApiSpaceControllerManager.startupSpace(id);
  }

  @RequestMapping(value = "/space/{id}/activate.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> activateSpace(@PathVariable String id) {
    return masterApiSpaceControllerManager.activateSpace(id);
  }

  @RequestMapping(value = "/space/{id}/deactivate.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> deactivateSpace(@PathVariable String id) {
    return masterApiSpaceControllerManager.deactivateSpace(id);
  }

  @RequestMapping(value = "/space/{id}/shutdown.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> shutdownSpace(@PathVariable String id) {
    return masterApiSpaceControllerManager.shutdownSpace(id);
  }
}
