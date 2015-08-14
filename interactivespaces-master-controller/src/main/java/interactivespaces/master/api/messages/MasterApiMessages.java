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

package interactivespaces.master.api.messages;

/**
 * Message components for the Master API.
 *
 * @author Keith M. Hughes
 */
public class MasterApiMessages {

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
   * Field in the Master API response giving the detail for a response.
   */
  public static final String MASTER_API_MESSAGE_ENVELOPE_DETAIL = "detail";

  /**
   * The field in a Master API command map giving the name of the command field.
   */
  public static final String MASTER_API_PARAMETER_COMMAND = "command";

  /**
   * Prefix for activity commands.
   */
  public static final String MASTER_API_COMMAND_PREFIX_ACTIVITY = "/activity";

  /**
   * Web socket command for getting view data for all activities.
   */
  public static final String MASTER_API_COMMAND_ACTIVITY_ALL = MASTER_API_COMMAND_PREFIX_ACTIVITY + "/all";

  /**
   * Web socket command for getting view data for an activity.
   */
  public static final String MASTER_API_COMMAND_ACTIVITY_VIEW = MASTER_API_COMMAND_PREFIX_ACTIVITY + "/view";

  /**
   * Web socket command for getting full view data for an activity.
   */
  public static final String MASTER_API_COMMAND_ACTIVITY_VIEW_FULL = MASTER_API_COMMAND_ACTIVITY_VIEW + "/FULL";

  /**
   * Web socket command for updating all deployments of an activity.
   */
  public static final String MASTER_API_COMMAND_ACTIVITY_DEPLOY = MASTER_API_COMMAND_PREFIX_ACTIVITY + "/deploy";

  /**
   * Web socket command for setting metadata for an activity.
   */
  public static final String MASTER_API_COMMAND_ACTIVITY_METADATA_SET = MASTER_API_COMMAND_PREFIX_ACTIVITY
      + "/metadata/set";

  /**
   * Web socket command for locally deleting an activity.
   */
  public static final String MASTER_API_COMMAND_ACTIVITY_DELETE_LOCAL = MASTER_API_COMMAND_PREFIX_ACTIVITY
      + "/delete/local";

  /**
   * Prefix for live activity commands.
   */
  public static final String MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY = "/liveactivity";

  /**
   * Web socket command for getting view data for all live activities.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_ALL = MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY + "/all";

  /**
   * Web socket command for getting view data for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_VIEW = MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY + "/view";

  /**
   * Web socket command for getting full view data for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_VIEW_FULL = MASTER_API_COMMAND_LIVE_ACTIVITY_VIEW
      + "/full";

  /**
   * Web socket command for creating a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_CREATE = MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY
      + "/create";

  /**
   * Web socket command for deploying a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_DEPLOY = MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY
      + "/deploy";

  /**
   * Web socket command for configuring a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURE = MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY
      + "/configure";

  /**
   * Web socket command for getting configuration data for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURATION_GET =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY + "/configuration/get";

  /**
   * Web socket command for setting configuration data for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_CONFIGURATION_SET =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY + "/configuration/set";

  /**
   * Web socket command for setting metadata for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_METADATA_SET = MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY
      + "/metadata/set";

  /**
   * Web socket command for starting up a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_STARTUP = MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY
      + "/startup";

  /**
   * Web socket command for activating a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_ACTIVATE = MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY
      + "/activate";

  /**
   * Web socket command for deactivating a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_DEACTIVATE = MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY
      + "/deactivate";

  /**
   * Web socket command for shutting down a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_SHUTDOWN = MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY
      + "/shutdown";

  /**
   * Web socket command for getting the status for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_STATUS = MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY
      + "/status";

  /**
   * Web socket command for deleting the master's entry for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_DELETE_LOCAL = MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY
      + "/delete/local";

  /**
   * Web socket command for deleting a live activity from its controller.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_DELETE_REMOTE = MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY
      + "/delete/remote";

  /**
   * Web socket command for cleaning out a live activity's permanent data folder.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_DATA_PERMANENT_CLEAN =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY + "/data/permanent/clean";

  /**
   * Web socket command for cleaning out a live activity's temporary data folder.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_DATA_TEMPORARY_CLEAN =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY + "/data/temporary/clean";

  /**
   * Prefix for live activity group commands.
   */
  public static final String MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY_GROUP = "/liveactivitygroup";

