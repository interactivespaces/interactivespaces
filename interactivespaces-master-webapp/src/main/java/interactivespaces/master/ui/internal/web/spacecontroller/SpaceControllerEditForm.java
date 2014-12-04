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

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.support.SpaceControllerUtils;
import interactivespaces.master.server.services.SpaceControllerRepository;
import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;
import interactivespaces.master.ui.internal.web.WebSupport;

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
 * A form for editing controllers.
 *
 * @author Keith M. Hughes
 */
@org.springframework.stereotype.Controller
@RequestMapping("/spacecontroller/{id}/edit")
@SessionAttributes({ "spacecontroller", "id" })
public class SpaceControllerEditForm extends BaseSpaceMasterController {

  /**
   * The space controller repository.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * Set the allowed fields for the given data binder.
   *
   * @param dataBinder
   *          binder to set allowed fields for
   */
  @InitBinder
  public void setAllowedFields(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
  }

  /**
   * Setup a GET method form.
   *
   * @param id
   *          id of the controller
   * @param model
   *          data model
   *
   * @return form continuation
   */
  @RequestMapping(method = RequestMethod.GET)
  public String setupForm(@PathVariable("id") String id, Model model) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
    model.addAttribute("spacecontroller", SpaceControllerUtils.toTemplate(controller));
    model.addAttribute("id", id);
    addNeededEntities(model);

    addGlobalModelItems(model);

    return "spacecontroller/SpaceControllerEdit";
  }

  /**
   * Process form submit request.
   *
   * @param id
   *          id of the controller
   * @param template
   *          data template to process
   * @param result
   *          binding result
   * @param status
   *          the session status
   *
   * @return form continuation
   */
  @RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
  public String processSubmit(@PathVariable("id") String id,
      @ModelAttribute("spacecontroller") SpaceController template, BindingResult result, SessionStatus status,
      Model model) {
    new SpaceControllerValidator().validate(template, result);
    if (result.hasErrors()) {
      addNeededEntities(model);
      return "spacecontroller/SpaceControllerEdit";
    } else {
      SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
      SpaceControllerUtils.copy(template, controller);
      spaceControllerRepository.saveSpaceController(controller);

      status.setComplete();

      return "redirect:/spacecontroller/" + controller.getId() + "/view.html";
    }
  }

  /**
   * Add any additional needed entities to the model.
   *
   * @param model
   *          the model
   */
  private void addNeededEntities(Model model) {
    model.addAttribute("modes", WebSupport.getControllerModes(messageSource, locale));
  }

  /**
   * Set the space controller repository to use.
   *
   * @param spaceControllerRepository
   *          the space controller repository
   */
  public void setSpaceControllerRepository(SpaceControllerRepository spaceControllerRepository) {
    this.spaceControllerRepository = spaceControllerRepository;
  }
}
