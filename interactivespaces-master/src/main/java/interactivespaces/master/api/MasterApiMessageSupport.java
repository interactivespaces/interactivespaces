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
   * Get the simple version of a Master API success response.
   *
   * @return a success API response
   */
  public static Map<String, Object> getSimpleSuccessResponse() {
    Map<String, Object> response = Maps.newHashMap();

    response.put(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_RESULT, MasterApiMessage.MASTER_API_RESULT_SUCCESS);

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
    result.put(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_RESULT, MasterApiMessage.MASTER_API_RESULT_FAILURE);
    result.put(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_REASON, reason);

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
    return MasterApiMessage.MASTER_API_RESULT_SUCCESS.equals(response.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_RESULT));
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
    return reason.equals(response.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_REASON));
  }

  /**
   * Get the data field as a map from a response object.
   *
   * @param response
   *          the response object
   *
   * @return the data map
   */
  public static Map<String, Object> getResponseDataMap(Map<String, Object> response) {
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) response.get(MasterApiMessage.MASTER_API_MESSAGE_ENVELOPE_DATA);
    return data;
  }
}
