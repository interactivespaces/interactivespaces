/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.example.activity.routable.output.web;

import java.util.Map;

import interactivespaces.activity.impl.web.BaseRoutableRosWebActivity;

/**
 * An Interactive Spaces activity which provides a routable output web example.
 * 
 * <p>
 * Buttons pushed in the web app will cause a message to be sent on the output
 * route.
 * 
 * @author Keith M. Hughes
 */
public class RoutableOutputWebExampleActivity extends
		BaseRoutableRosWebActivity {

	@Override
	public void onWebSocketReceive(String connectionId, Object d) {
		getLog().info("Got web socket data from connection " + connectionId);

		@SuppressWarnings("unchecked")
		Map<String, Object> data = (Map<String, Object>) d;
		getLog().info(data);
		if (isActivated()) {
			// In this example, just pass though the message as it came across.
			// This is not always the best choice.
			sendOutputJson("output1", data);
		}
	}
}
