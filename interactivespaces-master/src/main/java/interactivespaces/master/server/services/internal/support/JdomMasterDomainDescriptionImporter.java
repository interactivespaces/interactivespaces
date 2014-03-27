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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.ActivityDependency;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.GroupLiveActivity.GroupLiveActivityDependency;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.space.Space;
import interactivespaces.domain.system.NamedScript;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.AutomationRepository;
import interactivespaces.master.server.services.SpaceControllerRepository;
import interactivespaces.time.TimeProvider;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * An importer of the master domain description.
 *
 * @author Keith M. Hughes
 */
public class JdomMasterDomainDescriptionImporter implements MasterDomainDescription {

  // TODO(keith): Get some error checking in here.

  /**
   * Map of space controller IDs from the description to the created space
   * controller.
   */
  private Map<String, SpaceController> spaceControllers = Maps.newHashMap();

  /**
   * Map of activity IDs from the description to the created activity.
   */
  private Map<String, Activity> activities = Maps.newHashMap();

  /**
   * Map of live activity IDs from the description to the created live activity.
   */
  private Map<String, LiveActivity> liveActivities = Maps.newHashMap();

  /**
   * Map of live activity group IDs from the description to the created live
   * activity groups.
   */
  private Map<String, LiveActivityGroup> liveActivityGroups = Maps.newHashMap();

  /**
   * Map of space IDs from the description to the created spaces.
   */
  private Map<String, Space> spaces = Maps.newHashMap();

  /**
   * IMport a space domain description.
   *
   * @param description
   *          description to import
   * @param activityRepository
   *          the repository for activity entities
   * @param controllerRepository
   *          the repository for controller entities
   * @param automationRepository
   *          the repository for automation entities
   * @param timeProvider
   *          the time provider to use
   */
  public void importDescription(String description, ActivityRepository activityRepository,
      SpaceControllerRepository controllerRepository, AutomationRepository automationRepository,
      TimeProvider timeProvider) {
    Element rootElement = readDescription(description);

    if (!ELEMENT_NAME_DESCRIPTION_ROOT_ELEMENT.equals(rootElement.getName())) {
      throw new InteractiveSpacesException(String.format(
          "The description file doesn't have root element %s",
          ELEMENT_NAME_DESCRIPTION_ROOT_ELEMENT));
    }

    getSpaceControllers(rootElement, controllerRepository);
    getActivities(rootElement, activityRepository, timeProvider);
    getLiveActivities(rootElement, activityRepository);
    getLiveActivityGroups(rootElement, activityRepository);
    getSpaces(rootElement, activityRepository);
    getNamedScripts(rootElement, automationRepository);
  }

  /**
   * Get the space controllers.
   *
   * @param rootElement
   *          the root element of the XML description
   * @param controllerRepository
   *          repository for controller entities
   */
  private void getSpaceControllers(Element rootElement, SpaceControllerRepository controllerRepository) {
    Element spaceControllersElement = rootElement.getChild(ELEMENT_NAME_ROOT_SPACE_CONTROLLERS);

    if (spaceControllersElement != null) {
      List<Element> controllerElements =
          spaceControllersElement.getChildren(ELEMENT_NAME_INDIVIDUAL_SPACE_CONTROLLER);
      for (Element controllerElement : controllerElements) {
        getSpaceController(controllerElement, controllerRepository);
      }
    }
  }

  /**
   * Get a space controller.
   *
   * @param controllerElement
   *          the controller XML element
   * @param controllerRepository
   *          repository for controller entities
   */
  private void getSpaceController(Element controllerElement,
      SpaceControllerRepository controllerRepository) {
    String id = controllerElement.getAttributeValue(ATTRIBUTE_NAME_ID);

    SpaceController controller = controllerRepository.newSpaceController();

    String uuid = controllerElement.getChildText(ELEMENT_NAME_UUID);
    String name = controllerElement.getChildText(ELEMENT_NAME_NAME);
    String description = controllerElement.getChildText(ELEMENT_NAME_DESCRIPTION);
    String hostId = controllerElement.getChildText(ELEMENT_NAME_SPACE_CONTROLLER_HOST_ID);

    controller.setUuid(uuid);
    controller.setName(name);
    controller.setDescription(description);
    controller.setHostId(hostId);

    Map<String, Object> metadata = getMetadata(controllerElement.getChild(ELEMENT_NAME_METADATA));
    if (metadata != null) {
      controller.setMetadata(metadata);
    }

    spaceControllers.put(id, controller);

    controllerRepository.saveSpaceController(controller);
  }

