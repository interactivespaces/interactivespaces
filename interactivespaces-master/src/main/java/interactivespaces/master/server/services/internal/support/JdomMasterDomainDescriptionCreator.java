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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.ActivityDependency;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.SpaceControllerConfiguration;
import interactivespaces.domain.basic.SpaceControllerMode;
import interactivespaces.domain.space.Space;
import interactivespaces.domain.system.NamedScript;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.AutomationRepository;
import interactivespaces.master.server.services.SpaceControllerRepository;

import com.google.common.collect.Sets;

import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A creator of the master domain description which uses JDOM.
 *
 * @author Keith M. Hughes
 */
public class JdomMasterDomainDescriptionCreator implements MasterDomainDescription {

  /**
   * Create a description of the entire space domain.
   *
   * @param activityRepository
   *          the repository for activity entities
   * @param controllerRepository
   *          the repository for controller entities
   * @param automationRepository
   *          the repository for automation entities
   *
   * @return the XML description
   */
  public String newDescription(ActivityRepository activityRepository, SpaceControllerRepository controllerRepository,
      AutomationRepository automationRepository) {

    try {
      Element rootElement = new Element(ELEMENT_NAME_DESCRIPTION_ROOT_ELEMENT);
      Document document = new Document(rootElement);

      rootElement.addContent(newSpaceControllerEntries(controllerRepository));
      rootElement.addContent(newActivityEntries(activityRepository));
      rootElement.addContent(newLiveActivityEntries(activityRepository));
      rootElement.addContent(newLiveActivityGroupEntries(activityRepository));
      rootElement.addContent(newSpaceEntries(activityRepository));
      rootElement.addContent(newNamedScriptEntries(automationRepository));

      StringWriter out = new StringWriter();

      Format format = Format.getPrettyFormat();
      XMLOutputter outputter = new XMLOutputter(format);
      outputter.output(document, out);

      return out.toString();
    } catch (IOException e) {
      throw new InteractiveSpacesException("Could not create domain model", e);
    }
  }

  /**
   * Create the space controllers section.
   *
   * @param controllerRepository
   *          repository for the controllers
   *
   * @return the element for all controllers
   */
  private Element newSpaceControllerEntries(SpaceControllerRepository controllerRepository) {
    Element controllersElement = new Element(ELEMENT_NAME_ROOT_SPACE_CONTROLLERS);

    for (SpaceController controller : controllerRepository.getAllSpaceControllers()) {
      controllersElement.addContent(newSpaceControllerEntry(controller));
    }

    return controllersElement;
  }

  /**
   * Create the entry for a specific space controller.
   *
   * @param controller
   *          the controller to write
   *
   * @return the element for the controller
   */
  private Element newSpaceControllerEntry(SpaceController controller) {
    Element controllerElement = new Element(ELEMENT_NAME_INDIVIDUAL_SPACE_CONTROLLER);
    controllerElement.setAttribute(ATTRIBUTE_NAME_ID, controller.getId())
        .addContent(new Element(ELEMENT_NAME_NAME).addContent(controller.getName()))
        .addContent(new Element(ELEMENT_NAME_DESCRIPTION).addContent(new CDATA(controller.getDescription())))
        .addContent(new Element(ELEMENT_NAME_SPACE_CONTROLLER_HOST_ID).addContent(controller.getHostId()))
        .addContent(new Element(ELEMENT_NAME_UUID).addContent(controller.getUuid()))
        .addContent(newMetadataElement(controller.getMetadata()));

    SpaceControllerMode mode = controller.getMode();
    if (mode != null) {
      controllerElement.addContent(new Element(ELEMENT_NAME_SPACE_CONTROLLER_MODE).addContent(mode.name()));
    }

    addSpaceControllerConfiguration(controllerElement, controller.getConfiguration());

    return controllerElement;
  }