  /**
   * Web socket command for getting view data for all live activity groups.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_ALL =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY_GROUP + "/all";

  /**
   * Web socket command for getting view data for a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_VIEW =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY_GROUP + "/view";

  /**
   * Web socket command for getting full view data for a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_VIEW_FULL =
      MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_VIEW + "/full";

  /**
   * Web socket command for deploying a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DEPLOY =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY_GROUP + "/deploy";

  /**
   * Web socket command for configuring a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_CONFIGURE =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY_GROUP + "/configure";

  /**
   * Web socket command for setting metadata for a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_METADATA_SET =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY_GROUP + "/metadata/set";

  /**
   * Web socket command for starting up a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_STARTUP =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY_GROUP + "/startup";

  /**
   * Web socket command for activating a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_ACTIVATE =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY_GROUP + "/activate";

  /**
   * Web socket command for deactivating a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DEACTIVATE =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY_GROUP + "/deactivate";

  /**
   * Web socket command for shutting down a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_SHUTDOWN =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY_GROUP + "/shutdown";

  /**
   * Web socket command for getting the status for a live activity group.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_STATUS =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY_GROUP + "/status";

  /**
   * Web socket command for force shutting down a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_SHUTDOWN_FORCE =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY_GROUP + "/shutdown/force";

  /**
   * Web socket command for deleting the master's entry for a live activity.
   */
  public static final String MASTER_API_COMMAND_LIVE_ACTIVITY_GROUP_DELETE_LOCAL =
      MASTER_API_COMMAND_PREFIX_LIVE_ACTIVITY_GROUP + "/delete/local";

  /**
   * Prefix for space commands.
   */
  public static final String MASTER_API_COMMAND_PREFIX_SPACE = "/space";

  /**
   * Web socket command for getting all spaces.
   */
  public static final String MASTER_API_COMMAND_SPACE_ALL = MASTER_API_COMMAND_PREFIX_SPACE + "/all";

  /**
   * Web socket command for getting view data for a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_VIEW = MASTER_API_COMMAND_PREFIX_SPACE + "/view";

  /**
   * Web socket command for getting full view data for a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_VIEW_FULL = MASTER_API_COMMAND_SPACE_VIEW + "/full";

  /**
   * Web socket command for deploying a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_DEPLOY = MASTER_API_COMMAND_PREFIX_SPACE + "/deploy";

  /**
   * Web socket command for configuring a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONFIGURE = MASTER_API_COMMAND_PREFIX_SPACE + "/configure";

  /**
   * Web socket command for setting metadata for a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_METADATA_SET = MASTER_API_COMMAND_PREFIX_SPACE + "/metadata/set";

  /**
   * Web socket command for starting up a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_STARTUP = MASTER_API_COMMAND_PREFIX_SPACE + "/startup";

  /**
   * Web socket command for activating a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_ACTIVATE = MASTER_API_COMMAND_PREFIX_SPACE + "/activate";

  /**
   * Web socket command for deactivating a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_DEACTIVATE = MASTER_API_COMMAND_PREFIX_SPACE + "/deactivate";

  /**
   * Web socket command for shutting down a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_SHUTDOWN = MASTER_API_COMMAND_PREFIX_SPACE + "/shutdown";

  /**
   * Web socket command for getting the status for a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_STATUS = MASTER_API_COMMAND_PREFIX_SPACE + "/status";

  /**
   * Web socket command for deleting the master's entry for a space.
   */
  public static final String MASTER_API_COMMAND_SPACE_DELETE_LOCAL = MASTER_API_COMMAND_PREFIX_SPACE + "/delete/local";

  /**
   * Prefix for space controller commands.
   */
  public static final String MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER = "/spacecontroller";

