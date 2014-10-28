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

import interactivespaces.master.api.messages.MasterApiMessageSupport;
import interactivespaces.master.api.messages.MasterApiMessages;
import interactivespaces.master.ui.internal.web.BaseActiveSpaceMasterController;

import com.google.common.collect.Lists;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.Map;

/**
 * A controller for Interactive Spaces space controller operations.
 *
 * @author Keith M. Hughes
 */
@Controller
public class SpaceControllerController extends BaseActiveSpaceMasterController {

  /**
   * Display a list of all controllers.
   *
   * @return model and view for controller list display
   */
  @RequestMapping("/spacecontroller/all.html")
  public ModelAndView listControllers() {
    Map<String, Object> response = masterApiSpaceControllerManager.getSpaceControllerAllView();

    ModelAndView mav = getModelAndView();
    mav.setViewName("spacecontroller/SpaceControllerViewAll");
    mav.addObject("spacecontrollers", response.get(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_DATA));

    return mav;
  }

  @RequestMapping(value = "/spacecontroller/{id}/view.html", method = RequestMethod.GET)
  public ModelAndView viewController(@PathVariable String id) {
    Map<String, Object> response = masterApiSpaceControllerManager.getSpaceControllerFullView(id);

    ModelAndView mav = getModelAndView();

    if (MasterApiMessageSupport.isSuccessResponse(response)) {
      mav.setViewName("spacecontroller/SpaceControllerView");
      mav.addAllObjects(MasterApiMessageSupport.getResponseDataMap(response));
    } else {
      mav.setViewName("spacecontroller/SpaceControllerNonexistent");
    }

    return mav;
  }

  @RequestMapping(value = "/spacecontroller/{id}/delete.html", method = RequestMethod.GET)
  public ModelAndView deleteController(@PathVariable String id) {
    ModelAndView mav = getModelAndView();
    Map<String, Object> response = masterApiSpaceControllerManager.deleteSpaceController(id);

    if (MasterApiMessageSupport.isSuccessResponse(response)) {
      mav.clear();
      mav.setViewName("redirect:/spacecontroller/all.html");
    } else if (MasterApiMessageSupport.isResponseReason(response,
        MasterApiMessages.MESSAGE_SPACE_DOMAIN_CONTROLLER_UNKNOWN)) {
      mav.setViewName("spacecontroller/SpaceControllerNonexistent");
    }

    return mav;
  }

  @RequestMapping(value = "/spacecontroller/all.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> listAllControllersJson() {
    return masterApiSpaceControllerManager.getSpaceControllerAllView();
  }

  @RequestMapping(value = "/spacecontroller/{id}/view.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> viewControllerJson(@PathVariable String id) {
    return masterApiSpaceControllerManager.getSpaceControllerView(id);
  }

  @RequestMapping(value = "/spacecontroller/{id}/connect.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> connectController(@PathVariable String id) {
    return masterApiSpaceControllerManager.connectToSpaceControllers(Collections.singletonList(id));
  }

  @RequestMapping(value = "/spacecontroller/{id}/disconnect.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> disconnectSpaceController(@PathVariable String id) {
    return masterApiSpaceControllerManager.disconnectFromSpaceControllers(Collections.singletonList(id));
  }

  @RequestMapping(value = "/spacecontroller/{id}/cleantmpdata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> cleanTempData(@PathVariable String id) {
    return masterApiSpaceControllerManager.cleanSpaceControllerTempData(id);
  }

  @RequestMapping(value = "/spacecontroller/{id}/cleanactivitiestmpdata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> cleanActivitiesTempData(@PathVariable String id) {
    return masterApiSpaceControllerManager.cleanSpaceControllerActivitiesTempData(id);
  }

  @RequestMapping(value = "/spacecontroller/{id}/cleanpermanentdata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> cleanPermanentData(@PathVariable String id) {
    return masterApiSpaceControllerManager.cleanSpaceControllerPermanentData(id);
  }

  @RequestMapping(value = "/spacecontroller/{id}/cleanactivitiespermanentdata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> cleanActivitiesPermanentData(@PathVariable String id) {
    return masterApiSpaceControllerManager.cleanSpaceControllerActivitiesPermanentData(id);
  }