  /**
   * Add a space controller configuration to the given element if there is a configuration.
   *
   * @param element
   *          the XML element to add the configuration onto
   * @param configuration
   *          the possible configuration (can be {@code null})
   */
  private void addSpaceControllerConfiguration(Element element, SpaceControllerConfiguration configuration) {
    if (configuration != null) {
      Element configurationElement = new Element(ELEMENT_NAME_SPACE_CONTROLLER_CONFIGURATION);
      element.addContent(configurationElement);

      Set<ConfigurationParameter> parameters = configuration.getParameters();
      if (!parameters.isEmpty()) {
        Element parametersElement = new Element(ELEMENT_NAME_SPACE_CONTROLLER_CONFIGURATION_ROOT_PARAMETERS);
        configurationElement.addContent(parametersElement);

        for (ConfigurationParameter parameter : parameters) {
          Element parameterElement = new Element(ELEMENT_NAME_SPACE_CONTROLLER_CONFIGURATION_INDIVIDUAL_PARAMETER);
          parametersElement.addContent(parameterElement);

          parameterElement.setAttribute(ATTRIBUTE_NAME_SPACE_CONTROLLER_CONFIGURATION_PARAMETER_NAME,
              parameter.getName()).addContent(new CDATA(parameter.getValue()));
        }
      }
    }
  }

  /**
   * Create the activities section.
   *
   * @param activityRepository
   *          repository for the activities
   *
   * @return the element for all activities
   */
  private Element newActivityEntries(ActivityRepository activityRepository) {
    Element activitiesElement = new Element(ELEMENT_NAME_ROOT_ACTIVITIES);

    for (Activity activity : activityRepository.getAllActivities()) {
      activitiesElement.addContent(newActivityEntry(activity));
    }

    return activitiesElement;
  }

  /**
   * Create the entry for a specific activity.
   *
   * @param activity
   *          the activity to write
   *
   * @return the element for the activity
   */
  private Element newActivityEntry(Activity activity) {
    Element activityElement = new Element(ELEMENT_NAME_INDIVIDUAL_ACTIVITY);

    activityElement.setAttribute(ATTRIBUTE_NAME_ID, activity.getId())
        .addContent(new Element(ELEMENT_NAME_ACTIVITY_IDENTIFYING_NAME).addContent(activity.getIdentifyingName()))
        .addContent(new Element(ELEMENT_NAME_ACTIVITY_VERSION).addContent(activity.getVersion()))
        .addContent(new Element(ELEMENT_NAME_NAME).addContent(activity.getName()))
        .addContent(new Element(ELEMENT_NAME_DESCRIPTION).addContent(new CDATA(activity.getDescription())))
        .addContent(newMetadataElement(activity.getMetadata()));

    Date lastUploadDate = activity.getLastUploadDate();
    if (lastUploadDate != null) {
      activityElement.setAttribute(ATTRIBUTE_NAME_LAST_UPLOAD_DATE, Long.toString(lastUploadDate.getTime()));
    }

    Date lastStartDate = activity.getLastStartDate();
    if (lastStartDate != null) {
      activityElement.setAttribute(ATTRIBUTE_NAME_LAST_START_DATE, Long.toString(lastStartDate.getTime()));
    }

    String bundleContentHash = activity.getBundleContentHash();
    if (bundleContentHash != null) {
      activityElement.addContent(new Element(ELEMENT_NAME_ACTIVITY_BUNDLE_CONTENT_HASH).addContent(bundleContentHash));
    }

    addActivityDependencies(activity, activityElement);

    return activityElement;
  }

  /**
   * Add the dependencies for an activity, if any.
   *
   * @param activity
   *          the activity with the dependencies
   * @param activityElement
   *          the XML element to hang the dependencies off of
   */
  protected void addActivityDependencies(Activity activity, Element activityElement) {
    List<? extends ActivityDependency> dependencies = activity.getDependencies();
    if (!dependencies.isEmpty()) {
      Element activityDependenciesElement = new Element(ELEMENT_NAME_ROOT_ACTIVITY_DEPENDENCIES);
      activityElement.addContent(activityDependenciesElement);
      for (ActivityDependency dependency : dependencies) {
        Element dependencyElement = new Element(ELEMENT_NAME_INDIVIDUAL_ACTIVITY_DEPENDENCY);
        activityDependenciesElement.addContent(dependencyElement);

        dependencyElement
            .addContent(new Element(ELEMENT_NAME_ACTIVITY_DEPENDENCY_NAME).addContent(dependency.getName()))
            .addContent(
                new Element(ELEMENT_NAME_ACTIVITY_DEPENDENCY_VERSION_MINIMUM).addContent(dependency.getMinimumVersion()))
            .addContent(
                new Element(ELEMENT_NAME_ACTIVITY_DEPENDENCY_VERSION_MAXIMUM).addContent(dependency.getMaximumVersion()))
            .addContent(
                new Element(ELEMENT_NAME_ACTIVITY_DEPENDENCY_REQUIRED).addContent(dependency.isRequired() ? VALUE_TRUE
                    : VALUE_FALSE));
      }
    }
  }

