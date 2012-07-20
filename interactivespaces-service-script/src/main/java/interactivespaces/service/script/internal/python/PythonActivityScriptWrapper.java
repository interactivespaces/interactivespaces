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

package interactivespaces.service.script.internal.python;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.script.ActivityScriptWrapper;
import interactivespaces.service.script.ScriptActivityExecutionContext;
import interactivespaces.service.script.ScriptSource;

import java.io.File;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 * An {@link ActivityScriptWrapper} for python scripts.
 * 
 * @author Keith M. Hughes
 */
public class PythonActivityScriptWrapper implements ActivityScriptWrapper {

	/**
	 * Name of the object being extracted.
	 */
	private String objectName;

	/**
	 * The contents of the script.
	 */
	private ScriptSource scriptSource;

	/**
	 * File system for the activity
	 */
	private ActivityFilesystem activityFilesystem;

	/**
	 * Configuration for the activity script.
	 */
	private Configuration configuration;

	public PythonActivityScriptWrapper(String objectName,
			ScriptSource scriptSource, ActivityFilesystem activityFilesystem,
			Configuration configuration) {
		this.objectName = objectName;
		this.scriptSource = scriptSource;
		this.activityFilesystem = activityFilesystem;
		this.configuration = configuration;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public Activity newInstance() {
		try {
			PythonInterpreter interp = PythonInterpreter
					.threadLocalStateInterpreter(null);

			// If script dir in the activity install, include at beginning of
			// path.
			File scriptDir = new File(activityFilesystem.getInstallDirectory(),
					"lib/python");
			if (scriptDir.exists()) {
				if (scriptDir.canRead()) {
					PySystemState systemState = interp.getSystemState();
					systemState.path.add(0,
							Py.newString(scriptDir.getAbsolutePath()));
				}
			}

			String script = scriptSource.getScriptContents();
			interp.exec(script);

			PyObject cls = interp.get(objectName);
			if (cls == null)
				throw new InteractiveSpacesException(String.format(
						"No callable class named %s in %s", objectName, script));

			PyObject processingUnit = cls.__call__();
			Object o = processingUnit.__tojava__(Activity.class);
			if (o == Py.NoConversion)
				throw new InteractiveSpacesException(String.format(
						"The value with name %s must implement %s", objectName,
						Activity.class.getName()));

			return (Activity) o;
		} catch (PyException e) {
			e.printStackTrace();
			throw new InteractiveSpacesException(
					"Could not create Jython Interactive Spaces activity ", e);
		}
	}

	@Override
	public ActivityExecutionContext newExecutionContext() {
		return new ScriptActivityExecutionContext(
				PythonActivityScriptWrapper.class.getClassLoader());
	}
}
