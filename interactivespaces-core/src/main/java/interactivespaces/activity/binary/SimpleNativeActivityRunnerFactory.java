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

package interactivespaces.activity.binary;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.configuration.SystemConfiguration;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

/**
 * A factory for receiving native app launchers.
 * 
 * @author Keith M. Hughes
 */
public class SimpleNativeActivityRunnerFactory implements
		NativeActivityRunnerFactory {

	/**
	 * The Interactive Spaces environment being run under.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	public SimpleNativeActivityRunnerFactory(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

	@Override
	public NativeActivityRunner newPlatformNativeActivityRunner(Log log) {
		String os = spaceEnvironment.getSystemConfiguration()
				.getRequiredPropertyString(SystemConfiguration.PLATFORM_OS);

		if (LinuxNativeActivityRunner.OPERATING_SYSTEM_TAG.equals(os)) {
			return new LinuxNativeActivityRunner(spaceEnvironment, log);
		} else if (OsxNativeActivityRunner.OPERATING_SYSTEM_TAG.equals(os)) {
			return new OsxNativeActivityRunner(spaceEnvironment, log);
		} else if (WindowsNativeActivityRunner.OPERATING_SYSTEM_TAG.equals(os)) {
			return new WindowsNativeActivityRunner(spaceEnvironment, log);
		} else {
			throw new InteractiveSpacesException(
					"Cannot create native activity launcher. Unknown OS "
							+ os);
		}
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

}
