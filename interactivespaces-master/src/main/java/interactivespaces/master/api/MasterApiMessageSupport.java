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

package interactivespaces.master.api;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * A set of methods and constants to provide uniform Master API support.
 *
 * @author Keith M. Hughes
 */
public class MasterApiMessageSupport {

  /**
   * Field in the Master API response giving the result.
   */
  public static final String MASTER_API_FIELD_RESULT = "result";

  /**
   * The result given for a successful Master API call.
   */
  public static final String MASTER_API_RESULT_SUCCESS = "success";

  /**
   * The result given for a failed Master API call.
   */
  public static final String MASTER_API_RESULT_FAILURE = "failure";

  /**
   * Field in the Master API response giving the reason for a response.
   */
  public static final String MASTER_API_FIELD_REASON = "reason";

  /**
   * The field in a Master API command map giving the name of the command field.
   */
  public static final String MASTER_API_PARAMETER_COMMAND = "command";

  /**
   * The field in a Master API command map giving the name of the data field.
   */
  public static final String MASTER_API_PARAMETER_DATA = "data";

  /**
   * The JSON metadata command for replacing all of the metadata.
   */
  public static final String MASTER_API_COMMAND_METADATA_REPLACE = "replace";

  /**
   * The JSON metadata command for modifying the metadata.
   */
  public static final String MASTER_API_COMMAND_METADATA_MODIFY = "modify";

  /**
   * The JSON metadata command for seleting some of the metadata.
   */
  public static final String MASTER_API_COMMAND_METADATA_DELETE = "delete";

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
   * Get the simple version of a Master API success response.
   *
   * @return a success API response
   */
  public static Map<String, Object> getSimpleSuccessResponse() {
    Map<String, Object> response = Maps.newHashMap();

    response.put(MASTER_API_FIELD_RESULT, MASTER_API_RESULT_SUCCESS);

    return response;
  }

  /**
   * Get a Master API success response with data.
   *
   * @param data
   *          the data field for the response
   *
   * @return a success Master API response with data
   */
  public static Map<String, Object> getSuccessResponse(Object data) {
    Map<String, Object> response = getSimpleSuccessResponse();

    response.put("data", data);

    return response;
  }

  /**
   * Get a failure Master API response.
   *
   * @param reason
   *          the reason for the failure
   *
   * @return the Master API response object
   */
  public static Map<String, Object> getFailureResponse(String reason) {
    Map<String, Object> result = Maps.newHashMap();
    result.put(MASTER_API_FIELD_RESULT, MASTER_API_RESULT_FAILURE);
    result.put(MASTER_API_FIELD_REASON, reason);

    return result;
  }

  /**
   * Is the response a success response?
   *
   * @param response
   *          a Master API response
   *
   * @return {@code true} if the response was a success
   */
  public static boolean isSuccessResponse(Map<String, Object> response) {
    return MASTER_API_RESULT_SUCCESS.equals(response.get(MASTER_API_FIELD_RESULT));
  }

  /**
   * Was the reason for the response the reason given?
   *
   * @param response
   *          the Master API response
   * @param reason
   *          the reason given
   *
   * @return {@code true} if the reason given is the reason in the response
   */
  public static boolean isResponseReason(Map<String, Object> response, String reason) {
    return reason.equals(response.get(MASTER_API_FIELD_REASON));
  }
}
