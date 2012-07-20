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

package interactivespaces.activity.component;

import interactivespaces.activity.component.binary.NativeActivityComponent;
import interactivespaces.activity.component.ros.RosActivityComponent;
import interactivespaces.activity.component.ros.RosMessageRouterActivityComponent;
import interactivespaces.activity.component.web.WebBrowserActivityComponent;
import interactivespaces.activity.component.web.WebServerActivityComponent;

/**
 * A {@link ActivityComponentFactory} which registers all the core activity
 * components to start with.
 * 
 * @author Keith M. Hughes
 */
public class CoreExistingActivityComponentFactory extends
		SimpleActivityComponentFactory {
	
	public CoreExistingActivityComponentFactory() {
		register(NativeActivityComponent.COMPONENT_NAME,
				NativeActivityComponent.class);
		register(RosActivityComponent.COMPONENT_NAME,
				RosActivityComponent.class);
		register(RosMessageRouterActivityComponent.COMPONENT_NAME,
				RosMessageRouterActivityComponent.class);
		register(WebBrowserActivityComponent.COMPONENT_NAME,
				WebBrowserActivityComponent.class);
		register(WebServerActivityComponent.COMPONENT_NAME,
				WebServerActivityComponent.class);
	}
}
