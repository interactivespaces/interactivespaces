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

package interactivespaces.activity.impl.remote;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A remote activity.
 * 
 * <p>
 * The endpoint is picked up from the
 * 
 * @author Keith M. Hughes
 */
public class RemoteActivity {
	private static final int PORT_DEFAULT = 9000;

	/**
	 * Whether the server is running or not.
	 */
	private boolean running = false;

	/**
	 * The port the server is listening on.
	 */
	private int port = PORT_DEFAULT;

	/**
	 * The socker the server is listening on for incoming client requests.
	 */
	private ServerSocket serverSocket;

	public void start() {
		new Thread(new Runnable() {
			public void run() {
				try {
					serverSocket = new ServerSocket(port);

					running = true;
					while (!Thread.interrupted() && running) {
						System.out.println("Waiting for client");
						Socket clientSocket = serverSocket.accept();

						// handleClient(clientSocket);
					}

					serverSocket.close();
				} catch (IOException e) {
					System.out
							.println("robotWorldServer: Caught IO Error in main dispatch.");
				} finally {
					serverSocket = null;
				}
			}
		}).start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hideho.robotworld.controller.server.RobotWorldServer#stop()
	 */
	public void stop() {
		if (running) {
			// A possibly inelegant way of shutting the server down.
			// The server may be blocked at the serverSocket.accept()
			// method, so nothing will make it unblock.
			//
			// If we close it, it will unblock.
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("Error when stopping: " + e.getMessage());
			}
			running = false;
		}
	}

}
