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

package interactivespaces.example.activity.routable.output;

import java.util.Map;

import com.google.common.collect.Maps;

import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;

/**
 * A simple Interactive Spaces Java-based activity for writing to a route.
 */
public class SimpleJavaRoutableOutputActivity extends BaseRoutableRosActivity {

	@Override
	public void onActivityActivate() {
		Map<String, Object> message = Maps.newHashMap();
		message.put("message", "yipee! activated!");
		sendOutputJson("output1", message);
	}

	@Override
	public void onActivityDeactivate() {
		Map<String, Object> message = Maps.newHashMap();
		message.put("message", "bummer! deactivated!");
		sendOutputJson("output1", message);
	}
}