  /**
   * Get all activities from the description.
   *
   * @param rootElement
   *          the root element of the XML description
   * @param activityRepository
   *          repository for activity entities
   * @param timeProvider
   *          the time provider to use
   */
  private void getActivities(Element rootElement, ActivityRepository activityRepository,
      TimeProvider timeProvider) {
    Element activitiesElement = rootElement.getChild(ELEMENT_NAME_ROOT_ACTIVITIES);

    if (activitiesElement != null) {
      List<Element> activityElements =
          activitiesElement.getChildren(ELEMENT_NAME_INDIVIDUAL_ACTIVITY);
      for (Element activityElement : activityElements) {
        getActivity(activityElement, activityRepository, timeProvider);
      }
    }
  }

  /**
   * Get an individual activity.
   *
   * @param activityElement
   *          the activity XML element
   * @param activityRepository
   *          repository for activity entities
   * @param timeProvider
   *          the time provider
   */
  private void getActivity(Element activityElement, ActivityRepository activityRepository,
      TimeProvider timeProvider) {
    String id = activityElement.getAttributeValue(ATTRIBUTE_NAME_ID);

    Activity activity = activityRepository.newActivity();

    String name = activityElement.getChildText(ELEMENT_NAME_NAME);
    String description = activityElement.getChildText(ELEMENT_NAME_DESCRIPTION);
    String identifyingName = activityElement.getChildText(ELEMENT_NAME_ACTIVITY_IDENTIFYING_NAME);
    String version = activityElement.getChildText(ELEMENT_NAME_ACTIVITY_VERSION);

    activity.setIdentifyingName(identifyingName);
    activity.setVersion(version);
    activity.setName(name);
    activity.setDescription(description);

    String lastUploadDateString = activityElement.getAttributeValue(ATTRIBUTE_NAME_LAST_UPLOAD_DATE);
    if (lastUploadDateString != null) {
      activity.setLastUploadDate(new Date(Long.parseLong(lastUploadDateString)));
    }

    String lastStartDateString = activityElement.getAttributeValue(ATTRIBUTE_NAME_LAST_START_DATE);
    if (lastStartDateString != null) {
      activity.setLastStartDate(new Date(Long.parseLong(lastStartDateString)));
    }

    Map<String, Object> metadata = getMetadata(activityElement.getChild(ELEMENT_NAME_METADATA));
    if (metadata != null) {
      activity.setMetadata(metadata);
    }

    addActivityDependencies(activityElement, activity, activityRepository);

    activities.put(id, activity);

    activityRepository.saveActivity(activity);
  }

  /**
   * Add any activity dependencies there may be.
   *
   * @param activityElement
   *          root XML element for the activity
   * @param activity
   *          the activity being extracted
   * @param activityRepository
   *          the repository for activity entities
   */
  private void addActivityDependencies(Element activityElement, Activity activity,
      ActivityRepository activityRepository) {
    Element dependenciesElement = activityElement.getChild(ELEMENT_NAME_ROOT_ACTIVITY_DEPENDENCIES);
    if (dependenciesElement != null) {
      List<ActivityDependency> dependencies = Lists.newArrayList();

      List<Element> dependencyElements =
          dependenciesElement.getChildren(ELEMENT_NAME_INDIVIDUAL_ACTIVITY_DEPENDENCY);
      for (Element dependencyElement : dependencyElements) {
        String dependencyName =
            dependencyElement.getChildText(ELEMENT_NAME_ACTIVITY_DEPENDENCY_NAME);
        String dependencyVersionMinimum =
            dependencyElement.getChildText(ELEMENT_NAME_ACTIVITY_DEPENDENCY_VERSION_MINIMUM);
        String dependencyVersionMaximum =
            dependencyElement.getChildText(ELEMENT_NAME_ACTIVITY_DEPENDENCY_VERSION_MAXIMUM);
        String dependencyRequired =
            dependencyElement.getChildText(ELEMENT_NAME_ACTIVITY_DEPENDENCY_REQUIRED);

        ActivityDependency dependency = activityRepository.newActivityDependency();
        dependency.setName(dependencyName);
        dependency.setMinimumVersion(dependencyVersionMinimum);
        dependency.setMaximumVersion(dependencyVersionMaximum);
        dependency.setRequired(VALUE_TRUE.equals(dependencyRequired));

        dependencies.add(dependency);
      }

      activity.setDependencies(dependencies);
    }
  }

