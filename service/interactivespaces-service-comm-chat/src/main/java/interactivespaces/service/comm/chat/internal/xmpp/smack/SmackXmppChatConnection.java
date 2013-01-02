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

package interactivespaces.service.comm.chat.internal.xmpp.smack;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.comm.chat.ChatConnection;
import interactivespaces.service.comm.chat.ChatConnectionListener;

import java.util.List;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import com.google.common.collect.Lists;

/**
 *  A {@link ChatConnection} for XMPP using the Smack library.
 * 
 * @author Keith M. Hughes
 */
public class SmackXmppChatConnection implements ChatConnection {

	/**
	 * Default chat host for Google talk.
	 */
	public static final String CHAT_HOST_DEFAULT = "talk.google.com";

	/**
	 * Default chat port for Google talk.
	 */
	public static final int CHAT_PORT_DEFAULT = 5222;

	/**
	 * Default chat service name for Google talk.
	 */
	public static final String CHAT_SERVICE_NAME_DEFAULT = "gmail.com";

	/**
	 * Username for the connection.
	 */
	private String username;

	/**
	 * Password for the connection.
	 */
	private String password;

	/**
	 * The listeners for the connection.
	 */
	private List<ChatConnectionListener> listeners = Lists.newArrayList();

	/**
	 * The chat connection.
	 */
	private Connection connection;

	/**
	 * Host for the chat server.
	 */
	private String chatHost;

	/**
	 * Port for the chat server.
	 */
	private int chatPort;

	/**
	 * service name for the chat connection.
	 */
	private String chatServiceName;

	public SmackXmppChatConnection(String username, String password) {
		this(username, password, CHAT_HOST_DEFAULT, CHAT_PORT_DEFAULT,
				CHAT_SERVICE_NAME_DEFAULT);
	}

	public SmackXmppChatConnection(String username, String password,
			String chatHost, int chatPort, String chatServiceName) {
		this.username = username;
		this.password = password;
		this.chatHost = chatHost;
		this.chatPort = chatPort;
		this.chatServiceName = chatServiceName;
	}

	@Override
	public void startup() {
		ConnectionConfiguration config = new ConnectionConfiguration(
				chatHost, chatPort, chatServiceName);
		SASLAuthentication.supportSASLMechanism("PLAIN", 0);
		try {
			connection = new XMPPConnection(config);
			connection.connect();

			connection.login(username, password);

			connection.addPacketListener(new PacketListener() {

				@Override
				public void processPacket(Packet packet) {
					onProcessPacket(packet);
				}
			}, null);
		} catch (Exception e) {
			throw new InteractiveSpacesException(
					"Could not make XMPP connection", e);
		}
	}

	@Override
	public void shutdown() {
		if (connection != null) {
			connection.disconnect();
			connection = null;
		}
	}

	@Override
	public String getUser() {
		return connection.getUser();
	}

	@Override
	public void sendMessage(String to, String message) {
		Message m = new Message();
		m.addBody("en", message);
		m.setFrom(connection.getUser());
		m.setTo(to);

		connection.sendPacket(m);
	}

	@Override
	public void addListener(ChatConnectionListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(ChatConnectionListener listener) {
		listeners.remove(listener);
	}

	/**
	 * A new packet has come in. Process it.
	 * 
	 * @param packet
	 *            the packet to process
	 */
	public void onProcessPacket(Packet packet) {
		System.out.println("Got XMPP packet " + packet);
		if (packet instanceof Message) {
			Message m = (Message) packet;

			String from = m.getFrom();
			String message = m.getBody();

			for (ChatConnectionListener listener : listeners) {
				listener.onMessage(this, from, message);
			}
		}
	}
}