  /**
   * Create the live activities section.
   *
   * @param activityRepository
   *          repository for the activity entities
   *
   * @return the element for all live activities
   */
  private Element newLiveActivityEntries(ActivityRepository activityRepository) {
    Element activitiesElement = new Element(ELEMENT_NAME_ROOT_LIVE_ACTIVITIES);

    for (LiveActivity activity : activityRepository.getAllLiveActivities()) {
      activitiesElement.addContent(newLiveActivityEntry(activity));
    }

    return activitiesElement;
  }

  /**
   * Create the entry for a specific live activity.
   *
   * @param liveActivity
   *          the live activity to write
   *
   * @return the element for the activity
   */
  private Element newLiveActivityEntry(LiveActivity liveActivity) {
    Element liveActivityElement = new Element(ELEMENT_NAME_INDIVIDUAL_LIVE_ACTIVITY);

    liveActivityElement
        .setAttribute(ATTRIBUTE_NAME_ID, liveActivity.getId())
        .addContent(new Element(ELEMENT_NAME_UUID).addContent(liveActivity.getUuid()))
        .addContent(new Element(ELEMENT_NAME_NAME).addContent(liveActivity.getName()))
        .addContent(new Element(ELEMENT_NAME_DESCRIPTION).addContent(new CDATA(liveActivity.getDescription())))
        .addContent(
            new Element(ELEMENT_NAME_LIVE_ACTIVITY_CONTROLLER).setAttribute(ATTRIBUTE_NAME_ID, liveActivity
                .getController().getId()))
        .addContent(
            new Element(ELEMENT_NAME_LIVE_ACTIVITY_ACTIVITY).setAttribute(ATTRIBUTE_NAME_ID, liveActivity.getActivity()
                .getId())).addContent(newMetadataElement(liveActivity.getMetadata()));

    addActivityConfiguration(liveActivityElement, liveActivity.getConfiguration());

    Date lastDeployDate = liveActivity.getLastDeployDate();
    if (lastDeployDate != null) {
      liveActivityElement.addContent(new Element(ELEMENT_NAME_LIVE_ACTIVITY_LAST_DEPLOY_DATE).addContent(Long
          .toString(lastDeployDate.getTime())));
    }

    return liveActivityElement;
  }

  /**
   * Add an activity configuration to the given element if there is a configuration.
   *
   * @param element
   *          the XML element to add the configuration onto
   * @param configuration
   *          the possible configuration (can be {@code null})
   */
  private void addActivityConfiguration(Element element, ActivityConfiguration configuration) {
    if (configuration != null) {
      Element configurationElement = new Element(ELEMENT_NAME_ACTIVITY_CONFIGURATION);
      element.addContent(configurationElement);

      Set<ConfigurationParameter> parameters = configuration.getParameters();
      if (!parameters.isEmpty()) {
        Element parametersElement = new Element(ELEMENT_NAME_ACTIVITY_CONFIGURATION_ROOT_PARAMETERS);
        configurationElement.addContent(parametersElement);

        for (ConfigurationParameter parameter : parameters) {
          Element parameterElement = new Element(ELEMENT_NAME_ACTIVITY_CONFIGURATION_INDIVIDUAL_PARAMETER);
          parametersElement.addContent(parameterElement);

          parameterElement.setAttribute(ATTRIBUTE_NAME_ACTIVITY_CONFIGURATION_PARAMETER_NAME, parameter.getName())
              .addContent(new CDATA(parameter.getValue()));
        }
      }
    }
  }

  /**
   * Create the live activity groups section.
   *
   * @param activityRepository
   *          repository for the activity entities
   *
   * @return the element for all live activity groups
   */
  private Element newLiveActivityGroupEntries(ActivityRepository activityRepository) {
    Element groupsElement = new Element(ELEMENT_NAME_ROOT_LIVE_ACTIVITY_GROUPS);

    for (LiveActivityGroup group : activityRepository.getAllLiveActivityGroups()) {
      groupsElement.addContent(newLiveActivityGroupEntry(group));
    }

    return groupsElement;
  }

