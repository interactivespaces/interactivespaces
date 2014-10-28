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

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.SpaceControllerMode;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

/**
 * A JPA implementation of a {@link SpaceController}.
 *
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "space_controllers", uniqueConstraints = @UniqueConstraint(name = "SPACE_CONTROLLER_UUID_CNSTR",
    columnNames = "UUID"))
@NamedQueries({ @NamedQuery(name = "spaceControllerAll", query = "select c from JpaSpaceController c"),
    @NamedQuery(name = "spaceControllerByUuid", query = "select c from JpaSpaceController c where c.uuid = :uuid"),
    @NamedQuery(name = "countSpaceControllerAll", query = "select count(c) from JpaSpaceController c"), })
public class JpaSpaceController implements SpaceController {

  /**
   * Column length to use for the ID.
   */
  public static final int ID_COLUMN_LENGTH = 64;

  /**
   * Column length to use for the hostId.
   */
  public static final int HOSTID_COLUMN_LENGTH = 512;

  /**
   * Column length to use for the uuid.
   */
  public static final int UUID_COLUMN_LENGTH = 64;

  /**
   * Column length to use for the name.
   */
  public static final int NAME_COLUMN_LENGTH = 512;

  /**
   * Column length to use for the description.
   */
  public static final int DESCRIPTION_COLUMN_LENGTH = 2048;

  /**
   * Column length to use for ID.
   */
  public static final int MODE_COLUMN_LENGTH = 32;

  /**
   * The persistence ID for the space controller.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false, length = ID_COLUMN_LENGTH)
  private String id;

  /**
   * ID of the host. This should be usable to find the host in the network.
   */
  @Column(nullable = false, length = HOSTID_COLUMN_LENGTH)
  private String hostId;

  /**
   * UUID of the controller.
   */
  @Column(nullable = false, length = UUID_COLUMN_LENGTH)
  private String uuid;

  /**
   * Name of the controller.
   */
  @Column(nullable = false, length = NAME_COLUMN_LENGTH)
  private String name;

  /**
   * Description of the controller.
   */
  @Column(nullable = true, length = DESCRIPTION_COLUMN_LENGTH)
  private String description;

  /**
   * Mode of the controller (e.g., active, inactive, deprecated).
   */
  @Enumerated(EnumType.STRING)
  private SpaceControllerMode mode = SpaceControllerMode.ENABLED;

  /**
   * The configuration for this space controller.
   */
  // TODO(keith): make configurations completely separately contained items
  // that aren't dependent on their containing object so can have catalogs of
  // them.
  //@ManyToOne(optional = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  //private JpaSpaceControllerConfiguration configuration;

  /**
   * The metadata.
   */
  @OneToMany(targetEntity = JpaSpaceControllerMetadataItem.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER,
      orphanRemoval = true)
  private List<JpaSpaceControllerMetadataItem> metadata = Lists.newArrayList();

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
  public String getHostId() {
    return hostId;
  }

  @Override
  public void setHostId(String hostId) {
    this.hostId = hostId;
  }

  @Override
  public String getUuid() {
    return uuid;
  }

  @Override
  public void setUuid(String uuid) {
    this.uuid = uuid;
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

  /*
  @Override
  public SpaceControllerConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public void setConfiguration(SpaceControllerConfiguration configuration) {
    this.configuration = (JpaSpaceControllerConfiguration) configuration;
  }
  */

  @Override
  public void setMetadata(Map<String, Object> m) {

    synchronized (metadata) {
      metadata.clear();

      for (Entry<String, Object> entry : m.entrySet()) {
        metadata.add(new JpaSpaceControllerMetadataItem(this, entry.getKey(), entry.getValue().toString()));
      }
    }
  }

  @Override
  public Map<String, Object> getMetadata() {
    synchronized (metadata) {
      Map<String, Object> result = Maps.newHashMap();

      for (JpaSpaceControllerMetadataItem item : metadata) {
        result.put(item.getName(), item.getValue());
      }

      return result;
    }
  }

  @Override
  public SpaceControllerMode getMode() {
    return mode;
  }

  @Override
  public void setMode(SpaceControllerMode mode) {
    this.mode = mode;
  }

  @Override
  public String toString() {
    return "JpaSpaceController [id=" + id + ", hostId=" + hostId + ", uuid=" + uuid + ", name=" + name
        + ", description=" + description + ", metadata=" + getMetadata() + ", mode=" + mode + "]";
  }
}
