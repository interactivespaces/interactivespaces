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

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * A validator for {@link LiveActivityGroup} instances.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityGroupFormValidator implements Validator {

	@Override
	@SuppressWarnings("unchecked")
	public boolean supports(Class<?> clazz) {
		return LiveActivityGroupForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object obj, Errors errors) {
		LiveActivityGroupForm form = (LiveActivityGroupForm) obj;

		String name = form.getLiveActivityGroup().getName();
		if (!StringUtils.hasLength(name)) {
			errors.rejectValue("liveActivityGroup.name", "required", "required");
		}
	}
}
