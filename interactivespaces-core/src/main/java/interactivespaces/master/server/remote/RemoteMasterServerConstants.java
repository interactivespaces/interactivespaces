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

package interactivespaces.master.server.remote;

import interactivespaces_msgs.MasterServerData;

/**
 * Useful constants for the master server.
 *
 * @author Keith M. Hughes
 */
public class RemoteMasterServerConstants {

	/**
	 * The topic name for the master server topic.
	 */
	public static final String MASTER_SERVER_TOPIC_NAME = "/interactivespaces/master";

	/**
	 * The topic message type for the master server topic.
	 */
	public static final String MASTER_SERVER_TOPIC_MESSAGE_TYPE = MasterServerData._TYPE;

	/**
	 * The topic message type for the master server controller description topic.
	 */
	public static final String MASTER_SERVER_CONTROLLER_DESCRIPTION_MESSAGE_TYPE = "interactivespaces_msgs/ControllerDescription";
}