  @RequestMapping(value = "/spacecontroller/{id}/restoredata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> restoreData(@PathVariable String id) {
    return masterApiSpaceControllerManager.restoreSpaceControllerDataBundle(id);
  }

  @RequestMapping(value = "/spacecontroller/{id}/capturedata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> captureData(@PathVariable String id) {
    return masterApiSpaceControllerManager.captureSpaceControllerDataBundle(id);
  }

  @RequestMapping(value = "/spacecontroller/{id}/shutdown.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> shutdownController(@PathVariable String id) {
    return masterApiSpaceControllerManager.shutdownSpaceControllers(Lists.newArrayList(id));
  }

  @RequestMapping(value = "/spacecontroller/{id}/activities/shutdown.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> shutdownAllAppsController(@PathVariable String id) {
    return masterApiSpaceControllerManager.shutdownAllActivities(id);
  }

  @RequestMapping(value = "/spacecontroller/{id}/status.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> statusController(@PathVariable String id) {
    return masterApiSpaceControllerManager.statusSpaceControllers(Collections.singletonList(id));
  }

  @RequestMapping(value = "/spacecontroller/{id}/deploy.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> deployLiveActivities(@PathVariable String id) {
    return masterApiSpaceControllerManager.deployAllSpaceControllerActivityInstances(id);
  }

  @RequestMapping(value = "/spacecontroller/all/connect.html", method = RequestMethod.GET)
  public String connectAllControllers() {
    masterApiSpaceControllerManager.connectToAllSpaceControllers();

    return "redirect:/spacecontroller/all.html";
  }

  @RequestMapping(value = "/spacecontroller/all/disconnect.html", method = RequestMethod.GET)
  public String disconnectAllControllers() {
    masterApiSpaceControllerManager.disconnectFromAllSpaceControllers();

    return "redirect:/spacecontroller/all.html";
  }

  @RequestMapping(value = "/spacecontroller/all/shutdown.html", method = RequestMethod.GET)
  public String shutdownAllControllers() {
    masterApiSpaceControllerManager.shutdownAllSpaceControllers();

    return "redirect:/spacecontroller/all.html";
  }

  @RequestMapping(value = "/spacecontroller/all/cleantmpdata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> cleanTempDataAllControllers() {
    return masterApiSpaceControllerManager.cleanSpaceControllerTempDataAllSpaceControllers();
  }

  @RequestMapping(value = "/spacecontroller/all/cleanactivitiestmpdata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> cleanActivitiesTempDataAllControllers() {
    return masterApiSpaceControllerManager.cleanSpaceControllerActivitiesTempDataAllSpaceControllers();
  }

  @RequestMapping(value = "/spacecontroller/all/cleanpermanentdata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> cleanPermanentDataAllControllers() {
    return masterApiSpaceControllerManager.cleanSpaceControllerPermanentDataAllSpaceControllers();
  }

  @RequestMapping(value = "/spacecontroller/all/cleanactivitiespermanentdata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> cleanActivitiesPermanentDataAllControllers() {
    return masterApiSpaceControllerManager.cleanSpaceControllerActivitiesPermanentDataAllSpaceControllers();
  }

  @RequestMapping(value = "/spacecontroller/all/capturedata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> captureDataAllControllers() {
    return masterApiSpaceControllerManager.captureDataAllSpaceControllers();
  }

  @RequestMapping(value = "/spacecontroller/all/restoredata.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> restoreDataAllControllers() {
    return masterApiSpaceControllerManager.restoreDataAllSpaceControllers();
  }

  @RequestMapping(value = "/spacecontroller/all/status.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> statusAllControllers() {
    return masterApiSpaceControllerManager.statusFromAllSpaceControllers();
  }

  @RequestMapping(value = "/spacecontroller/all/forcestatus.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> forceStatusAllControllers() {
    return masterApiSpaceControllerManager.forceStatusFromAllSpaceControllers();
  }

  @RequestMapping(value = "/spacecontroller/all/activities/shutdown.html", method = RequestMethod.GET)
  public String shutdownAllActivitiesAllControllers() {
    masterApiSpaceControllerManager.shutdownAllActivitiesAllSpaceControllers();

    return "redirect:/spacecontroller/all.html";
  }
}
