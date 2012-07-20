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

package interactivespaces.controller.activity.wrapper.internal.osnative;

import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.activity.wrapper.ActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.ActivityWrapper;
import interactivespaces.controller.domain.InstalledLiveActivity;

/**
 * A {@link ActivityWrapperFactory} for running native apps.
 * 
 * @author Keith M. Hughes
 */
public class NativeActivityWrapperFactory implements
		ActivityWrapperFactory {

	@Override
	public String getActivityType() {
		return "native";
	}

	@Override
	public ActivityWrapper createActivityWrapper(
			InstalledLiveActivity liapp,
			ActivityFilesystem activityFilesystem,
			Configuration configuration, SpaceController controller) {
		return new NativeActivityWrapper();
	}

}
