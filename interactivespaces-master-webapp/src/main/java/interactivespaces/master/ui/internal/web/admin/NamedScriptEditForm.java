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

package interactivespaces.master.ui.internal.web.admin;

import interactivespaces.domain.support.AutomationUtils;
import interactivespaces.domain.system.NamedScript;
import interactivespaces.domain.system.pojo.SimpleNamedScript;
import interactivespaces.master.server.services.AutomationRepository;
import interactivespaces.master.server.ui.UiAutomationManager;
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
 * A form for editing named scripts.
 * 
 * @author Keith M. Hughes
 */
@Controller
@RequestMapping("/admin/namedscript/{id}/edit")
@SessionAttributes({"script", "id"})
public class NamedScriptEditForm extends BaseSpaceMasterController {

	/**
	 * The repository for automation entities.
	 */
	private AutomationRepository automationRepository;

	/**
	 * The ui manager for automation operations.
	 */
	private UiAutomationManager uiAutomationManager;

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(@PathVariable("id") String id, Model model) {
		NamedScript controller = automationRepository.getNamedScriptById(id);
		model.addAttribute("script", AutomationUtils.toTemplate(controller));
		model.addAttribute("id", id);
        
        addGlobalModelItems(model);

		return "admin/NamedScriptEdit";
	}

	@RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
	public String processSubmit(@PathVariable("id") String id, 
			@ModelAttribute("script") SimpleNamedScript template,
			BindingResult result, SessionStatus status) {
		new NamedScriptValidator().validate(template, result);
		if (result.hasErrors()) {
			return "admin/NamedScriptEdit";
		} else {
			uiAutomationManager.updateNamedScript(id, template);
			
			status.setComplete();
			
			return "redirect:/admin/namedscript/" + id + "/view.html";
		}
	}

	/**
	 * @param automationRepository
	 *            the automationRepository to set
	 */
	public void setAutomationRepository(
			AutomationRepository automationRepository) {
		this.automationRepository = automationRepository;
	}

	/**
	 * @param uiAutomationManager the uiAutomationManager to set
	 */
	public void setUiAutomationManager(UiAutomationManager uiAutomationManager) {
		this.uiAutomationManager = uiAutomationManager;
	}
}