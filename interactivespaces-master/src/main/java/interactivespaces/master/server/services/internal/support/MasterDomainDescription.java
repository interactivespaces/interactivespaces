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

package interactivespaces.master.server.services.internal.support;

import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;

/**
 * Constants for creating a master domain description.
 * 
 * @author Keith M. Hughes
 */
public interface MasterDomainDescription {

	/**
	 * The XML value for {@code false}
	 */
	public static final String VALUE_FALSE = "false";

	/**
	 * The XML value for {@code true}
	 */
	public static final String VALUE_TRUE = "true";

	/**
	 * The XML attribute name to be used for all IDs.
	 */
	public static final String ATTRIBUTE_NAME_ID = "id";

	/**
	 * The XML element name to be used for all UUIDs.
	 */
	public static final String ELEMENT_NAME_UUID = "uuid";

	/**
	 * The XML element name to be used for all names.
	 */
	public static final String ELEMENT_NAME_NAME = "name";

	/**
	 * The XML element name to be used for all descriptions.
	 */
	public static final String ELEMENT_NAME_DESCRIPTION = "description";

	/**
	 * The XML element name for the root element description.
	 */
	public static final String ELEMENT_NAME_DESCRIPTION_ROOT_ELEMENT = "interactivespaces-master-domain-model";

	/**
	 * The XML element name for the root space controllers element.
	 */
	public static final String ELEMENT_NAME_ROOT_SPACE_CONTROLLERS = "space-controllers";

	/**
	 * The XML element name for an individual space controller element.
	 */

	public static final String ELEMENT_NAME_INDIVIDUAL_SPACE_CONTROLLER = "space-controller";
	/**
	 * The XML element name for a space controller host id.
	 */
	public static final String ELEMENT_NAME_SPACE_CONTROLLER_HOST_ID = "host-id";

	/**
	 * The XML element name for the root activities element.
	 */
	public static final String ELEMENT_NAME_ROOT_ACTIVITIES = "activities";

	/**
	 * The XML element name for an individual activity.
	 */
	public static final String ELEMENT_NAME_INDIVIDUAL_ACTIVITY = "activity";

	/**
	 * The XML element name for an activity's identifying name.
	 */
	public static final String ELEMENT_NAME_ACTIVITY_IDENTIFYING_NAME = "identifying-name";

	/**
	 * The XML element name for an activity's version.
	 */
	public static final String ELEMENT_NAME_ACTIVITY_VERSION = "version";

	/**
	 * The XML element name for the root activity dependencies.
	 */
	public static final String ELEMENT_NAME_ROOT_ACTIVITY_DEPENDENCIES = "activity-dependencies";

	/**
	 * The XML element name for an individual activity dependency.
	 */
	public static final String ELEMENT_NAME_INDIVIDUAL_ACTIVITY_DEPENDENCY = "activity-dependency";

	/**
	 * The XML element name for an individual activity dependency's name.
	 */
	public static final String ELEMENT_NAME_ACTIVITY_DEPENDENCY_NAME = "activity-dependency-name";

	/**
	 * The XML element name for an individual activity dependency's minimum
	 * version.
	 */
	public static final String ELEMENT_NAME_ACTIVITY_DEPENDENCY_VERSION_MINIMUM = "activity-dependency-version-min";

	/**
	 * The XML element name for an individual activity dependency's maximum
	 * version.
	 */
	public static final String ELEMENT_NAME_ACTIVITY_DEPENDENCY_VERSION_MAXIMUM = "activity-dependency-version-max";

	/**
	 * The XML element name for an individual activity dependency's required
	 * property.
	 */
	public static final String ELEMENT_NAME_ACTIVITY_DEPENDENCY_REQUIRED = "activity-dependency-required";

	/**
	 * The XML element name for the root {@link LiveActivity} element.
	 */
	public static final String ELEMENT_NAME_ROOT_LIVE_ACTIVITIES = "live-activities";

	/**
	 * The XML element name for an individual {@link LiveActivity}.
	 */
	public static final String ELEMENT_NAME_INDIVIDUAL_LIVE_ACTIVITY = "live-activity";

	/**
	 * The XML element name for a live activity's controller.
	 */
	public static final String ELEMENT_NAME_LIVE_ACTIVITY_CONTROLLER = "controller";

	/**
	 * The XML element name for a live activity's activity.
	 */
	public static final String ELEMENT_NAME_LIVE_ACTIVITY_ACTIVITY = "activity";

	/**
	 * The XML element name for the root {@link LiveActivityGroup} element.
	 */
	public static final String ELEMENT_NAME_ROOT_LIVE_ACTIVITY_GROUPS = "live-activity-groups";

