/*
 * Copyright (C) 2015 Google Inc.
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

import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.InteractiveSpacesDomainCloner;
import interactivespaces.master.server.services.StandardInteractiveSpacesDomainCloner;
import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;
import interactivespaces.master.ui.internal.web.SimpleCloneForm;
import interactivespaces.master.ui.internal.web.SimpleCloneFormValidator;

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
 * A form for cloning live activities.
 *
 * @author Keith M. Hughes
 */
@Controller
@RequestMapping("/liveactivity/{id}/clone")
@SessionAttributes({ "form", "id", "liveactivityname" })
public class LiveActivityCloneForm extends BaseSpaceMasterController {

  /**
   * The activity repository.
   */
  private ActivityRepository activityRepository;

  @InitBinder
  public void initBinder(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
    dataBinder.setDisallowedFields("liveactivityname");
  }

  @RequestMapping(method = RequestMethod.GET)
  public String setupForm(@PathVariable("id") String id, Model model) {
    LiveActivity liveActivity = activityRepository.getLiveActivityById(id);

    SimpleCloneForm form = new SimpleCloneForm();

    model.addAttribute("form", form);
    model.addAttribute("id", id);
    model.addAttribute("liveactivityname", liveActivity.getName());

    addGlobalModelItems(model);

    return "liveactivity/LiveActivityClone";
  }

  @RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
  public String processSubmit(@PathVariable("id") String id, @ModelAttribute("form") SimpleCloneForm form,
      BindingResult result, SessionStatus status, Model model) {
    new SimpleCloneFormValidator().validate(form, result);
    if (result.hasErrors()) {
      return "liveactivity/LiveActivityClone";
    } else {
      // TODO(keith): Move into Master API
      LiveActivity liveActivity = activityRepository.getLiveActivityById(id);

      InteractiveSpacesDomainCloner cloner = new StandardInteractiveSpacesDomainCloner(activityRepository);
      cloner.setNamePrefix(form.getNamePrefix());
      cloner.cloneLiveActivity(liveActivity);
      cloner.saveClones();
      LiveActivity clone = cloner.getClonedLiveActivity(liveActivity.getId());

      status.setComplete();

      return "redirect:/liveactivity/" + clone.getId() + "/view.html";
    }
  }

  /**
   * Set the activity repository.
   *
   * @param activityRepository
   *          the activity repository
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }
}