  /**
   * Web socket command for getting view data for all space controllers.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_ALL = MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER
      + "/all";

  /**
   * Web socket command for getting view data for a space controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_VIEW = MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER
      + "/view";

  /**
   * Web socket command for getting full view data for a space controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_VIEW_FULL = MASTER_API_COMMAND_SPACE_CONTROLLER_VIEW
      + "/full";

  /**
   * Web socket command for configuring a space controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_CONFIGURE =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/configure";

  /**
   * Web socket command for getting configuration data for a space controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_CONFIGURATION_GET =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/configuration/get";

  /**
   * Web socket command for setting configuration data for a space controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_CONFIGURATION_SET =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/configuration/set";

  /**
   * Web socket command for setting metadata for a space controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_METADATA_SET =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/metadata/set";

  /**
   * Web socket command for connecting to a space controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_CONNECT = MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER
      + "/connect";

  /**
   * Web socket command for disconnecting from a space controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_DISCONNECT =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/disconnect";

  /**
   * Web socket command for connecting to all space controllers.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_CONNECT_ALL =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/all/connect";

  /**
   * Web socket command for disconnecting from all space controllers.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_DISCONNECT_ALL =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/all/disconnect";

  /**
   * Web socket command for requesting the status from a space controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_STATUS = MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER
      + "/status";

  /**
   * Web socket command for requesting the status from all space controllers.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_STATUS_ALL =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/all/status";

  /**
   * Web socket command for deploying all live activities on a controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_DEPLOY = MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER
      + "/deploy";

  /**
   * Web socket command for deploying all live activities on all controllers.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_DEPLOY_ALL =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/all/deploy";

  /**
   * Web socket command for cleaning out a space controller's permanent data folder.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_PERMANENT_CLEAN =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/data/permanent/clean";

  /**
   * Web socket command for cleaning out a space controller's temporary data folder.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_TEMPORARY_CLEAN =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/data/temporary/clean";

  /**
   * Web socket command for cleaning out all space controller's permanent data folders.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_PERMANENT_CLEAN_ALL =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/all/data/permanent/clean";

  /**
   * Web socket command for cleaning out all space controller's temporary data folders.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_TEMPORARY_CLEAN_ALL =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/all/data/temporary/clean";

  /**
   * Web socket command for cleaning out a space controller's permanent data folder.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_LIVE_ACTIVITY_DATA_PERMANENT_CLEAN =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/liveactivities/data/permanent/clean";

  /**
   * Web socket command for cleaning out a space controller's temporary data folder.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_LIVE_ACTIVITY_DATA_TEMPORARY_CLEAN =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/liveactivities/data/temporary/clean";

  /**
   * Web socket command for cleaning out all space controller's permanent data folders.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_LIVE_ACTIVITY_DATA_PERMANENT_CLEAN =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/all/liveactivities/data/permanent/clean";

  /**
   * Web socket command for cleaning out a space controller's temporary data folder.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_LIVE_ACTIVITY_DATA_TEMPORARY_CLEAN =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/all/liveactivities/data/temporary/clean";

  /**
   * Web socket command for shutting down a space controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_SHUTDOWN = MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER
      + "/shutdown";

  /**
   * Web socket command for shutting down all live activities on a space controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_LIVE_ACTIVITY_SHUTDOWN_ALL =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/liveactivities/shutdown";

  /**
   * Web socket command for shutting down all space controllers.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_SHUTDOWN =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/all/shutdown";

  /**
   * Web socket command for shutting down all live activities on all space controllers.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_LIVE_ACTIVITY_SHUTDOWN_ALL =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/all/liveactivities/shutdown";

  /**
   * Web socket command for capturing data from a space controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_CAPTURE =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/data/capture";

  /**
   * Web socket command for restoring data from a space controller.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_DATA_RESTORE =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/all/data/restore";

  /**
   * Web socket command for capturing data from all space controllers.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_DATA_CAPTURE =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/all/data/capture";

  /**
   * Web socket command for restoring data from all space controllers.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_ALL_DATA_RESTORE =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/all/data/restore";

  /**
   * Web socket command for deleting a space controller from the master domain model.
   */
  public static final String MASTER_API_COMMAND_SPACE_CONTROLLER_DELETE_LOCAL =
      MASTER_API_COMMAND_PREFIX_SPACE_CONTROLLER + "/delete";

  /**
   * Prefix for named script commands.
   */
  public static final String MASTER_API_COMMAND_PREFIX_NAMEDSCRIPT = "/admin/namedscript";

  /**
   * Web socket command for getting all named scripts.
   */
  public static final String MASTER_API_COMMAND_NAMEDSCRIPT_ALL = MASTER_API_COMMAND_PREFIX_NAMEDSCRIPT + "/all";

  /**
   * Web socket command for viewing a named script.
   */
  public static final String MASTER_API_COMMAND_NAMEDSCRIPT_VIEW = MASTER_API_COMMAND_PREFIX_NAMEDSCRIPT + "/view";

  /**
   * Web socket command for setting metadata for a named script.
   */
  public static final String MASTER_API_COMMAND_NAMEDSCRIPT_METADATA_SET = MASTER_API_COMMAND_PREFIX_NAMEDSCRIPT
      + "/metadata/set";

  /**
   * Web socket command for running a named script.
   */
  public static final String MASTER_API_COMMAND_NAMEDSCRIPT_RUN = MASTER_API_COMMAND_PREFIX_NAMEDSCRIPT + "/run";

  /**
   * Web socket command for deleting a named script.
   */
  public static final String MASTER_API_COMMAND_NAMEDSCRIPT_DELETE = MASTER_API_COMMAND_PREFIX_NAMEDSCRIPT + "/delete";

  /**
   * Prefix for master domain model commands.
   */
  public static final String MASTER_API_COMMAND_PREFIX_ADMIN_MASTER_DOMAIN_MODEL = "/admin/masterdomainmodel";

  /**
   * Web socket command for importing a master domain model.
   */
  public static final String MASTER_API_COMMAND_ADMIN_MASTER_DOMAIN_MODEL_IMPORT =
      MASTER_API_COMMAND_PREFIX_ADMIN_MASTER_DOMAIN_MODEL + "/import";

