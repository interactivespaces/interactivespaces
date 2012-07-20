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
import interactivespaces.master.server.services.ControllerRepository;

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
@SessionAttributes({"spacecontroller", "id"})
public class SpaceControllerEditForm {

	/**
	 * The controller repository.
	 */
	private ControllerRepository controllerRepository;

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(@PathVariable("id") String id, Model model) {
		SpaceController controller = controllerRepository.getSpaceControllerById(id);
		model.addAttribute("spacecontroller", SpaceControllerUtils.toTemplate(controller));
		model.addAttribute("id", id);

		return "spacecontroller/SpaceControllerEdit";
	}

	@RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
	public String processSubmit(@PathVariable("id") String id, 
			@ModelAttribute("spacecontroller") SpaceController template,
			BindingResult result, SessionStatus status) {
		new SpaceControllerValidator().validate(template, result);
		if (result.hasErrors()) {
			return "spacecontroller/SpaceControllerEdit";
		} else {
			SpaceController controller = controllerRepository.getSpaceControllerById(id);
			SpaceControllerUtils.copy(template, controller);
			controllerRepository.saveSpaceController(controller);
			
			status.setComplete();
			
			return "redirect:/spacecontroller/" + controller.getId() + "/view.html";
		}
	}

	/**
	 * @param controllerRepository
	 *            the controllerRepository to set
	 */
	public void setControllerRepository(
			ControllerRepository controllerRepository) {
		this.controllerRepository = controllerRepository;
	}
}