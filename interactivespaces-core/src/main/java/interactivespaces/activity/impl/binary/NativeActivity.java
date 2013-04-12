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

package interactivespaces.activity.impl.binary;

import interactivespaces.activity.Activity;
import interactivespaces.activity.binary.NativeActivityRunner;
import interactivespaces.activity.component.binary.NativeActivityComponent;
import interactivespaces.activity.configuration.ActivityConfiguration;
import interactivespaces.activity.impl.BaseActivity;

/**
 * An {@link Activity} which will just start up a native application from
 * information int the configuration file.
 * 
 * @author Keith M. Hughes
 */
public class NativeActivity extends BaseActivity {

	@Override
	public void commonActivitySetup() {
		addActivityComponent(new NativeActivityComponent(
				ActivityConfiguration.CONFIGURATION_ACTIVITY_EXECUTABLE,
				ActivityConfiguration.CONFIGURATION_ACTIVITY_EXECUTABLE_FLAGS));
	}
}
