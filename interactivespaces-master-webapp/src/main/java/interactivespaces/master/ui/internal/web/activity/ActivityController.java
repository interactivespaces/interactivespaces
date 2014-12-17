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

import interactivespaces.master.api.messages.MasterApiMessageSupport;
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
 * Spring MVC controller for activity operations.
 *
 * @author Keith M. Hughes
 */
@Controller
public class ActivityController extends BaseActiveSpaceMasterController {

  /**
   * Display a list of all activities.
   *
   * @return Model and view for controller list display.
   */
  @RequestMapping("/activity/all.html")
  public ModelAndView listActivities() {
    Map<String, Object> response = masterApiActivityManager.getActivitiesByFilter(null);

    ModelAndView mav = getModelAndView();

    mav.setViewName("activity/ActivityViewAll");
    mav.addObject("activities", response.get(MasterApiMessages.MASTER_API_MESSAGE_ENVELOPE_DATA));

    return mav;
  }

  @RequestMapping(value = "/activity/{id}/view.html", method = RequestMethod.GET)
  public ModelAndView viewActivity(@PathVariable String id) {
    ModelAndView mav = getModelAndView();

    Map<String, Object> response = masterApiActivityManager.getActivityFullView(id);
    if (MasterApiMessageSupport.isSuccessResponse(response)) {
      mav.setViewName("activity/ActivityView");
      mav.addAllObjects(MasterApiMessageSupport.getResponseDataMap(response));
    } else {
      mav.setViewName("activity/ActivityNonexistent");
    }

    return mav;
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

  @RequestMapping(value = "/activity/{id}/view.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> viewActivityJson(@PathVariable String id) {
    return masterApiActivityManager.getActivityFullView(id);
  }

  @RequestMapping(value = "/activity/{id}/deploy.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> deployActivities(@PathVariable String id) {
    return masterApiSpaceControllerManager.deployAllLiveActivityInstances(id);
  }

  @RequestMapping(value = "/activity/{id}/metadata.json", method = RequestMethod.POST)
  public @ResponseBody
  Map<String, ? extends Object> modifyLiveActivityMetadata(@PathVariable String id,
      @RequestBody Object metadataCommandObj, HttpServletResponse response) {
    return masterApiActivityManager.updateActivityMetadata(id, metadataCommandObj);
  }
}
