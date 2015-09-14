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
import interactivespaces.master.server.services.SpaceControllerRepository;
import interactivespaces.master.ui.internal.web.ConfigurationForm;
import interactivespaces.master.ui.internal.web.MetadataEditFormSupport;

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
 * A form for editing space controller metadata.
 *
 * @author Keith M. Hughes
 */
@Controller
@RequestMapping("/spacecontroller/{id}/metadata/edit")
@SessionAttributes({ "spacecontroller", "id", "metadata" })
public class SpaceControllerMetadataEditForm extends MetadataEditFormSupport {

  /**
   * The Space Controller repository.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * Set the fields which allowed and which are disallowed.
   *
   * @param dataBinder
   *          the data binder
   */
  @InitBinder
  public void setAllowedFields(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
  }

  /**
   * Set up the editing form.
   *
   * @param id
   *          ID for the space controller
   * @param model
   *          the data model
   *
   * @return the name of the page to show
   */
  @RequestMapping(method = RequestMethod.GET)
  public String setupForm(@PathVariable("id") String id, Model model) {
    SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);
    model.addAttribute("spacecontroller", controller);
    model.addAttribute("id", id);

    addGlobalModelItems(model);

    ConfigurationForm metadataForm = newMetadataForm(controller.getMetadata());

    model.addAttribute("metadata", metadataForm);

    return "spacecontroller/SpaceControllerMetadataEdit";
  }

  /**
   * Process the form submission.
   *
   * @param id
   *          ID of the space controller
   * @param metadataForm
   *          the form
   * @param result
   *          the result from the post
   * @param status
   *          the session status
   *
   * @return the page to show
   */
  @RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
  public String processSubmit(@PathVariable("id") String id,
      @ModelAttribute("meatadata") ConfigurationForm metadataForm, BindingResult result, SessionStatus status) {
    metadataForm.validate(result, false, "space.metadata");
    if (result.hasErrors()) {
      return "spacecontroller/SpaceControllerMetadataEdit";
    } else {
      SpaceController controller = spaceControllerRepository.getSpaceControllerById(id);

      if (saveMetadataForm(metadataForm, controller)) {
        spaceControllerRepository.saveSpaceController(controller);
      }

      status.setComplete();

      return "redirect:/spacecontroller/" + id + "/view.html";
    }
  }

  /**
   * Save the metadata form.
   *
   * @param form
   *          the metadata form
   * @param controller
   *          the space controller which contains the metadata
   *
   * @return {@code true} if there were changes
   */
  private boolean saveMetadataForm(ConfigurationForm form, SpaceController controller) {
    return saveMetadata(controller, form.getSubmittedMap());
  }

  /**
   * Save the metadata.
   *
   * @param controller
   *          the space controller being reconfigured
   * @param map
   *          the map of new configurations
   *
   * @return {@code true} if there was a change in the configuration
   */
  private boolean saveMetadata(SpaceController controller, Map<String, Object> map) {
    Map<String, Object> metadata = controller.getMetadata();
    if (metadata != null) {
      if (metadata.isEmpty() && map.isEmpty()) {
        return false;
      }

      controller.setMetadata(map);

      return true;
    } else {
      // No configuration. If nothing in submission, nothing has changed.
      // Otherwise add everything.
      if (map.isEmpty()) {
        return false;
      }

      controller.setMetadata(map);

      return true;
    }
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