  /**
   * Create the entry for a specific live activity group.
   *
   * @param group
   *          the live activity group to write
   *
   * @return the element for the live activity group
   */
  private Element newLiveActivityGroupEntry(LiveActivityGroup group) {
    Element groupElement = new Element(ELEMENT_NAME_INDIVIDUAL_LIVE_ACTIVITY_GROUP);

    groupElement.setAttribute(ATTRIBUTE_NAME_ID, group.getId())
        .addContent(new Element(ELEMENT_NAME_NAME).addContent(new CDATA(group.getName())))
        .addContent(new Element(ELEMENT_NAME_DESCRIPTION).addContent(new CDATA(group.getDescription())))
        .addContent(newMetadataElement(group.getMetadata()));

    addLiveActivityGroupLiveActivities(group, groupElement);

    return groupElement;
  }

  /**
   * Add any live activities in the group.
   *
   * @param group
   *          the group which should have activities
   * @param groupElement
   *          the XML element for the group
   */
  private void addLiveActivityGroupLiveActivities(LiveActivityGroup group, Element groupElement) {
    List<? extends GroupLiveActivity> activities = group.getLiveActivities();
    if (!activities.isEmpty()) {
      Element activitiesElement = new Element(ELEMENT_NAME_LIVE_ACTIVITY_GROUP_ROOT_GROUP_LIVE_ACTIVITIES);
      groupElement.addContent(activitiesElement);

      for (GroupLiveActivity activity : activities) {
        Element activityElement = new Element(ELEMENT_NAME_LIVE_ACTIVITY_GROUP_INDIVIDUAL_GROUP_LIVE_ACTIVITY);
        activitiesElement.addContent(activityElement);

        activityElement.setAttribute(ATTRIBUTE_NAME_GROUP_LIVE_ACTIVITY_ID, activity.getActivity().getId())
            .setAttribute(ATTRIBUTE_NAME_GROUP_LIVE_ACTIVITY_DEPENDENCY, activity.getDependency().name());

      }
    }
  }

  /**
   * Create the spaces section.
   *
   * @param activityRepository
   *          repository for the space entities
   *
   * @return the element for all spaces
   */
  private Element newSpaceEntries(ActivityRepository activityRepository) {
    Element spacesElement = new Element(ELEMENT_NAME_ROOT_SPACES);

    Set<String> spacesDone = Sets.newHashSet();

    for (Space rootSpace : activityRepository.getAllSpaces()) {
      walkSpace(spacesElement, spacesDone, rootSpace);
    }

    return spacesElement;
  }

  /**
   * Do a depth first walk of space so get everything that needs to be defined written out before it is used.
   *
   * @param spacesElement
   *          element where spaces get attached
   * @param spacesDone
   *          IDs of the spaces which have been done
   * @param space
   *          the current space
   */
  private void walkSpace(Element spacesElement, Set<String> spacesDone, Space space) {
    if (!spacesDone.contains(space.getId())) {
      // Though there better not be circularly defined spaces, this will
      // help not get trapped by them.
      spacesDone.add(space.getId());

      for (Space subspace : space.getSpaces()) {
        walkSpace(spacesElement, spacesDone, subspace);
      }

      spacesElement.addContent(newSpaceEntry(space));
    }
  }

  /**
   * Create the entry for a specific space.
   *
   * @param space
   *          the space to write
   *
   * @return the element for the space
   */
  private Element newSpaceEntry(Space space) {
    Element spaceElement = new Element(ELEMENT_NAME_INDIVIDUAL_SPACE);

    spaceElement.setAttribute(ATTRIBUTE_NAME_ID, space.getId())
        .addContent(new Element(ELEMENT_NAME_NAME).addContent(new CDATA(space.getName())))
        .addContent(new Element(ELEMENT_NAME_DESCRIPTION).addContent(new CDATA(space.getDescription())))
        .addContent(newMetadataElement(space.getMetadata()));

    addSubspaces(space, spaceElement);
    addSpaceLiveActivityGroups(space, spaceElement);

    return spaceElement;
  }

