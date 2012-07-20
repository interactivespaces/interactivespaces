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

package interactivespaces.controller.activity.wrapper.internal.interactivespaces;

import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.configuration.ActivityConfiguration;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.activity.wrapper.ActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.ActivityWrapper;
import interactivespaces.controller.domain.InstalledLiveActivity;

import java.io.File;

import org.osgi.framework.BundleContext;

/**
 * A {@link ActivityWrapperFactory} for Interactive Spaces Native apps.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesNativeActivityWrapperFactory implements
		ActivityWrapperFactory {

	/**
	 * The bundle context for entrance into the OSGi container.
	 */
	private BundleContext bundleContext;

	public InteractiveSpacesNativeActivityWrapperFactory(
			BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	public String getActivityType() {
		return "interactivespaces_native";
	}

	@Override
	public ActivityWrapper createActivityWrapper(InstalledLiveActivity liapp,
			ActivityFilesystem activityFilesystem, Configuration configuration,
			SpaceController controller) {
		// TODO(keith): Need something which reference counts the OSGi bundle so
		// that
		// can have multiple instances of the same app running in the OSGi
		// container.
		// Will need to take versions into account.
		File executable = getActivityExecutable(liapp, activityFilesystem,
				configuration);

		InteractiveSpacesNativeActivityWrapper wrapper = new InteractiveSpacesNativeActivityWrapper(
				bundleContext, executable, configuration, controller
						.getSpaceEnvironment().getLog());

		return wrapper;
	}

	/**
	 * Get a file to the activity executable.
	 * 
	 * @param liapp
	 *            Activity containing the executable.
	 * @param activityFilesystem
	 *            The activity's filesystem.
	 * @param configuration
	 *            Configuration for the activity.
	 * 
	 * @return File containing the executable.
	 */
	private File getActivityExecutable(InstalledLiveActivity liapp,
			ActivityFilesystem activityFilesystem, Configuration configuration) {
		return new File(
				activityFilesystem.getInstallDirectory(),
				configuration
						.getRequiredPropertyString(ActivityConfiguration.CONFIGURATION_ACTIVITY_EXECUTABLE));
	}
}
