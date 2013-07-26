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
import interactivespaces.domain.basic.ActivityDependency;

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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * A JPA implementation of an {@link Activity}.
 *
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "activities")
@NamedQueries({
    @NamedQuery(name = "activityAll", query = "select a from JpaActivity a"),
    @NamedQuery(
        name = "activityByNameAndVersion",
        query = "select a from JpaActivity a where a.identifyingName = :identifyingName and a.version = :version") })
public class JpaActivity implements Activity {

  /**
   * For serialization.
   */

  /**
   * The persistence ID for the activity.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false, length = 64)
  private String id;

  /**
   * The identifying identifyingName of the activity.
   */
  @Column(nullable = false, length = 512)
  private String identifyingName;

  /**
   * The descriptive identifyingName of the activity.
   */
  @Column(nullable = false, length = 512)
  private String name;

  /**
   * The description of the activity.
   */
  @Column(nullable = true, length = 2048)
  private String description;

  /**
   * Version of the activity.
   */
  @Column(nullable = false, length = 32)
  private String version;

  /**
   * When the activity was last uploaded.
   */
  @Column(nullable = true)
  private Date lastUploadDate;

  /**
   * The dependencies the activity has.
   */
  @OneToMany(targetEntity = JpaActivityDependency.class, cascade = CascadeType.ALL,
      fetch = FetchType.EAGER, orphanRemoval = true)
  private List<JpaActivityDependency> dependencies = Lists.newArrayList();

  /**
   * The metadata.
   */
  @OneToMany(targetEntity = JpaActivityMetadataItem.class, cascade = CascadeType.ALL,
      fetch = FetchType.EAGER, orphanRemoval = true)
  private List<JpaActivityMetadataItem> metadata = Lists.newArrayList();

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
  public String getIdentifyingName() {
    return identifyingName;
  }

  @Override
  public void setIdentifyingName(String identifyingName) {
    this.identifyingName = identifyingName;
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
  public String getVersion() {
    return version;
  }

  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public Date getLastUploadDate() {
    return lastUploadDate;
  }

  @Override
  public void setLastUploadDate(Date lastUploadDate) {
    this.lastUploadDate = lastUploadDate;
  }

  @Override
  public void setDependencies(List<ActivityDependency> newDependencies) {
    synchronized (dependencies) {
      dependencies.clear();
      for (ActivityDependency d : newDependencies) {
        JpaActivityDependency dependency = (JpaActivityDependency) d;

        dependency.setActivity(this);
        dependencies.add(dependency);
      }
    }
  }

  @Override
  public List<? extends ActivityDependency> getDependencies() {
    synchronized (dependencies) {
      return Lists.newArrayList(dependencies);
    }
  }

  @Override
  public void setMetadata(Map<String, Object> m) {
    synchronized (metadata) {
      metadata.clear();

      for (Entry<String, Object> entry : m.entrySet()) {
        metadata
            .add(new JpaActivityMetadataItem(this, entry.getKey(), entry.getValue().toString()));
      }
    }
  }

  @Override
  public Map<String, Object> getMetadata() {
    synchronized (metadata) {
      Map<String, Object> result = Maps.newHashMap();

      for (JpaActivityMetadataItem item : metadata) {
        result.put(item.getName(), item.getValue());
      }

      return result;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "JpaActivity [id=" + id + ", identifyingName=" + identifyingName + ", name=" + name
        + ", description=" + description + ", version=" + version + ", lastUploadDate="
        + lastUploadDate + ", dependencies=" + dependencies + ", metadata=" + getMetadata() + "]";
  }
}
