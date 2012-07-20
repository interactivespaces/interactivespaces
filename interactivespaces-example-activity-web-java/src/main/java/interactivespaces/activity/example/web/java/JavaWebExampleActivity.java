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

package interactivespaces.activity.example.web.java;

import interactivespaces.activity.impl.web.BaseWebActivity;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A sample Interactive Spaces Java-based activity.
 * 
 * @author Keith M. Hughes
 * @since Ap4 4, 2012
 */
public class JavaWebExampleActivity extends BaseWebActivity {

	@Override
	public void onActivityStartup() {
		getLog().info("Java Web Example Activity startup");
	}

	@Override
	public void onActivityActivate() {
		getLog().info("Java Web Example Activity activate");

		sendImageUrl("images/Geoffrey.jpg");
	}

	@Override
	public void onActivityDeactivate() {
		getLog().info("Java Web Example Activity deactivate");

		sendImageUrl("images/Lion.jpg");
	}


	private void sendImageUrl(String imageUrl) {
		Map<String, Object> data = Maps.newHashMap();
		data.put("imageUrl", imageUrl);
		Map<String, Object> msg = Maps.newHashMap();
		msg.put("data", data);

		// Send data to all websocket connections
		sendAllWebSocketJson(data);
	}

	@Override
	public void onActivityShutdown() {
		getLog().info("Java Web Example Activity shutdown");
	}

	@Override
	public void onNewWebSocketConnection(String connectionId) {
		getLog().info(
				"Got web socket connection from connection " + connectionId);
	}

	@Override
	public void onWebSocketClose(String connectionId) {
		getLog().info("Got web socket close from connection " + connectionId);
	}

	@Override
	public void onWebSocketReceive(String connectionId, Object d) {
		getLog().info("Got web socket data from connection " + connectionId);
		
		Map<String, Object> data = (Map<String, Object>)d;
		getLog().info(data);
	}
}
