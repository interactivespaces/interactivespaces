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

package interactivespaces.master.server.ui;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A set of methods and constants to provide uniform JSON support.
 * 
 * @author Keith M. Hughes
 */
public class JsonSupport {

	/**
	 * The field in a JSON command map giving the name of the command field.
	 */
	public static final String JSON_PARAMETER_COMMAND = "command";

	/**
	 * The field in a JSON command map giving the name of the data field.
	 */
	public static final String JSON_PARAMETER_DATA = "data";

	/**
	 * Message key for unknown commands.
	 */
	public static final String MESSAGE_SPACE_COMMAND_UNKNOWN = "space.command.unknown";

	/**
	 * Message key for a call failing.
	 */
	public static final String MESSAGE_SPACE_CALL_FAILURE = "space.command.failure";

	/**
	 * Message key for a call failing.
	 */
	public static final String MESSAGE_SPACE_CALL_ARGS_NOMAP = "space.command.args.notmap";

	/**
	 * Get the simple version of a JSON success response.
	 * 
	 * @return a success JSON object with no data
	 */
	public static Map<String, Object> getSimpleSuccessJsonResponse() {
		Map<String, Object> response = Maps.newHashMap();

		response.put("result", "success");

		return response;
	}

	/**
	 * Get ta JSON success response with the data field filled in.
	 * 
	 * @return a success JSON object with data in the "data" field
	 */
	public static Map<String, Object> getSuccessJsonResponse(Object data) {
		Map<String, Object> response = getSimpleSuccessJsonResponse();

		response.put("data", data);

		return response;
	}

	/**
	 * Get a failure JSON response.
	 * 
	 * @param reason
	 *            the reason for the failure.
	 * 
	 * @return the JSON response object
	 */
	public static Map<String, Object> getFailureJsonResponse(String reason) {
		Map<String, Object> result = Maps.newHashMap();
		result.put("result", "failure");
		result.put("reason", reason);

		return result;
	}
}
