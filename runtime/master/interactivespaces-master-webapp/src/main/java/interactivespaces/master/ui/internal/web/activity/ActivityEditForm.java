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
import interactivespaces.domain.support.ActivityUtils;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

/**
 * A form for editing activities.
 *
 * @author Keith M. Hughes
 */
@Controller
@RequestMapping("/activity/{id}/edit")
@SessionAttributes({ "activity", "id" })
public class ActivityEditForm extends BaseSpaceMasterController {

  /**
   * The activity repository.
   */
  private ActivityRepository activityRepository;

  @InitBinder
  public void setAllowedFields(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
  }

  @RequestMapping(method = RequestMethod.GET)
  public String setupForm(@PathVariable("id") String id, Model model) {
    Activity activity = activityRepository.getActivityById(id);
    model.addAttribute("activity", ActivityUtils.toTemplate(activity));
    model.addAttribute("id", id);

    addGlobalModelItems(model);

    return "activity/ActivityEdit";
  }

  @RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
  public String processSubmit(@PathVariable("id") String id,
      @ModelAttribute("activity") Activity template, BindingResult result, SessionStatus status) {
    new ActivityValidator().validate(template, result);
    if (result.hasErrors()) {
      return "activity/ActivityEdit";
    } else {
      Activity activity = activityRepository.getActivityById(id);
      ActivityUtils.copy(template, activity);
      activityRepository.saveActivity(activity);

      status.setComplete();

      return "redirect:/activity/" + activity.getId() + "/view.html";
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