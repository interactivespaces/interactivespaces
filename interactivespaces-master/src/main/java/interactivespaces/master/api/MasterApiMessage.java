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

package interactivespaces.master.api;

/**
 * Message components for the Master API.
 *
 * @author Keith M. Hughes
 */
public class MasterApiMessage {

  /**
   * The type field for a websocket message envelope.
   */
  public static final String MASTER_API_MESSAGE_ENVELOPE_TYPE = "type";

  /**
   * The data field for a websocket message envelope.
   */
  public static final String MASTER_API_MESSAGE_ENVELOPE_DATA = "data";

  /**
   * Field in the Master API response giving the request ID.
   */
  public static final String MASTER_API_MESSAGE_ENVELOPE_REQUEST_ID = "requestId";

  /**
   * Field in the Master API response giving the result.
   */
  public static final String MASTER_API_MESSAGE_ENVELOPE_RESULT = "result";

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
  public static final String MASTER_API_MESSAGE_ENVELOPE_REASON = "reason";

  /**
   * The field in a Master API command map giving the name of the command field.
   */
  public static final String MASTER_API_PARAMETER_COMMAND = "command";

  /**
   * Web socket command for getting view data for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_VIEW = "/liveactivity/view";

  /**
   * Web socket command for deploying a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_DEPLOY = "/liveactivity/deploy";

  /**
   * Web socket command for configuring a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURE = "/liveactivity/configure";

  /**
   * Web socket command for getting configuration data for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURATION_GET = "/liveactivity/configuration/get";

  /**
   * Web socket command for setting configuration data for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURATION_SET = "/liveactivity/configuration/set";

  /**
   * Web socket command for setting metadata for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_METADATA_SET = "/liveactivity/metadata/set";

  /**
   * Web socket command for starting up a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_STARTUP = "/liveactivity/startup";

  /**
   * Web socket command for activating a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_ACTIVATE = "/liveactivity/activate";

  /**
   * Web socket command for deactivating a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_DEACTIVATE = "/liveactivity/deactivate";

  /**
   * Web socket command for shutting down a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_SHUTDOWN = "/liveactivity/shutdown";

  /**
   * Web socket command for getting the status for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_STATUS = "/liveactivity/status";

  /**
   * Web socket command for deleting the master's entry for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_DELETE_LOCAL = "/liveactivity/delete/local";

  /**
   * Web socket command for deleting a live activity from its controller.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_DELETE_REMOTE = "/liveactivity/delete/remote";

  /**
   * Web socket command for getting view data for a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_VIEW = "/liveactivitygroup/view";

  /**
   * Web socket command for deploying a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DEPLOY = "/liveactivitygroup/deploy";

  /**
   * Web socket command for configuring a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_CONFIGURE = "/liveactivitygroup/configure";

  /**
   * Web socket command for setting metadata for a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_METADATA_SET = "/liveactivitygroup/metadata/set";

  /**
   * Web socket command for starting up a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_STARTUP = "/liveactivitygroup/startup";

  /**
   * Web socket command for activating a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_ACTIVATE = "/liveactivitygroup/activate";

  /**
   * Web socket command for deactivating a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DEACTIVATE = "/liveactivitygroup/deactivate";

  /**
   * Web socket command for shutting down a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_SHUTDOWN = "/liveactivitygroup/shutdown";

  /**
   * Web socket command for getting the status for a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_STATUS = "/liveactivitygroup/status";

  /**
   * Web socket command for getting view data for a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_VIEW = "/space/view";

  /**
   * Web socket command for deploying a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_DEPLOY = "/space/deploy";

  /**
   * Web socket command for configuring a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONFIGURE = "/space/configure";

  /**
   * Web socket command for setting metadata for a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_METADATA_SET = "/space/metadata/set";

  /**
   * Web socket command for starting up a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_STARTUP = "/space/startup";

  /**
   * Web socket command for activating a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_ACTIVATE = "/space/activate";

  /**
   * Web socket command for deactivating a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_DEACTIVATE = "/space/deactivate";

  /**
   * Web socket command for shutting down a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_SHUTDOWN = "/space/shutdown";

  /**
   * Web socket command for getting the status for a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_STATUS = "/space/status";

  /**
   * Web socket command for running a named script.
   */
  public static final String MASTER_API_COMMAND_NAMEDSCRIPT_RUN = "/admin/namedscript/run";

  /**
   * Parameter name for the ID of the entity in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_ENTITY_ID = "id";

  /**
   * Status parameter name for the UUID of the entity in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_ENTITY_UUID = "uuid";

  /**
   * Message type for a command response.
   */
  public static final String MASTER_API_MESSAGE_TYPE_COMMAND_RESPONSE = "commandResponse";

  /**
   * Message type for a status update.
   */
  public static final String MASTER_API_MESSAGE_TYPE_STATUS_UPDATE = "statusUpdate";

  /**
   * Status parameter name for the status time in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_STATUS_TIME = "statusTime";

  /**
   * Status parameter name for the status in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_STATUS_RUNTIME_STATE = "runtimeState";

  /**
   * Status parameter name for the status in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_STATUS_RUNTIME_STATE_DESCRIPTION = "runtimeStateDescription";

  /**
   * Status parameter name for the detailed status in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_STATUS_DETAIL = "runtimeStateDetail";

  /**
   * Status parameter name for the type of the entity in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_STATUS_TYPE = "type";

  /**
   * Status parameter value if the entity type is a live activity.
   */
  public static final String MASTER_API_PARAMETER_VALUE_TYPE_STATUS_LIVE_ACTIVITY = "liveactivity";

  /**
   * The prefix for using an extension.
   */
  public static final String MASTER_API_COMMAND_EXTENSION_PREFIX = "/extension/";

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
   * Message key for non-existent activities.
   */
  public static final String MESSAGE_SPACE_DOMAIN_ACTIVITY_UNKNOWN = "space.domain.activity.unknown";

  /**
   * Message key for non-existent live activities.
   */
  public static final String MESSAGE_SPACE_DOMAIN_LIVEACTIVITY_UNKNOWN = "space.domain.liveactivity.unknown";

  /**
   * Message key for non-existent live activity groups.
   */
  public static final String MESSAGE_SPACE_DOMAIN_LIVEACTIVITYGROUP_UNKNOWN = "space.domain.liveactivitygroup.unknown";

  /**
   * Message key for non-existent spaces.
   */
  public static final String MESSAGE_SPACE_DOMAIN_SPACE_UNKNOWN = "space.domain.space.unknown";

  /**
   * Message key for non-existent controllers.
   */
  public static final String MESSAGE_SPACE_DOMAIN_CONTROLLER_UNKNOWN = "space.domain.controller.unknown";
}
