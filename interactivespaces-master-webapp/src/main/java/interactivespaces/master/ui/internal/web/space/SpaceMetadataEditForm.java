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
import interactivespaces.master.ui.internal.web.ConfigurationForm;
import interactivespaces.master.ui.internal.web.MetadataEditFormSupport;

import java.util.Map;

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
 * A form for editing space metadata.
 * 
 * @author Keith M. Hughes
 */
@Controller
@RequestMapping("/space/{id}/metadata/edit")
@SessionAttributes({ "space", "id", "metadata" })
public class SpaceMetadataEditForm extends MetadataEditFormSupport {

	/**
	 * The space repository.
	 */
	private ActivityRepository activityRepository;

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@RequestMapping(method = RequestMethod.GET)
	public String setupForm(@PathVariable("id") String id, Model model) {
		Space space = activityRepository.getSpaceById(id);
		model.addAttribute("space", space);
		model.addAttribute("id", id);

		ConfigurationForm metadataForm = newMetadataForm(space.getMetadata());

		model.addAttribute("metadata", metadataForm);

		return "space/SpaceMetadataEdit";
	}

	@RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
	public String processSubmit(@PathVariable("id") String id,
			@ModelAttribute("config") ConfigurationForm configurationForm,
			BindingResult result, SessionStatus status) {
		validate(configurationForm, result);
		if (result.hasErrors()) {
			return "space/SpaceMetadataEdit";
		} else {
			Space space = activityRepository.getSpaceById(id);

			if (saveMetadataForm(configurationForm, space)) {
				activityRepository.saveSpace(space);
			}

			status.setComplete();

			return "redirect:/space/" + id + "/view.html";
		}
	}

	/**
	 * Save the metadata form
	 * 
	 * @param form
	 *            the metadata form
	 * @param space
	 *            the space which contains the metadata
	 * 
	 * @return {@code true} if there were changes
	 */
	private boolean saveMetadataForm(ConfigurationForm form, Space space) {
		return saveMetadata(space, form.getSubmittedMap());
	}

	/**
	 * save the metadata.
	 * 
	 * @param space
	 *            the space being reconfigured
	 * @param map
	 *            the map of new configurations
	 * 
	 * @return {@code true} if there was a change in the configuration
	 */
	private boolean saveMetadata(Space space, Map<String, Object> map) {
		Map<String, Object> metadata = space.getMetadata();
		if (metadata != null) {
			if (metadata.isEmpty() && map.isEmpty()) {
				return false;
			}

			space.setMetadata(map);

			return true;
		} else {
			// No configuration. If nothing in submission, nothing has changed.
			// Otherwise add everything.
			if (map.isEmpty())
				return false;

			space.setMetadata(map);

			return true;
		}
	}

	/**
	 * @param activityRepository
	 *            the activityRepository to set
	 */
	public void setActivityRepository(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}
}