	/**
	 * The XML element name for an individual {@link LiveActivityGroup}.
	 */
	public static final String ELEMENT_NAME_INDIVIDUAL_LIVE_ACTIVITY_GROUP = "live-activity-group";

	/**
	 * The XML element name for the root live activity group's
	 * {@link GroupLiveActivity} collection element.
	 */
	public static final String ELEMENT_NAME_LIVE_ACTIVITY_GROUP_ROOT_GROUP_LIVE_ACTIVITIES = "group-live-activities";

	/**
	 * The XML element name for an individual live activity group's
	 * {@link GroupLiveActivity}.
	 */
	public static final String ELEMENT_NAME_LIVE_ACTIVITY_GROUP_INDIVIDUAL_GROUP_LIVE_ACTIVITY = "group-live-activity";

	/**
	 * The XML attribute name of the live activity ID attribute in a
	 * {@link GroupLiveActivity}.
	 */
	public static final String ATTRIBUTE_NAME_GROUP_LIVE_ACTIVITY_ID = "live-activity";

	/**
	 * The XML attribute name of the dependency attribute in a
	 * {@link GroupLiveActivity}.
	 */
	public static final String ATTRIBUTE_NAME_GROUP_LIVE_ACTIVITY_DEPENDENCY = "dependency";

	/**
	 * The XML element name for an activity configuration.
	 */
	public static final String ELEMENT_NAME_ACTIVITY_CONFIGURATION = "activity-configuration";

	/**
	 * The XML element name for the root element for an activity configuration's
	 * parameters.
	 */
	public static final String ELEMENT_NAME_ACTIVITY_CONFIGURATION_ROOT_PARAMETERS = "activity-configuration-parameters";

	/**
	 * The XML element name for an individual parameter element for an activity
	 * configuration's parameters.
	 */
	public static final String ELEMENT_NAME_ACTIVITY_CONFIGURATION_INDIVIDUAL_PARAMETER = "parameter";

	/**
	 * The XML attribute name for an activity configuration's parameter name.
	 */
	public static final String ATTRIBUTE_NAME_ACTIVITY_CONFIGURATION_PARAMETER_NAME = "name";

	/**
	 * The XML element name for the root spaces element.
	 */
	public static final String ELEMENT_NAME_ROOT_SPACES = "spaces";

	/**
	 * The XML element name for an individual space.
	 */
	public static final String ELEMENT_NAME_INDIVIDUAL_SPACE = "space";

	/**
	 * The XML element name for a space's root subspaces element.
	 */
	public static final String ELEMENT_NAME_SPACE_ROOT_SUBSPACES = "subspaces";

	/**
	 * The XML element name for a space's individual subspace element.
	 */
	public static final String ELEMENT_NAME_SPACE_INDIVIDUAL_SUBSPACE = "subspace";

	/**
	 * The XML element name for a space's root live activity groups element.
	 */
	public static final String ELEMENT_NAME_SPACE_ROOT_LIVE_ACTIVITY_GROUPS = "live-activity-groups";

	/**
	 * The XML element name for a space's individual live activity group
	 * element.
	 */
	public static final String ELEMENT_NAME_SPACE_INDIVIDUAL_LIVE_ACTIVITY_GROUP = "live-activity-group";

	/**
	 * The XML element name for the root named scripts element.
	 */
	public static final String ELEMENT_NAME_ROOT_NAMED_SCRIPTS = "named-scripts";

	/**
	 * The XML element name for an individual named script element.
	 */
	public static final String ELEMENT_NAME_INDIVIDUAL_NAMED_SCRIPT = "named-script";

	/**
	 * The XML element name for a named script's language element.
	 */
	public static final String ELEMENT_NAME_NAMED_SCRIPT_LANGUAGE = "language";

	/**
	 * The XML element name for a named script's language schedule.
	 */
	public static final String ELEMENT_NAME_NAMED_SCRIPT_SCHEDULE = "schedule";

	/**
	 * The XML element name for a named script's content element.
	 */
	public static final String ELEMENT_NAME_NAMED_SCRIPT_CONTENT = "content";

	/**
	 * The XML element name for metadata
	 */
	public static final String ELEMENT_NAME_METADATA = "metadata";

	/**
	 * The XML element name for a metadata item.
	 */
	public static final String ELEMENT_NAME_METADATA_ITEM = "item";

	/**
	 * The XML element name for the name of a metadata item.
	 */
	public static final String ATTRIBUTE_NAME_METADATA_ITEM_NAME = ELEMENT_NAME_NAME;

}