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

package interactivespaces.example.activity.chat.xmpp;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.comm.chat.ChatConnection;
import interactivespaces.service.comm.chat.ChatConnectionListener;
import interactivespaces.service.comm.chat.ChatService;

/**
 * An example activity for the chat service.
 * 
 * <p>
 * It echoes back messages to the sender if activated.
 * 
 * author Keith M. Hughes
 */
public class ExampleXmppChatActivity extends BaseActivity {

	/**
	 * Configuration property for getting the chat username.
	 */
	private static final String CONFIGURATION_PROPERTY_USERNAME = "activity.example.chat.xmpp.username";

	/**
	 * Configuration property for getting the chat password.
	 */
	private static final String CONFIGURATION_PROPERTY_PASSWORD = "activity.example.chat.xmpp.password";

	/**
	 * The connection to the chat server.
	 */
	private ChatConnection connection;

	@Override
	public void onActivitySetup() {
		ChatService chatService = getSpaceEnvironment().getServiceRegistry()
				.getRequiredService(ChatService.SERVICE_NAME);
		
		String username = getConfiguration().getRequiredPropertyString(CONFIGURATION_PROPERTY_USERNAME);
		String password = getConfiguration().getRequiredPropertyString(CONFIGURATION_PROPERTY_PASSWORD);

		connection = chatService.newChatConnection(username, password);
		
		connection.addListener(new ChatConnectionListener() {

			@Override
			public void onMessage(ChatConnection connection, String from, String message) {
				if (isActivated()) {
					connection.sendMessage(from, "You said: " + message);
				}
			} 
		});
		
		connection.connect();
	}

	@Override
	public void onActivityCleanup() {
		if (connection != null) {
			connection.shutdown();
			connection = null;
		}
	}
}
