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

package interactivespaces.master.ui.internal.web.liveactivitygroup;

import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.master.server.services.ActivityRepository;
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

/**
 * A form for editing live activity groups.
 * 
 * @author Keith M. Hughes
 */
@Controller
@RequestMapping("/liveactivitygroup/{id}/edit")
@SessionAttributes({ "form", "id" })
public class LiveActivityGroupEditForm {

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
		LiveActivityGroup group = activityRepository
				.getLiveActivityGroupById(id);
		LiveActivityGroupForm form = new LiveActivityGroupForm();
		form.copyLiveActivityGroup(group);
		
		model.addAttribute("form", form);
		model.addAttribute("id", id);
		addNeededEntities(model);

		return "liveactivitygroup/LiveActivityGroupEdit";
	}

	@RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
	public String processSubmit(@PathVariable("id") String id,
			@ModelAttribute("form") LiveActivityGroupForm form,
			BindingResult result, SessionStatus status, Model model) {
		new LiveActivityGroupFormValidator().validate(form, result);
		if (result.hasErrors()) {
			addNeededEntities(model);
			return "liveactivitygroup/LiveActivityGroupEdit";
		} else {
			LiveActivityGroup group = activityRepository
					.getLiveActivityGroupById(id);
			form.saveLiveActivityGroup(group, activityRepository);
			activityRepository.saveLiveActivityGroup(group);

			status.setComplete();

			return "redirect:/liveactivitygroup/" + group.getId()
					+ "/view.html";
		}
	}

	/**
	 * Get any entities needed by the form that will be too heavyweight in the
	 * session.
	 * 
	 * @param model
	 *            the model to put the values in
	 */
	private void addNeededEntities(Model model) {
		model.addAttribute("liveactivities", WebSupport
				.getLiveActivitySelections(activityRepository.getAllLiveActivities()));
	}

	/**
	 * @param activityRepository
	 *            the activityRepository to set
	 */
	public void setActivityRepository(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}
}