  /**
   * Web socket command for obtaining the Interactive Spaces version.
   */
  public static final String MASTER_API_COMMAND_INTERACTIVE_SPACES_VERSION = "/interactivespaces/version";

  /**
   * Web socket command for importing a master domain model.
   */
  public static final String MASTER_API_COMMAND_ADMIN_MASTER_DOMAIN_MODEL_EXPORT =
      MASTER_API_COMMAND_PREFIX_ADMIN_MASTER_DOMAIN_MODEL + "/export";

  /**
   * Parameter name for the ID of the entity in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_ENTITY_ID = "id";

  /**
   * Status parameter name for the UUID of the entity in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_ENTITY_UUID = "uuid";

  /**
   * Status parameter name for the descriptive name of the entity in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_ENTITY_NAME = "name";

  /**
   * Status parameter name for the description of the entity in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_ENTITY_DESCRIPTION = "description";

  /**
   * Status parameter name for the identifying name of the entity in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_ENTITY_IDENTIFYING_NAME = "identifyingName";

  /**
   * Status parameter name for the metadata of the entity in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_ENTITY_METADATA = "metadata";

  /**
   * Status parameter name for the configuration of a configurable entity in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_ENTITY_CONFIG = "config";

  /**
   * Parameter name for the ID of an activity in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_ACTIVITY_ID = "activityId";

  /**
   * Parameter name for the ID of a space controller in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_SPACE_CONTROLLER_ID = "spaceControllerId";

  /**
   * Parameter name for the host ID of a space controller in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_SPACE_CONTROLLER_HOSTID = "hostId";

  /**
   * Parameter name for the mode of a space controller in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_SPACE_CONTROLLER_MODE = "mode";

  /**
   * Parameter name for the mode decription of a space controller in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_SPACE_CONTROLLER_MODE_DESCRIPTION = "modeDescription";

  /**
   * Parameter name for the language of a named script in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_NAMED_SCRIPT_LANGUAGE = "language";

  /**
   * Message type for a command response.
   */
  public static final String MASTER_API_MESSAGE_TYPE_COMMAND_RESPONSE = "commandResponse";

  /**
   * Message type for a status update.
   */
  public static final String MASTER_API_MESSAGE_TYPE_STATUS_UPDATE = "statusUpdate";

  /**
   * Status parameter name for a model, e.g. a Master Domain Model, in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_MODEL = "model";

  /**
   * Status parameter name for the Interactive Spaces version in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_NAME_INTERACTIVE_SPACES_VERSION = "interactiveSpacesVersion";

  /**
   * Status parameter value for an unknown Interactive Spaces version in the Master API message.
   */
  public static final String MASTER_API_PARAMETER_VALUE_INTERACTIVE_SPACES_VERSION_UNKNOWN = "Unknown";

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
   * The JSON metadata command for selecting some of the metadata.
   */
  public static final String MASTER_API_COMMAND_METADATA_DELETE = "delete";

  /**
   * The Master API command parameter for filtering retrievals of all entities of a domain model type..
   */
  public static final String MASTER_API_PARAMETER_NAME_FILTER = "filter";

  /**
   * Message key for unknown commands.
   */
  public static final String MESSAGE_SPACE_COMMAND_UNKNOWN = "space.command.unknown";

  /**
   * Message key for a call failing.
   */
  public static final String MESSAGE_SPACE_CALL_FAILURE = "space.command.failure";

  /**
   * Message key for a call failing because live activities cannot be created.
   */
  public static final String MESSAGE_SPACE_CALL_FAILURE_CANNOT_CREATE_LIVE_ACTIVITY =
      "space.command.failure.liveactivity.nocreate";

  /**
   * Message key for a call failing because live activities cannot be created.
   */
  public static final String MESSAGE_SPACE_DETAIL_CALL_FAILURE_CANNOT_CREATE_LIVE_ACTIVITY =
      "Live activities currently cannot be created.";

  /**
   * Message key for a call failing because live activities cannot be created.
   */
  public static final String MESSAGE_SPACE_DETAIL_CALL_FAILURE_MISSING_MODEL = "The argument 'mode' is missing.";

  /**
   * Message key for a call failing due to missing arguments.
   */
  public static final String MESSAGE_SPACE_CALL_ARGS_MISSING = "space.command.args.missing";

  /**
   * Message key for a call failing because an argument needed to be a map.
   */
  public static final String MESSAGE_SPACE_CALL_ARGS_NOMAP = "space.command.args.notmap";

  /**
   * Message key for a call failing.
   */
  public static final String MESSAGE_SPACE_DETAIL_CALL_ARGS_NOMAP = "The call was made without a map of arguments";

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
