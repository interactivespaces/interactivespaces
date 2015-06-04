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

package interactivespaces.master.server.services.internal.jpa;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.ActivityDependency;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleLiveActivity;
import interactivespaces.domain.space.Space;
import interactivespaces.expression.FilterExpression;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.internal.jpa.domain.JpaActivity;
import interactivespaces.master.server.services.internal.jpa.domain.JpaActivityConfiguration;
import interactivespaces.master.server.services.internal.jpa.domain.JpaActivityConfigurationParameter;
import interactivespaces.master.server.services.internal.jpa.domain.JpaActivityDependency;
import interactivespaces.master.server.services.internal.jpa.domain.JpaLiveActivity;
import interactivespaces.master.server.services.internal.jpa.domain.JpaLiveActivityGroup;
import interactivespaces.master.server.services.internal.jpa.domain.JpaSpace;
import interactivespaces.util.uuid.UuidGenerator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.springframework.orm.jpa.JpaTemplate;

import java.util.List;
import java.util.Map;

/**
 * A JPA implementation of {@link ActivityRepository}.
 *
 * @author Keith M. Hughes
 */
public class JpaActivityRepository implements ActivityRepository {

  /**
   * The UUID generator to use.
   */
  private UuidGenerator uuidGenerator;

  /**
   * The Spring JPA template.
   */
  private JpaTemplate template;

  @Override
  public Activity newActivity() {
    return new JpaActivity();
  }

  @Override
  public ActivityDependency newActivityDependency() {
    return new JpaActivityDependency();
  }

  @Override
  public ActivityConfiguration newActivityConfiguration() {
    return new JpaActivityConfiguration();
  }

