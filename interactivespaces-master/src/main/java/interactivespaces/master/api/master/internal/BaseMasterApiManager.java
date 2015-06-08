/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.master.api.master.internal;

import interactivespaces.expression.ExpressionFactory;
import interactivespaces.master.api.messages.MasterApiMessageSupport;
import interactivespaces.master.api.messages.MasterApiMessages;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.Map;

/**
 * A base manager with support for Master API managers.
 *
 * @author Keith M. Hughes
 */
public class BaseMasterApiManager {

  /**
   * A factory for expressions.
   */
  protected ExpressionFactory expressionFactory;

  /**
   * The space environment to use.
   */
  protected InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Log a response error.
   *
   * @param errorPreamble
   *          the error preamble
   * @param response
   *          the API response
   */
  protected void logResponseError(String errorPreamble, Map<String, Object> response) {
    spaceEnvironment.getLog().error(errorPreamble + "\n" + MasterApiMessageSupport.getResponseDetail(response));
  }

  /**
   * Get the Master API response for no such activity.
   *
   * @param id
   *          the ID of the activity
   *
   * @return the API response
   */
  protected Map<String, Object> getNoSuchActivityResponse(String id) {
    return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_DOMAIN_ACTIVITY_UNKNOWN,
        String.format("Unknown activity %s", id));
  }

  /**
   * Get the Master API response for no such live activity.
   *
   * @param id
   *          the ID of the live activity
   *
   * @return the API response
   */
  protected Map<String, Object> getNoSuchLiveActivityResponse(String id) {
    return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_DOMAIN_LIVEACTIVITY_UNKNOWN,
        String.format("Unknown live activity %s", id));
  }

  /**
   * Get the Master API response for no such live activity group.
   *
   * @param id
   *          the ID of the live activity group
   *
   * @return the API response
   */
  protected Map<String, Object> getNoSuchLiveActivityGroupResponse(String id) {
    return MasterApiMessageSupport.getFailureResponse(
        MasterApiMessages.MESSAGE_SPACE_DOMAIN_LIVEACTIVITYGROUP_UNKNOWN,
        String.format("Unknown live activity group %s", id));
  }

  /**
   * Get a no such space API response.
   *
   * @param id
   *          the ID of the space
   *
   * @return a no such space API response
   */
  protected Map<String, Object> getNoSuchSpaceResponse(String id) {
    return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_DOMAIN_SPACE_UNKNOWN,
        String.format("Unknown space %s", id));
  }

  /**
   * Get a master API response for no such space controller.
   *
   * @param id
   *          the ID for the group
   *
   * @return the API response
   */
  protected Map<String, Object> getNoSuchSpaceControllerResponse(String id) {
    return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_DOMAIN_CONTROLLER_UNKNOWN,
        String.format("Unknown space controller %s", id));
  }

  /**
   * Set the expression factory for this manager.
   *
   * @param expressionFactory
   *          the factory to use
   */
  public void setExpressionFactory(ExpressionFactory expressionFactory) {
    this.expressionFactory = expressionFactory;
  }

  /**
   * Set the space environment to use.
   *
   * @param spaceEnvironment
   *          the space environment to use
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
