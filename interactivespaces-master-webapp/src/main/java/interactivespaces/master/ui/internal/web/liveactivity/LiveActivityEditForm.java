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

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.support.LiveActivityUtils;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.ControllerRepository;
import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;
import interactivespaces.master.ui.internal.web.WebSupport;
import interactivespaces.master.ui.internal.web.editor.ActivityEditor;
import interactivespaces.master.ui.internal.web.editor.SpaceControllerEditor;

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
 * A form for editing lives. activities.
 *
 * @author Keith M. Hughes
 */
@Controller
@RequestMapping("/liveactivity/{id}/edit")
@SessionAttributes({ "liveactivity", "id" })
public class LiveActivityEditForm extends BaseSpaceMasterController {

  /**
   * The activity repository.
   */
  private ActivityRepository activityRepository;

  /**
   * The controller repository.
   */
  private ControllerRepository controllerRepository;

  @InitBinder
  public void initBinder(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
    dataBinder.setDisallowedFields("activities");
    dataBinder.setDisallowedFields("controllers");
    dataBinder.registerCustomEditor(Activity.class, new ActivityEditor(activityRepository));
    dataBinder.registerCustomEditor(SpaceController.class, new SpaceControllerEditor(
        controllerRepository));
  }

  @RequestMapping(method = RequestMethod.GET)
  public String setupForm(@PathVariable("id") String id, Model model) {
    LiveActivity liveactivity = activityRepository.getLiveActivityById(id);
    model.addAttribute("liveactivity", LiveActivityUtils.toTemplate(liveactivity));
    model.addAttribute("id", id);

    addGlobalModelItems(model);

    addNeededEntities(model);

    return "liveactivity/LiveActivityEdit";
  }

  @RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
  public String processSubmit(@PathVariable("id") String id,
      @ModelAttribute("liveactivity") LiveActivity template, BindingResult result,
      SessionStatus status, Model model) {
    new LiveActivityValidator().validate(template, result);
    if (result.hasErrors()) {
      addNeededEntities(model);
      return "liveactivity/LiveActivityEdit";
    } else {
      LiveActivity liveactivity = activityRepository.getLiveActivityById(id);
      LiveActivityUtils.copy(template, liveactivity);
      activityRepository.saveLiveActivity(liveactivity);

      status.setComplete();

      return "redirect:/liveactivity/" + liveactivity.getId() + "/view.html";
    }
  }

  /**
   * Get any entities needed by the form that will be too heavyweight in the
   * session.
   *
   * @param model
   *          the model to put the values in
   */
  private void addNeededEntities(Model model) {
    model.addAttribute("activities",
        WebSupport.getActivitySelections(activityRepository.getAllActivities()));
    model.addAttribute("controllers",
        WebSupport.getControllerSelections(controllerRepository.getAllSpaceControllers()));
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * @param controllerRepository
   *          the controllerRepository to set
   */
  public void setControllerRepository(ControllerRepository controllerRepository) {
    this.controllerRepository = controllerRepository;
  }
}