  @Override
  public ConfigurationParameter newActivityConfigurationParameter() {
    return new JpaActivityConfigurationParameter();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Activity> getAllActivities() {
    return template.findByNamedQuery("activityAll");
  }

  @Override
  public List<Activity> getActivities(FilterExpression filter) {
    @SuppressWarnings("unchecked")
    List<Activity> activities = template.findByNamedQuery("activityAll");

    List<Activity> results = Lists.newArrayList();

    for (Activity activity : activities) {
      if (filter.accept(activity)) {
        results.add(activity);
      }
    }

    return results;
  }

  @Override
  public Activity getActivityById(String id) {
    return template.find(JpaActivity.class, id);
  }

  @Override
  public Activity getActivityByNameAndVersion(String identifyingName, String version) {
    Map<String, String> params = Maps.newHashMap();
    params.put("identifyingName", identifyingName);
    params.put("version", version);
    @SuppressWarnings("unchecked")
    List<Activity> results = template.findByNamedQueryAndNamedParams("activityByNameAndVersion", params);
    if (!results.isEmpty()) {
      return results.get(0);
    } else {
      return null;
    }
  }

  @Override
  public Activity saveActivity(Activity activity) {
    if (activity.getId() != null) {
      return template.merge(activity);
    } else {
      template.persist(activity);
      return activity;
    }
  }

  @Override
  public void deleteActivity(Activity activity) {
    long count = getNumberLiveActivitiesByActivity(activity);
    if (count == 0) {
      template.remove(activity);
    } else {
      throw new InteractiveSpacesException(String.format("Cannot delete activity %s, it is in %d live activities",
          activity.getId(), count));
    }
  }

  @Override
  public LiveActivity newLiveActivity() {
    LiveActivity lactivity = new JpaLiveActivity();
    lactivity.setUuid(uuidGenerator.newUuid());

    return lactivity;
  }

  @Override
  public LiveActivity newAndSaveLiveActivity(SimpleLiveActivity liveActivityTemplate) {
    LiveActivity newLiveActivity = newLiveActivity();
    newLiveActivity.setActivity(liveActivityTemplate.getActivity());
    newLiveActivity.setController(liveActivityTemplate.getController());
    newLiveActivity.setName(liveActivityTemplate.getName());
    newLiveActivity.setDescription(liveActivityTemplate.getDescription());

    return saveLiveActivity(newLiveActivity);
  }

  @Override
  public long getNumberActivities() {
    @SuppressWarnings("unchecked")
    List<Long> results = template.findByNamedQuery("countActivityAll");
    return results.get(0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<LiveActivity> getAllLiveActivities() {
    return template.findByNamedQuery("liveActivityAll");
  }

  @Override
  public List<LiveActivity> getLiveActivities(FilterExpression filter) {
    @SuppressWarnings("unchecked")
    List<LiveActivity> activities = template.findByNamedQuery("liveActivityAll");

    List<LiveActivity> results = Lists.newArrayList();

    for (LiveActivity activity : activities) {
      if (filter.accept(activity)) {
        results.add(activity);
      }
    }

    return results;
  }

  @Override
  public LiveActivity getLiveActivityById(String id) {
    return template.find(JpaLiveActivity.class, id);
  }

  @Override
  public List<LiveActivity> getLiveActivitiesByController(SpaceController controller) {
    Map<String, String> params = Maps.newHashMap();
    params.put("controller_id", controller.getId());
    @SuppressWarnings("unchecked")
    List<LiveActivity> results = template.findByNamedQueryAndNamedParams("liveActivityByController", params);
    return results;
  }

  @Override
  public long getNumberLiveActivitiesByController(SpaceController controller) {
    Map<String, String> params = Maps.newHashMap();
    params.put("controller_id", controller.getId());
    @SuppressWarnings("unchecked")
    List<Long> results = template.findByNamedQueryAndNamedParams("countLiveActivityByController", params);
    return results.get(0);
  }

  @Override
  public List<LiveActivity> getLiveActivitiesByActivity(Activity activity) {
    Map<String, String> params = Maps.newHashMap();
    params.put("activity_id", activity.getId());
    @SuppressWarnings("unchecked")
    List<LiveActivity> results = template.findByNamedQueryAndNamedParams("liveActivityByActivity", params);
    return results;
  }

  @Override
  public long getNumberLiveActivitiesByActivity(Activity activity) {
    Map<String, String> params = Maps.newHashMap();
    params.put("activity_id", activity.getId());
    @SuppressWarnings("unchecked")
    List<Long> results = template.findByNamedQueryAndNamedParams("countLiveActivityByActivity", params);
    return results.get(0);
  }

  @Override
  public LiveActivity getLiveActivityByUuid(String uuid) {
    Map<String, String> params = Maps.newHashMap();
    params.put("uuid", uuid);
    @SuppressWarnings("unchecked")
    List<LiveActivity> results = template.findByNamedQueryAndNamedParams("liveActivityByUuid", params);
    if (!results.isEmpty()) {
      return results.get(0);
    } else {
      return null;
    }
  }

  @Override
  public LiveActivity saveLiveActivity(LiveActivity activity) {
    if (activity.getId() != null) {
      return template.merge(activity);
    } else {
      template.persist(activity);
      return activity;
    }
  }

  @Override
  public void deleteLiveActivity(LiveActivity activity) {
    long count = getNumberLiveActivityGroupsByLiveActivity(activity);
    if (count == 0) {
      template.remove(activity);
    } else {
      throw new InteractiveSpacesException(String.format(
          "Cannot delete live activity %s, it is in %d live activity groups", activity.getId(), count));
    }
  }

  @Override
  public LiveActivityGroup newLiveActivityGroup() {
    return new JpaLiveActivityGroup();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<LiveActivityGroup> getAllLiveActivityGroups() {
    return template.findByNamedQuery("liveActivityGroupAll");
  }

  @Override
  public List<LiveActivityGroup> getLiveActivityGroups(FilterExpression filter) {
    @SuppressWarnings("unchecked")
    List<LiveActivityGroup> groups = template.findByNamedQuery("liveActivityGroupAll");

    List<LiveActivityGroup> results = Lists.newArrayList();

    for (LiveActivityGroup group : groups) {
      if (filter.accept(group)) {
        results.add(group);
      }
    }

    return results;
  }

  @Override
  public LiveActivityGroup getLiveActivityGroupById(String id) {
    return template.find(JpaLiveActivityGroup.class, id);
  }

  @Override
  public List<LiveActivityGroup> getLiveActivityGroupsByLiveActivity(LiveActivity liveActivity) {
    Map<String, String> params = Maps.newHashMap();
    params.put("activity_id", liveActivity.getId());
    @SuppressWarnings("unchecked")
    List<LiveActivityGroup> results =
        template.findByNamedQueryAndNamedParams("liveActivityGroupByLiveActivity", params);
    return results;
  }

  @Override
  public long getNumberLiveActivityGroupsByLiveActivity(LiveActivity liveActivity) {
    Map<String, String> params = Maps.newHashMap();
    params.put("activity_id", liveActivity.getId());
    @SuppressWarnings("unchecked")
    List<Long> results = template.findByNamedQueryAndNamedParams("countLiveActivityGroupByLiveActivity", params);
    return results.get(0);
  }

  @Override
  public LiveActivityGroup saveLiveActivityGroup(LiveActivityGroup liveActivityGroup) {
    if (liveActivityGroup.getId() != null) {
      return template.merge(liveActivityGroup);
    } else {
      template.persist(liveActivityGroup);
      return liveActivityGroup;
    }
  }

  @Override
  public void deleteLiveActivityGroup(LiveActivityGroup liveActivityGroup) {
    long count = getNumberSpacesByLiveActivityGroup(liveActivityGroup);
    if (count == 0) {
      template.remove(liveActivityGroup);
    } else {
      throw new InteractiveSpacesException(String.format("Cannot delete live activity group %s, it is in %d spaces",
          liveActivityGroup.getId(), count));
    }
  }

  @Override
  public Space newSpace() {
    return new JpaSpace();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Space> getAllSpaces() {
    return template.findByNamedQuery("spaceAll");
  }

  @Override
  public List<Space> getSpaces(FilterExpression filter) {
    @SuppressWarnings("unchecked")
    List<Space> spaces = template.findByNamedQuery("spaceAll");

    List<Space> results = Lists.newArrayList();

    for (Space space : spaces) {
      if (filter.accept(space)) {
        results.add(space);
      }
    }
    return results;
  }

  @Override
  public Space getSpaceById(String id) {
    return template.find(JpaSpace.class, id);
  }

  @Override
  public List<Space> getSpacesByLiveActivityGroup(LiveActivityGroup liveActivityGroup) {
    Map<String, String> params = Maps.newHashMap();
    params.put("live_activity_group_id", liveActivityGroup.getId());
    @SuppressWarnings("unchecked")
    List<Space> results = template.findByNamedQueryAndNamedParams("spaceByLiveActivityGroup", params);

    return results;
  }

  @Override
  public long getNumberSpacesByLiveActivityGroup(LiveActivityGroup liveActivityGroup) {
    Map<String, String> params = Maps.newHashMap();
    params.put("live_activity_group_id", liveActivityGroup.getId());
    @SuppressWarnings("unchecked")
    List<Long> results = template.findByNamedQueryAndNamedParams("countSpaceByLiveActivityGroup", params);

    return results.get(0);
  }

  @Override
  public List<Space> getSpacesBySubspace(Space subspace) {
    Map<String, String> params = Maps.newHashMap();
    params.put("subspace_id", subspace.getId());
    @SuppressWarnings("unchecked")
    List<Space> results = template.findByNamedQueryAndNamedParams("spaceBySubspace", params);

    return results;
  }

  @Override
  public long getNumberSpacesBySubspace(Space subspace) {
    Map<String, String> params = Maps.newHashMap();
    params.put("subspace_id", subspace.getId());
    @SuppressWarnings("unchecked")
    List<Long> results = template.findByNamedQueryAndNamedParams("countSpaceBySubspace", params);

    return results.get(0);
  }

  @Override
  public Space saveSpace(Space space) {
    if (space.getId() != null) {
      return template.merge(space);
    } else {
      template.persist(space);
      return space;
    }
  }

  @Override
  public void deleteSpace(Space space) {
    long count = getNumberSpacesBySubspace(space);
    if (count == 0) {
      template.remove(space);
    } else {
      throw new InteractiveSpacesException(String.format("Cannot delete space %s, it is in %d subspaces",
          space.getId(), count));
    }
  }

  /**
   * @param uuidGenerator
   *          the uuidGenerator to set
   */
  public void setUuidGenerator(UuidGenerator uuidGenerator) {
    this.uuidGenerator = uuidGenerator;
  }

  /**
   * @param template
   *          the template to set
   */
  public void setTemplate(JpaTemplate template) {
    this.template = template;
  }
}
