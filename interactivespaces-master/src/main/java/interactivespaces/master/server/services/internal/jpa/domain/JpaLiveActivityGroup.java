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

package interactivespaces.master.server.services.internal.jpa.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.GroupLiveActivity.GroupLiveActivityDependency;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * A JPA implementation of a {@link LiveActivityGroup}.
 *
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "live_activity_groups")
@NamedQueries({
    @NamedQuery(name = "liveActivityGroupAll", query = "select g from JpaLiveActivityGroup g"),
    @NamedQuery(
        name = "liveActivityGroupByLiveActivity",
        query = "select distinct gla.activityGroup from JpaGroupLiveActivity gla where gla.activity.id = :activity_id"),
    @NamedQuery(
        name = "countLiveActivityGroupByLiveActivity",
        query = "select count(distinct gla.activityGroup) from JpaGroupLiveActivity gla where gla.activity.id = :activity_id"), })
public class JpaLiveActivityGroup implements LiveActivityGroup {

  /**
   * The persistence ID for the live activity group.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false, length = 64)
  private String id;

  /**
   * The name of the group.
   */
  @Column(nullable = false, length = 512)
  private String name;

  /**
   * The description of the group.
   */
  @Column(nullable = true, length = 2048)
  private String description;

  /**
   * All live activities installed in the activity group.
   */
  @OneToMany(targetEntity = JpaGroupLiveActivity.class, cascade = CascadeType.ALL,
      fetch = FetchType.EAGER, orphanRemoval = true)
  private List<JpaGroupLiveActivity> activities = Lists.newArrayList();

  /**
   * The metadata.
   */
  @OneToMany(targetEntity = JpaLiveActivityGroupMetadataItem.class, cascade = CascadeType.ALL,
      fetch = FetchType.EAGER, orphanRemoval = true)
  private List<JpaLiveActivityGroupMetadataItem> metadata = Lists.newArrayList();

  /**
   * The database version. Used for detecting concurrent modifications.
   */
  @Version
  private long databaseVersion;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public List<? extends GroupLiveActivity> getActivities() {
    synchronized (activities) {
      return Lists.newArrayList(activities);
    }
  }

  @Override
  public LiveActivityGroup addActivity(LiveActivity activity) {
    return addActivity(activity, GroupLiveActivityDependency.REQUIRED);
  }

  @Override
  public LiveActivityGroup
      addActivity(LiveActivity activity, GroupLiveActivityDependency dependency) {
    synchronized (activities) {
      for (GroupLiveActivity ga : getActivities()) {
        if (ga.getActivity().equals(activity))
          throw new InteractiveSpacesException("Group already contains activity");
      }

      activities.add(new JpaGroupLiveActivity(this, (JpaLiveActivity) activity, dependency));
    }

    return this;
  }

  @Override
  public void removeActivity(LiveActivity activity) {
    synchronized (activities) {
      for (GroupLiveActivity gactivity : activities) {
        if (activity.equals(gactivity.getActivity())) {
          activities.remove(activity);

          return;
        }
      }
    }
  }

  @Override
  public void clearActivities() {
    synchronized (activities) {
      activities.clear();
    }
  }

  @Override
  public void setMetadata(Map<String, Object> m) {

    synchronized (metadata) {
      metadata.clear();

      for (Entry<String, Object> entry : m.entrySet()) {
        metadata.add(new JpaLiveActivityGroupMetadataItem(this, entry.getKey(), entry.getValue()
            .toString()));
      }
    }
  }

  @Override
  public Map<String, Object> getMetadata() {
    synchronized (metadata) {
      Map<String, Object> result = Maps.newHashMap();

      for (JpaLiveActivityGroupMetadataItem item : metadata) {
        result.put(item.getName(), item.getValue());
      }

      return result;
    }
  }

  @Override
  public String toString() {
    return "JpaLiveActivityGroup [id=" + id + ", name=" + name + ", description=" + description
        + ", metadata=" + getMetadata() + ", activities=" + activities + "]";
  }
}