  /**
   * Get all live activities from the description.
   *
   * @param rootElement
   *          the root element of the XML description
   * @param activityRepository
   *          repository for activity entities
   */
  private void getLiveActivities(Element rootElement, ActivityRepository activityRepository) {
    Element activitiesElement = rootElement.getChild(ELEMENT_NAME_ROOT_LIVE_ACTIVITIES);

    if (activitiesElement != null) {
      List<Element> activityElements =
          activitiesElement.getChildren(ELEMENT_NAME_INDIVIDUAL_LIVE_ACTIVITY);
      for (Element activityElement : activityElements) {
        getLiveActivity(activityElement, activityRepository);
      }
    }
  }

  /**
   * Get an individual live activity.
   *
   * @param activityElement
   *          the live activity XML element
   * @param activityRepository
   *          repository for activity entities
   */
  private void getLiveActivity(Element activityElement, ActivityRepository activityRepository) {
    String id = activityElement.getAttributeValue(ATTRIBUTE_NAME_ID);

    LiveActivity activity = activityRepository.newLiveActivity();

    String uuid = activityElement.getChildText(ELEMENT_NAME_UUID);
    String name = activityElement.getChildText(ELEMENT_NAME_NAME);
    String description = activityElement.getChildText(ELEMENT_NAME_DESCRIPTION);

    activity.setUuid(uuid);
    activity.setName(name);
    activity.setDescription(description);

    Map<String, Object> metadata = getMetadata(activityElement.getChild(ELEMENT_NAME_METADATA));
    if (metadata != null) {
      activity.setMetadata(metadata);
    }

    Element mySpaceControllerElement =
        activityElement.getChild(ELEMENT_NAME_LIVE_ACTIVITY_CONTROLLER);
    if (mySpaceControllerElement != null) {
      String mySpaceControllerId = mySpaceControllerElement.getAttributeValue(ATTRIBUTE_NAME_ID);
      SpaceController myController = spaceControllers.get(mySpaceControllerId);
      activity.setController(myController);
    }

    Element myActivityElement = activityElement.getChild(ELEMENT_NAME_LIVE_ACTIVITY_ACTIVITY);
    if (myActivityElement != null) {
      String myActivityId = myActivityElement.getAttributeValue(ATTRIBUTE_NAME_ID);
      Activity myActivity = activities.get(myActivityId);
      activity.setActivity(myActivity);
    }

    ActivityConfiguration configuration =
        getActivityConfiguration(activityElement, activityRepository);
    if (configuration != null) {
      activity.setConfiguration(configuration);
    }

    liveActivities.put(id, activity);

    activityRepository.saveLiveActivity(activity);
  }

  /**
   * Get an activity configuration.
   *
   * @param rootElement
   *          the XML element that might contain an activity configuration
   * @param activityRepository
   *          repository for activity entities
   *
   * @return an activity configuration, if there was one, or {@code null}
   */
  private ActivityConfiguration getActivityConfiguration(Element rootElement,
      ActivityRepository activityRepository) {
    Element configurationElement = rootElement.getChild(ELEMENT_NAME_ACTIVITY_CONFIGURATION);
    if (configurationElement != null) {
      ActivityConfiguration configuration = activityRepository.newActivityConfiguration();
      Element parametersElement =
          configurationElement.getChild(ELEMENT_NAME_ACTIVITY_CONFIGURATION_ROOT_PARAMETERS);
      if (parametersElement != null) {
        List<Element> parameterElements =
            parametersElement.getChildren(ELEMENT_NAME_ACTIVITY_CONFIGURATION_INDIVIDUAL_PARAMETER);
        for (Element parameterElement : parameterElements) {
          ConfigurationParameter parameter = activityRepository.newConfigurationParameter();

          String name =
              parameterElement
                  .getAttributeValue(ATTRIBUTE_NAME_ACTIVITY_CONFIGURATION_PARAMETER_NAME);
          String value = parameterElement.getText();

          parameter.setName(name);
          parameter.setValue(value);

          configuration.addParameter(parameter);
        }
      }

      return configuration;
    }

    return null;
  }

  /**
   * Get all live activity groups from the description.
   *
   * @param rootElement
   *          the root element of the XML description
   * @param activityRepository
   *          repository for activity entities
   */
  private void getLiveActivityGroups(Element rootElement, ActivityRepository activityRepository) {
    Element groupsElement = rootElement.getChild(ELEMENT_NAME_ROOT_LIVE_ACTIVITY_GROUPS);

    if (groupsElement != null) {
      List<Element> groupElements =
          groupsElement.getChildren(ELEMENT_NAME_INDIVIDUAL_LIVE_ACTIVITY_GROUP);
      for (Element groupElement : groupElements) {
        getLiveActivityGroup(groupElement, activityRepository);
      }
    }
  }

