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

package interactivespaces.master.ui.internal.web;


import java.util.List;
import java.util.Map;

import org.springframework.validation.Errors;

import com.google.common.collect.Maps;

/**
 * Support base class for metadata editing classes.
 *
 * @author Keith M. Hughes
 */
public abstract class MetadataEditFormSupport extends BaseSpaceMasterController {

	/**
	 * Create a configuration form with the values of the metadata
	 * parameters in it.
	 * 
	 * @param metadata
	 *            the metadata in the form
	 * 
	 * @return a filled out form object
	 */
	protected ConfigurationForm newMetadataForm(Map<String, Object> metadata) {
		ConfigurationForm metadataForm = new ConfigurationForm();
	
		if (metadata != null) {
			List<LabeledValue> configParameters = UiUtilities
					.getMetadataView(metadata);
	
			StringBuilder builder = new StringBuilder();
			for (LabeledValue parameter : configParameters) {
				builder.append(parameter.getLabel()).append("=")
						.append(parameter.getValue()).append('\n');
			}
	
			metadataForm.setValues(builder.toString());
		} else {
			metadataForm.setValues("");
		}
	
		return metadataForm;
	}

	/**
	 * Get a map of the submitted parameters.
	 * 
	 * @param form
	 *            the form
	 * 
	 * @return a map of the metadata names to values
	 */
	protected Map<String, Object> getSubmittedMap(ConfigurationForm form) {
		Map<String, Object> map = Maps.newHashMap();
	
		String[] lines = form.getValues().split("\n");
	
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty())
				continue;
	
			int pos = line.indexOf('=');
			map.put(new String(line.substring(0, pos).trim()), new String(line
					.substring(pos + 1).trim()));
		}
		return map;
	}
}
