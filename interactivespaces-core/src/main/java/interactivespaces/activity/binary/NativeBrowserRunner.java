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

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivitySystemConfiguration;
import interactivespaces.configuration.Configuration;
import interactivespaces.util.process.restart.LimitedRetryRestartStrategy;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * A runner for native browsers.
 * 
 * @author Keith M. Hughes
 */
public class NativeBrowserRunner {

	/**
	 * Activity this browser is running under.
	 */
	private Activity activity;

	/**
	 * Launcher for the browser.
	 */
	public NativeActivityRunner browserRunner;

	public NativeBrowserRunner(Activity activity) {
		this.activity = activity;
	}

	/**
	 * Start up the browser.
	 * 
	 * @param initialUrl
	 *            URL the browser should start on.
	 * 
	 * @param debug
	 *            True if the browser should have a URL bar, etc, to make it
	 *            easier to debug.
	 */
	public void startup(String initialUrl, boolean debug) {
		browserRunner = activity.getController()
				.getNativeActivityRunnerFactory()
				.newPlatformNativeActivityRunner(activity.getLog());

		Configuration configuration = activity.getConfiguration();

		Map<String, Object> appConfig = new HashMap<String, Object>();
		appConfig.put(NativeActivityRunner.ACTIVITYNAME,
				ActivitySystemConfiguration
						.getActivityNativeBrowserBinary(configuration));

		String commandFlags = MessageFormat.format(ActivitySystemConfiguration
				.getActivityNativeBrowserCommandFlags(configuration, debug),
				initialUrl);

		appConfig.put(NativeActivityRunner.FLAGS, commandFlags);

		browserRunner.configure(appConfig);
		browserRunner.setRestartStrategy(new LimitedRetryRestartStrategy(4,
				1000, 4000, activity.getSpaceEnvironment()));

		browserRunner.startup();
	}

	/**
	 * Shut the browser down.
	 */
	public void shutdown() {
		if (browserRunner != null) {
			browserRunner.shutdown();

			browserRunner = null;
		}
	}

	/**
	 * Is the browser still running?
	 * 
	 * @return {@code true} if the browser is running
	 */
	public boolean isRunning() {
		if (browserRunner != null) {
			return browserRunner.isRunning();
		} else {
			return false;
		}
	}

}