  /**
   * Get an individual live activity group.
   *
   * @param groupElement
   *          the live activity group XML element
   * @param activityRepository
   *          repository for activity entities
   */
  private void getLiveActivityGroup(Element groupElement, ActivityRepository activityRepository) {
    String id = groupElement.getAttributeValue(ATTRIBUTE_NAME_ID);

    LiveActivityGroup group = activityRepository.newLiveActivityGroup();

    String name = groupElement.getChildText(ELEMENT_NAME_NAME);
    String description = groupElement.getChildText(ELEMENT_NAME_DESCRIPTION);

    group.setName(name);
    group.setDescription(description);

    Map<String, Object> metadata = getMetadata(groupElement.getChild(ELEMENT_NAME_METADATA));
    if (metadata != null) {
      group.setMetadata(metadata);
    }

    getLiveActivityGroupLiveActivities(groupElement, group);

    liveActivityGroups.put(id, group);

    activityRepository.saveLiveActivityGroup(group);
  }

  /**
   * Get the live activities for a live activity group.
   *
   * @param groupElement
   *          the XML element for the live activity group
   * @param group
   *          the live activity group
   */
  private void getLiveActivityGroupLiveActivities(Element groupElement, LiveActivityGroup group) {
    Element groupLiveActivitiesElement =
        groupElement.getChild(ELEMENT_NAME_LIVE_ACTIVITY_GROUP_ROOT_GROUP_LIVE_ACTIVITIES);
    if (groupLiveActivitiesElement != null) {
      List<Element> groupLiveActivityElements =
          groupLiveActivitiesElement
              .getChildren(ELEMENT_NAME_LIVE_ACTIVITY_GROUP_INDIVIDUAL_GROUP_LIVE_ACTIVITY);

      for (Element groupLiveActivityElement : groupLiveActivityElements) {
        String myLiveActivityId =
            groupLiveActivityElement.getAttributeValue(ATTRIBUTE_NAME_GROUP_LIVE_ACTIVITY_ID);
        String myDependency =
            groupLiveActivityElement
                .getAttributeValue(ATTRIBUTE_NAME_GROUP_LIVE_ACTIVITY_DEPENDENCY);

        LiveActivity activity = liveActivities.get(myLiveActivityId);
        GroupLiveActivityDependency dependency = GroupLiveActivityDependency.valueOf(myDependency);

        group.addActivity(activity, dependency);
      }
    }
  }

  /**
   * Get all spaces from the description.
   *
   * @param rootElement
   *          the root element of the XML description
   * @param activityRepository
   *          repository for activity entities
   */
  private void getSpaces(Element rootElement, ActivityRepository activityRepository) {
    // TODO(keith): This can be made to work no matter how spaces are
    // defined in XML by storing in map as find ID (even in subspaces), but
    // don't save in repository.
    // Then do depth first walk of all spaces to persist.

    Element spacesElement = rootElement.getChild(ELEMENT_NAME_ROOT_SPACES);

    if (spacesElement != null) {
      List<Element> spaceElements = spacesElement.getChildren(ELEMENT_NAME_INDIVIDUAL_SPACE);
      for (Element spaceElement : spaceElements) {
        getSpace(spaceElement, activityRepository);
      }
    }
  }

  /**
   * Get an individual space.
   *
   * @param spaceElement
   *          the space XML element
   * @param activityRepository
   *          repository for space entities
   */
  private void getSpace(Element spaceElement, ActivityRepository activityRepository) {
    String id = spaceElement.getAttributeValue(ATTRIBUTE_NAME_ID);

    Space space = activityRepository.newSpace();

    String name = spaceElement.getChildText(ELEMENT_NAME_NAME);
    String description = spaceElement.getChildText(ELEMENT_NAME_DESCRIPTION);

    space.setName(name);
    space.setDescription(description);

    Map<String, Object> metadata = getMetadata(spaceElement.getChild(ELEMENT_NAME_METADATA));
    if (metadata != null) {
      space.setMetadata(metadata);
    }

    getSpaceSubspaces(spaceElement, space);
    getSpaceLiveActivityGroups(spaceElement, space);

    spaces.put(id, space);

    activityRepository.saveSpace(space);
  }

