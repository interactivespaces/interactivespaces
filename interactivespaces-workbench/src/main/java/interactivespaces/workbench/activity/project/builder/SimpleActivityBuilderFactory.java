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

package interactivespaces.workbench.activity.project.builder;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.workbench.activity.project.ActivityProject;
import interactivespaces.workbench.activity.project.builder.java.JavaActivityBuilder;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A very simple {@link ActivityBuilderFactory}.
 * 
 * @author Keith M. Hughes
 */
public class SimpleActivityBuilderFactory implements ActivityBuilderFactory {

	/**
	 * Map of builder names to builder types.
	 */
	private Map<String, Class<? extends ActivityBuilder>> builderClasses = Maps
			.newHashMap();

	public SimpleActivityBuilderFactory() {
		builderClasses.put(NoopActivityBuilder.NAME, NoopActivityBuilder.class);
		builderClasses.put(JavaActivityBuilder.NAME, JavaActivityBuilder.class);
	}

	@Override
	public ActivityBuilder newBuilder(ActivityProject project) {
		Class<? extends ActivityBuilder> builderClass = builderClasses
				.get(NoopActivityBuilder.NAME);
		String builderType = project.getActivity().getBuilderType();
		if (builderType != null) {
			builderClass = builderClasses.get(builderType);
			if (builderClass == null) {
				throw new InteractiveSpacesException(String.format(
						"No builder found for type %s", builderType));
			}
		}

		try {
			return builderClass.newInstance();
		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Cannot create activity builder for type %s", builderType),
					e);
		}
	}
}
