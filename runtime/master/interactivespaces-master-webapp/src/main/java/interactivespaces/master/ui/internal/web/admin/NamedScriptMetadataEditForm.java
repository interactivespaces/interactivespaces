/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.master.ui.internal.web.admin;

import interactivespaces.domain.system.NamedScript;
import interactivespaces.master.api.master.MasterApiAutomationManager;
import interactivespaces.master.server.services.AutomationRepository;
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
 * A form for editing named script metadata.
 *
 * @author Keith M. Hughes
 */
@Controller
@RequestMapping("/admin/namedscript/{id}/metadata/edit")
@SessionAttributes({ "script", "id", "metadata" })
public class NamedScriptMetadataEditForm extends MetadataEditFormSupport {

  /**
   * The repository for automation entities.
   */
  private AutomationRepository automationRepository;

  /**
   * The masterApi manager for automation operations.
   */
  private MasterApiAutomationManager masterApiAutomationManager;

  @InitBinder
  public void setAllowedFields(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
  }

  @RequestMapping(method = RequestMethod.GET)
  public String setupForm(@PathVariable("id") String id, Model model) {
    NamedScript script = automationRepository.getNamedScriptById(id);
    model.addAttribute("script", script);
    model.addAttribute("id", id);

    addGlobalModelItems(model);

    ConfigurationForm metadataForm = newMetadataForm(script.getMetadata());

    model.addAttribute("metadata", metadataForm);

    return "admin/NamedScriptMetadataEdit";
  }

  @RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
  public String processSubmit(@PathVariable("id") String id,
      @ModelAttribute("metadata") ConfigurationForm metadataForm, BindingResult result, SessionStatus status) {
    metadataForm.validate(result, false, "namedscript.metadata");
    if (result.hasErrors()) {
      return "admin/NamedScriptMetadataEdit";
    } else {
      NamedScript script = automationRepository.getNamedScriptById(id);

      if (saveMetadataForm(metadataForm, script)) {
        automationRepository.saveNamedScript(script);
      }

      status.setComplete();

      return "redirect:/admin/namedscript/" + id + "/view.html";
    }
  }

  /**
   * Save the metadata form
   *
   * @param form
   *          the metadata form
   * @param script
   *          the script which contains the metadata
   *
   * @return {@code true} if there were changes
   */
  private boolean saveMetadataForm(ConfigurationForm form, NamedScript script) {
    Map<String, Object> map = getSubmittedMap(form);

    return saveMetadata(script, map);
  }

  /**
   * Save the metadata.
   *
   * @param script
   *          the script being reconfigured
   * @param map
   *          the map of new configurations
   *
   * @return {@code true} if there was a change in the configuration
   */
  private boolean saveMetadata(NamedScript script, Map<String, Object> map) {
    Map<String, Object> metadata = script.getMetadata();
    if (metadata != null) {
      if (metadata.isEmpty() && map.isEmpty()) {
        return false;
      }

      script.setMetadata(map);

      return true;
    } else {
      // No configuration. If nothing in submission, nothing has changed.
      // Otherwise add everything.
      if (map.isEmpty())
        return false;

      script.setMetadata(map);

      return true;
    }
  }

  /**
   * Set the automation repository to use.
   *
   * @param automationRepository
   *          the automation repository
   */
  public void setAutomationRepository(AutomationRepository automationRepository) {
    this.automationRepository = automationRepository;
  }

  /**
   * Set the Master API automation manager to use.
   *
   * @param masterApiAutomationManager
   *          the Master API automation manager
   */
  public void setMasterApiAutomationManager(MasterApiAutomationManager masterApiAutomationManager) {
    this.masterApiAutomationManager = masterApiAutomationManager;
  }
}