  /**
   * Get the subspaces for a space.
   *
   * @param spaceElement
   *          the XML element for the space
   * @param space
   *          the space which potentially has subspaces
   */
  private void getSpaceSubspaces(Element spaceElement, Space space) {
    Element subspacesElement = spaceElement.getChild(ELEMENT_NAME_SPACE_ROOT_SUBSPACES);
    if (subspacesElement != null) {
      List<Element> subspaceElements =
          subspacesElement.getChildren(ELEMENT_NAME_SPACE_INDIVIDUAL_SUBSPACE);
      for (Element subspaceElement : subspaceElements) {
        String mySubspaceId = subspaceElement.getAttributeValue(ATTRIBUTE_NAME_ID);

        Space subspace = spaces.get(mySubspaceId);
        space.addSpace(subspace);
      }
    }
  }

  /**
   * Get the live activity groups for a space.
   *
   * @param spaceElement
   *          the XML element for the space
   * @param space
   *          the space which potentially has subspaces
   */
  private void getSpaceLiveActivityGroups(Element spaceElement, Space space) {
    Element groupsElement = spaceElement.getChild(ELEMENT_NAME_SPACE_ROOT_LIVE_ACTIVITY_GROUPS);
    if (groupsElement != null) {
      List<Element> groupElements =
          groupsElement.getChildren(ELEMENT_NAME_SPACE_INDIVIDUAL_LIVE_ACTIVITY_GROUP);
      for (Element groupElement : groupElements) {
        String mygroupId = groupElement.getAttributeValue(ATTRIBUTE_NAME_ID);

        LiveActivityGroup group = liveActivityGroups.get(mygroupId);
        space.addActivityGroup(group);
      }
    }
  }

  /**
   * Get all named scripts from the description.
   *
   * @param rootElement
   *          the root element of the XML description
   * @param automationRepository
   *          repository for automation entities
   */
  private void getNamedScripts(Element rootElement, AutomationRepository automationRepository) {
    Element scriptsElement = rootElement.getChild(ELEMENT_NAME_ROOT_NAMED_SCRIPTS);

    if (scriptsElement != null) {
      List<Element> scriptElements =
          scriptsElement.getChildren(ELEMENT_NAME_INDIVIDUAL_NAMED_SCRIPT);
      for (Element scriptElement : scriptElements) {
        getNamedScript(scriptElement, automationRepository);
      }
    }
  }

  /**
   * Get an individual named script.
   *
   * @param scriptElement
   *          the script XML element
   * @param automationRepository
   *          repository for automation entities
   */
  private void getNamedScript(Element scriptElement, AutomationRepository automationRepository) {
    String id = scriptElement.getAttributeValue(ATTRIBUTE_NAME_ID);

    NamedScript script = automationRepository.newNamedScript();

    String name = scriptElement.getChildText(ELEMENT_NAME_NAME);
    String description = scriptElement.getChildText(ELEMENT_NAME_DESCRIPTION);
    String language = scriptElement.getChildText(ELEMENT_NAME_NAMED_SCRIPT_LANGUAGE);
    String schedule = scriptElement.getChildText(ELEMENT_NAME_NAMED_SCRIPT_SCHEDULE);
    String content = scriptElement.getChildText(ELEMENT_NAME_NAMED_SCRIPT_CONTENT);

    script.setName(name);
    script.setDescription(description);
    script.setLanguage(language);
    script.setSchedule(schedule);
    script.setContent(content);

    automationRepository.saveNamedScript(script);
  }

  /**
   * Get a metadata map from an element, if any.
   *
   * @param metadataElement
   *          the metadata XML element (can be {@code null})
   *
   * @return either a map of metadata or {@code null} if no map.
   */
  private Map<String, Object> getMetadata(Element metadataElement) {
    if (metadataElement == null) {
      return null;
    }

    Map<String, Object> metadata = Maps.newHashMap();

    List<Element> itemElements = metadataElement.getChildren(ELEMENT_NAME_METADATA_ITEM);
    for (Element itemElement : itemElements) {
      String name = itemElement.getAttributeValue(ATTRIBUTE_NAME_METADATA_ITEM_NAME);
      String value = itemElement.getText();

      metadata.put(name, value);
    }

    if (metadata.isEmpty()) {
      return null;
    } else {
      return metadata;
    }
  }

  /**
   * Parse the description.
   *
   * @param description
   *          the model description
   *
   * @return the root element of the parse
   *
   * @throws InteractiveSpacesException
   *           if there was an error reading the description
   */
  private Element readDescription(String description) throws InteractiveSpacesException {
    try {
      StringReader reader = new StringReader(description);

      SAXBuilder builder = new SAXBuilder();
      Document doc = builder.build(reader);

      return doc.getRootElement();
    } catch (Exception e) {
      throw new InteractiveSpacesException("Unable to read master domain model description", e);
    }
  }
}
