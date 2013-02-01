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

package interactivespaces.controller.client.node;

/**
 * Request for a deletion of a live activity from a space controller.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerLiveActivityDeleteRequest {
	
	/**
	 * UUID of the live activity to be deleted.
	 */
	private String uuid;
	
	/**
	 * Idnetifying name of the live activity to delete.
	 */
	private String identifyingName;
	
	/**
	 * Version of the live activity to delete.
	 */
	private String version;

	public SpaceControllerLiveActivityDeleteRequest(String uuid, String identifyingName, String version) {
		this.uuid = uuid;
		this.identifyingName = identifyingName;
		this.version = version;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @return the identifyingName
	 */
	public String getIdentifyingName() {
		return identifyingName;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
}
