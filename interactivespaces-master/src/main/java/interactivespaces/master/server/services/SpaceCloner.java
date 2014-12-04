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

package interactivespaces.master.server.services;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.space.Space;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Clone a space.
 *
 * <p>
 * An instance of this class should be used for only 1 cloning operation as it keeps much state.
 *
 * @author Keith M. Hughes
 */
public class SpaceCloner {

  /**
   * The activity repository to use during cloning.
   */
  private ActivityRepository activityRepository;

  /**
   * A map from the old live activity to the cloned live activity.
   */
  private Map<String, LiveActivity> liveActivityClones = Maps.newHashMap();

  /**
   * A map from the old live activity group to the cloned live activity group.
   */
  private Map<String, LiveActivityGroup> liveActivityGroupClones = Maps.newHashMap();

  /**
   * A map from the old space to the cloned space.
   */
  private Map<String, Space> spaceClones = Maps.newLinkedHashMap();

  /**
   * A map from the old controller to the controller it should go to.
   */
  private Map<SpaceController, SpaceController> controllerMap;

  /**
   * The name prefix to put on all cloned items.
   */
  private String namePrefix;

  /**
   * Construct a cloner.
   *
   * @param activityRepository
   *          the activity repository to use during cloning
   */
  public SpaceCloner(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * Set the name prefix for each created entity.
   *
   * @param namePrefix
   *          the namePrefix to set
   */
  public void setNamePrefix(String namePrefix) {
    this.namePrefix = namePrefix;
  }

  /**
   * Set the map from controllers in the old space to controllers in the new space.
   *
   * @param controllerMap
   *          keys are the old controller, values are the controller to replace (can be {@code null})
   */
  public void setControllerMap(Map<SpaceController, SpaceController> controllerMap) {
    this.controllerMap = controllerMap;
  }

  /**
   * Save all generated clones in their respective repositories.
   */
  public void saveClones() {
    for (LiveActivity liveActivity : liveActivityClones.values()) {
      activityRepository.saveLiveActivity(liveActivity);
    }
    for (LiveActivityGroup liveActivityGroup : liveActivityGroupClones.values()) {
      activityRepository.saveLiveActivityGroup(liveActivityGroup);
    }
    for (Space space : spaceClones.values()) {
      activityRepository.saveSpace(space);
    }
  }

  /**
   * Clone a live activity.
   *
   * @param src
   *          the source live activity
   *
   * @return the cloned live activity
   */
  public LiveActivity cloneLiveActivity(LiveActivity src) {
    String id = src.getId();
    LiveActivity clone = liveActivityClones.get(id);
    if (clone != null) {
      return clone;
    }

    clone = activityRepository.newLiveActivity();
    liveActivityClones.put(id, clone);

    Activity activity = src.getActivity();
    clone.setName(namePrefix + " " + activity.getName());
    clone.setDescription(src.getDescription());
    clone.setActivity(activity);
    clone.setController(getController(src.getController()));
    clone.setMetadata(src.getMetadata());
    clone.setConfiguration(cloneConfiguration(src.getConfiguration()));

    return clone;
  }

  /**
   * Clone a configuration.
   *
   * @param src
   *          the configuration to clone
   *
   * @return the cloned configuration
   */
  public ActivityConfiguration cloneConfiguration(ActivityConfiguration src) {
    if (src == null) {
      return null;
    }

    ActivityConfiguration clone = activityRepository.newActivityConfiguration();
    String name = src.getName();
    if (name != null) {
      clone.setName(namePrefix + " " + name);
      clone.setDescription(src.getDescription());
    }

    clone.setDescription(src.getDescription());

    for (ConfigurationParameter parameter : src.getParameters()) {
      ConfigurationParameter clonedParameter = activityRepository.newActivityConfigurationParameter();
      clonedParameter.setName(parameter.getName());
      clonedParameter.setValue(parameter.getValue());
      clone.addParameter(clonedParameter);
    }

    return clone;
  }

  /**
   * Clone a live activity group.
   *
   * @param src
   *          the source live activity group
   *
   * @return the cloned live activity group
   */
  public LiveActivityGroup cloneLiveActivityGroup(LiveActivityGroup src) {
    String id = src.getId();
    LiveActivityGroup clone = liveActivityGroupClones.get(id);
    if (clone != null) {
      return clone;
    }

    clone = activityRepository.newLiveActivityGroup();
    liveActivityGroupClones.put(id, clone);

    clone.setName(namePrefix + " " + src.getName());
    clone.setDescription(src.getDescription());
    clone.setMetadata(src.getMetadata());

    for (GroupLiveActivity activity : src.getLiveActivities()) {
      LiveActivity activityClone = cloneLiveActivity(activity.getActivity());
      clone.addLiveActivity(activityClone, activity.getDependency());
    }

    return clone;
  }

  /**
   * Clone a space.
   *
   * @param src
   *          the source space
   *
   * @return the cloned space
   */
  public Space cloneSpace(Space src) {
    String id = src.getId();
    Space clone = spaceClones.get(id);
    if (clone != null) {
      return clone;
    }

    clone = activityRepository.newSpace();

    clone.setName(namePrefix + " " + src.getName());
    clone.setDescription(src.getDescription());
    clone.setMetadata(src.getMetadata());

    for (Space subspace : src.getSpaces()) {
      Space subspaceClone = cloneSpace(subspace);
      clone.addSpace(subspaceClone);
    }

    for (LiveActivityGroup group : src.getActivityGroups()) {
      LiveActivityGroup groupClone = cloneLiveActivityGroup(group);
      clone.addActivityGroup(groupClone);
    }

    spaceClones.put(id, clone);

    return clone;
  }

  /**
   * Get the controller to replace the old controller.
   *
   * @param oldController
   *          the controller from the source elements
   *
   * @return the controller which should be used
   */
  private SpaceController getController(SpaceController oldController) {
    if (controllerMap != null) {
      SpaceController newController = controllerMap.get(oldController);
      if (newController != null) {
        return newController;
      }
    }

    return oldController;
  }
}
