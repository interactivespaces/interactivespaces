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

import interactivespaces.domain.space.Space;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;
import interactivespaces.master.ui.internal.web.WebSupport;

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

import java.util.Map;

/**
 * A form for editing spaces.
 *
 * @author Keith M. Hughes
 */
@Controller
@RequestMapping("/space/{id}/edit")
@SessionAttributes({ "form", "id" })
public class SpaceEditForm extends BaseSpaceMasterController {

  /**
   * The activity repository.
   */
  private ActivityRepository activityRepository;

  @InitBinder
  public void initBinder(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
  }

  @RequestMapping(method = RequestMethod.GET)
  public String setupForm(@PathVariable("id") String id, Model model) {
    Space space = activityRepository.getSpaceById(id);
    SpaceForm form = new SpaceForm();
    form.copySpace(space);

    model.addAttribute("form", form);
    model.addAttribute("id", id);

    addGlobalModelItems(model);

    addNeededEntities(model, space);

    return "space/SpaceEdit";
  }

  @RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
  public String processSubmit(@PathVariable("id") String id,
      @ModelAttribute("form") SpaceForm form, BindingResult result, SessionStatus status,
      Model model) {
    Space space = activityRepository.getSpaceById(id);
    new SpaceValidator().validate(space, form, result);
    if (result.hasErrors()) {
      addNeededEntities(model, space);
      return "space/SpaceEdit";
    } else {
      form.saveSpace(space, activityRepository);
      activityRepository.saveSpace(space);

      status.setComplete();

      return "redirect:/space/" + space.getId() + "/view.html";
    }
  }

  /**
   * Get any entities needed by the form that will be too heavyweight in the
   * session.
   *
   * @param model
   *          the model to put the values in
   * @param space
   *          the current space
   */
  private void addNeededEntities(Model model, Space space) {
    Map<String, String> spaceSelections =
        WebSupport.getSpaceSelections(activityRepository.getAllSpaces());
    spaceSelections.remove(space.getId());
    model.addAttribute("spaces", spaceSelections);

    model.addAttribute("liveactivitygroups",
        WebSupport.getLiveActivityGroupSelections(activityRepository.getAllLiveActivityGroups()));
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }
}
