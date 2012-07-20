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

package interactivespaces.workbench.activity.project.creator;

import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.activity.project.ActivityProjectCreationSpecification;

import java.io.File;
import java.util.Map;

/**
 * A project creator for java projects.
 * 
 * @author Keith M. Hughes
 */
public class JavascriptProjectSourceDescription implements ProjectSourceDescription {

	@Override
	public void process(ActivityProjectCreationSpecification spec,
			InteractiveSpacesWorkbench workbench,
			FreemarkerTemplater templater, Map<String, Object> templateData) {
		spec.getProject().setActivityType("script");

		spec.setExecutable("SimplePythonActivity.js");
		spec.addExtraConfigurationParameter("space.activity.log.level",
				InteractiveSpacesEnvironment.LOG_LEVEL_INFO);

		templater.writeTemplate(templateData, new File(spec.getProject().getActivityResourceDirectory(),
				"SimplePythonActivity.js"),
				"activity/generic/javascript/simple/SimpleJavascriptActivity.js.ftl");
	}
}
