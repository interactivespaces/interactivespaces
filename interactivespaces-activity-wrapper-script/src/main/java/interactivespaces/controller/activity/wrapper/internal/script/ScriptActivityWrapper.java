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

package interactivespaces.controller.activity.wrapper.internal.script;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.activity.wrapper.BaseActivityWrapper;
import interactivespaces.controller.activity.wrapper.ActivityWrapper;
import interactivespaces.service.script.ActivityScriptWrapper;
import interactivespaces.service.script.ScriptService;
import interactivespaces.service.script.FileScriptSource;

import java.io.File;

/**
 * An {@link ActivityWrapper} for scripted activities.
 * 
 * TODO(keith): Eventually retire this class and use scripting class directly.
 * 
 * @author Keith M. Hughes
 */
public class ScriptActivityWrapper extends BaseActivityWrapper {

	/**
	 * Wrapper around the script.
	 */
	private ActivityScriptWrapper scriptWrapper;

	public ScriptActivityWrapper(File scriptFile,
			ScriptService scriptService,
			ActivityFilesystem activityFilesystem,
			Configuration configuration) {
		if (!scriptFile.exists()) {
			throw new InteractiveSpacesException(String.format(
					"Script file %s does not exist",
					scriptFile.getAbsolutePath()));
		}

		String filename = scriptFile.getName();
		int periodPos = filename.lastIndexOf('.');
		String extension = filename.substring(periodPos + 1);
		String baseName = filename.substring(0, periodPos);

		scriptWrapper = scriptService.getActivityByExtension(extension,
				baseName, new FileScriptSource(scriptFile),
				activityFilesystem, configuration);
	}

	@Override
	public void destroy() {
		scriptWrapper.destroy();
	}

	@Override
	public Activity newInstance() {
		return scriptWrapper.newInstance();
	}

	@Override
	public ActivityExecutionContext newExecutionContext() {
		return scriptWrapper.newExecutionContext();
	}
}
