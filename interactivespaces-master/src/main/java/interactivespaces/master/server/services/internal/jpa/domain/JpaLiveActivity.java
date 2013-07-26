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

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.SpaceController;

import java.util.Date;
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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

/**
 * A JPA implementation of a {@link LiveActivity}.
 *
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "live_activities", uniqueConstraints = @UniqueConstraint(
    name = "LIVE_ACTIVITY_UUID_CNSTR", columnNames = "UUID"))
@NamedQueries({
    @NamedQuery(name = "liveActivityAll", query = "select la from JpaLiveActivity la"),
    @NamedQuery(name = "liveActivityByUuid",
        query = "select la from JpaLiveActivity la where la.uuid = :uuid"),
    @NamedQuery(name = "liveActivityByActivity",
        query = "select la from JpaLiveActivity la where la.activity.id = :activity_id"),
    @NamedQuery(name = "countLiveActivityByActivity",
        query = "select count(la) from JpaLiveActivity la where la.activity.id = :activity_id"),
    @NamedQuery(name = "liveActivityByController",
        query = "select la from JpaLiveActivity la where la.controller.id = :controller_id"),
    @NamedQuery(name = "countLiveActivityByController",
        query = "select count(la) from JpaLiveActivity la where la.controller.id = :controller_id"), })
public class JpaLiveActivity implements LiveActivity {
  /**
   * For serialization.
   */

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false, length = 64)
  private String id;

  /**
   * The UUID of the activity.
   */
  @Column(nullable = false, length = 64)
  private String uuid;

  /**
   * The controller this activity is installed on.
   */
  @ManyToOne(optional = true, fetch = FetchType.EAGER)
  private JpaSpaceController controller;

  /**
   * The activity this is an instance of.
   */
  @ManyToOne(optional = true, fetch = FetchType.EAGER)
  private JpaActivity activity;

  /**
   * A name for this live activity.
   */
  @Column(nullable = false, length = 512)
  private String name;

  /**
   * A description of this installed activity.
   */
  @Column(nullable = true, length = 2048)
  private String description;

  /**
   * The activity configuration for this installed activity.
   */
  // TODO(keith): make configurations completely separately contained items
  // that aren't dependent on their containing object so can have catalogs of
  // them.
  @ManyToOne(optional = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private JpaActivityConfiguration configuration;

  /**
   * The last date the activity was deployed to the controller.
   *
   * <p>
   * {@code null} if never deployed
   */
  @Column(nullable = true)
  private Date lastDeployDate;

  /**
   * The metadata.
   */
  @OneToMany(targetEntity = JpaLiveActivityMetadataItem.class, cascade = CascadeType.ALL,
      fetch = FetchType.EAGER, orphanRemoval = true)
  private List<JpaLiveActivityMetadataItem> metadata = Lists.newArrayList();

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
  public Activity getActivity() {
    return activity;
  }

  @Override
  public LiveActivity setActivity(Activity activity) {
    this.activity = (JpaActivity) activity;

    return this;
  }

  @Override
  public String getUuid() {
    return uuid;
  }

  @Override
  public LiveActivity setUuid(String uuid) {
    this.uuid = uuid;

    return this;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public LiveActivity setName(String name) {
    this.name = name;

    return this;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public LiveActivity setDescription(String description) {
    this.description = description;

    return this;
  }

  @Override
  public SpaceController getController() {
    return controller;
  }

  @Override
  public LiveActivity setController(SpaceController controller) {
    this.controller = (JpaSpaceController) controller;

    return this;
  }

  @Override
  public ActivityConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public void setConfiguration(ActivityConfiguration configuration) {
    this.configuration = (JpaActivityConfiguration) configuration;
  }

  @Override
  public Date getLastDeployDate() {
    return lastDeployDate;
  }

  @Override
  public void setLastDeployDate(Date lastDeployDate) {
    this.lastDeployDate = lastDeployDate;
  }

  @Override
  public boolean isOutOfDate() {
    Date date = getLastDeployDate();
    if (date != null) {
      return activity.getLastUploadDate().after(date);
    } else {
      // Never been deployed.
      return true;
    }
  }

  @Override
  public void setMetadata(Map<String, Object> m) {

    synchronized (metadata) {
      metadata.clear();

      for (Entry<String, Object> entry : m.entrySet()) {
        metadata.add(new JpaLiveActivityMetadataItem(this, entry.getKey(), entry.getValue()
            .toString()));
      }
    }
  }

  @Override
  public Map<String, Object> getMetadata() {
    synchronized (metadata) {
      Map<String, Object> result = Maps.newHashMap();

      for (JpaLiveActivityMetadataItem item : metadata) {
        result.put(item.getName(), item.getValue());
      }

      return result;
    }
  }

  @Override
  public String toString() {
    return "JpaLiveActivity [id=" + id + ", uuid=" + uuid + ", name=" + name + ", description="
        + description + ", lastDeployDate=" + lastDeployDate + ", metadata=" + getMetadata()
        + ", configuration=" + configuration + ", controller=" + controller + ", activity="
        + activity + "]";
  }
}