  /**
   * Add any subspace elements needed.
   *
   * @param space
   *          the space which contains the subspaces
   * @param spaceElement
   *          the space element where the entries should be added
   */
  private void addSubspaces(Space space, Element spaceElement) {
    List<? extends Space> subspaces = space.getSpaces();
    if (!subspaces.isEmpty()) {
      Element subspacesElement = new Element(ELEMENT_NAME_SPACE_ROOT_SUBSPACES);
      spaceElement.addContent(subspacesElement);
      for (Space subspace : subspaces) {
        subspacesElement.addContent(new Element(ELEMENT_NAME_SPACE_INDIVIDUAL_SUBSPACE).setAttribute(ATTRIBUTE_NAME_ID,
            subspace.getId()));
      }
    }
  }

  /**
   * Add any live activity group elements needed from a space.
   *
   * @param space
   *          the space which contains the live activity groups
   * @param spaceElement
   *          the space element where the entries should be added
   */
  private void addSpaceLiveActivityGroups(Space space, Element spaceElement) {
    List<? extends LiveActivityGroup> groups = space.getActivityGroups();
    if (!groups.isEmpty()) {
      Element groupsElement = new Element(ELEMENT_NAME_SPACE_ROOT_LIVE_ACTIVITY_GROUPS);
      spaceElement.addContent(groupsElement);
      for (LiveActivityGroup group : groups) {
        groupsElement.addContent(new Element(ELEMENT_NAME_SPACE_INDIVIDUAL_LIVE_ACTIVITY_GROUP).setAttribute(
            ATTRIBUTE_NAME_ID, group.getId()));
      }
    }
  }

  /**
   * Create all named script entries.
   *
   * @param automationRepository
   *          repository which contains the scripts
   *
   * @return an XML element for all master scripts
   */
  private Element newNamedScriptEntries(AutomationRepository automationRepository) {
    Element scriptsElement = new Element(ELEMENT_NAME_ROOT_NAMED_SCRIPTS);

    for (NamedScript script : automationRepository.getAllNamedScripts()) {
      scriptsElement.addContent(newNamedScriptEntry(script));
    }

    return scriptsElement;
  }

  /**
   * Create the entry for a specific named script.
   *
   * @param script
   *          the named script to write
   *
   * @return the XML element for the named script
   */
  private Element newNamedScriptEntry(NamedScript script) {
    Element scriptElement = new Element(ELEMENT_NAME_INDIVIDUAL_NAMED_SCRIPT);

    scriptElement.setAttribute(ATTRIBUTE_NAME_ID, script.getId())
        .addContent(new Element(ELEMENT_NAME_NAME).addContent(new CDATA(script.getName())))
        .addContent(new Element(ELEMENT_NAME_DESCRIPTION).addContent(new CDATA(script.getDescription())))
        .addContent(new Element(ELEMENT_NAME_NAMED_SCRIPT_LANGUAGE).addContent(script.getLanguage()))
        .addContent(new Element(ELEMENT_NAME_NAMED_SCRIPT_CONTENT).addContent(new CDATA(script.getContent())))
        .addContent(newMetadataElement(script.getMetadata()));

    Element scheduleElement =
        new Element(ELEMENT_NAME_NAMED_SCRIPT_SCHEDULE).addContent(new CDATA(script.getSchedule()));
    scheduleElement.addContent(scheduleElement);
    scheduleElement.setAttribute(ATTRIBUTE_NAME_NAMED_SCRIPT_SCHEDULE_SCHEDULED, script.getScheduled() ? VALUE_TRUE
        : VALUE_FALSE);

    return scriptElement;
  }

  /**
   * Create a metadata element.
   *
   * @param metadata
   *          the metadata to create the XML for
   *
   * @return the XML description of the metadata
   */
  private Element newMetadataElement(Map<String, Object> metadata) {
    Element metadataElement = new Element(ELEMENT_NAME_METADATA);

    for (Entry<String, Object> entry : metadata.entrySet()) {
      metadataElement.addContent(new Element(ELEMENT_NAME_METADATA_ITEM).setAttribute(
          ATTRIBUTE_NAME_METADATA_ITEM_NAME, entry.getKey()).addContent(new CDATA(entry.getValue().toString())));
    }

    return metadataElement;
  }
}
