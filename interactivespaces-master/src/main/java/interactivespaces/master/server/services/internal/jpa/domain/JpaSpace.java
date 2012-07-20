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

import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.space.Space;

import java.util.ArrayList;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A JPA implementation of a {@link Space}.
 * 
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "spaces")
	@NamedQueries({ @NamedQuery(name = "spaceAll", query = "select s from JpaSpace s"),
	@NamedQuery(name = "spaceByLiveActivityGroup", query = "select distinct s from JpaSpace s, IN(s.activityGroups) g where g.id = :live_activity_group_id"),
	@NamedQuery(name = "countSpaceByLiveActivityGroup", query = "select count(distinct s) from JpaSpace s, IN(s.activityGroups) g where g.id = :live_activity_group_id"),
	@NamedQuery(name = "spaceBySubspace", query = "select distinct s from JpaSpace s, IN(s.spaces) ss where ss.id = :subspace_id"),
	@NamedQuery(name = "countSpaceBySubspace", query = "select count(distinct s) from JpaSpace s, IN(s.spaces) ss where ss.id = :subspace_id"),
})
public class JpaSpace implements Space {

	/**
	 * The persistence ID for the activity.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false, length = 64)
	private String id;

	/**
	 * Name of this space.
	 */
	@Column(nullable = false, length = 512)
	private String name;

	/**
	 * Description of the space.
	 */
	@Column(nullable = true, length = 2048)
	private String description;

	/**
	 * The metadata.
	 */
	@OneToMany(targetEntity = JpaSpaceMetadataItem.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<JpaSpaceMetadataItem> metadata = Lists.newArrayList();

	/**
	 * The child spaces of this space.
	 */
	@OneToMany(targetEntity = JpaSpace.class, fetch = FetchType.EAGER)
	private List<JpaSpace> spaces = Lists.newArrayList();

	/**
	 * The activity groups in for this space.
	 */
	@OneToMany(targetEntity = JpaLiveActivityGroup.class, fetch = FetchType.EAGER)
	private List<JpaLiveActivityGroup> activityGroups = Lists.newArrayList();

	/**
	 * The database version. Used for detecting concurrent modifications.
	 */
	@Version
	private long databaseVersion;

	/**
	 * Concurrency lock for the spaces collection.
	 */
	private Object spacesLock = new Object();

	/**
	 * Concurrency lock for the groups collection.
	 */
	private Object groupsLock = new Object();

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
	public void setMetadata(Map<String, Object> m) {

		synchronized (metadata) {
			metadata.clear();

			for (Entry<String, Object> entry : m.entrySet()) {
				metadata.add(new JpaSpaceMetadataItem(this, entry.getKey(),
						entry.getValue().toString()));
			}
		}
	}

	@Override
	public Map<String, Object> getMetadata() {
		synchronized (metadata) {
			Map<String, Object> result = Maps.newHashMap();

			for (JpaSpaceMetadataItem item : metadata) {
				result.put(item.getName(), item.getValue());
			}

			return result;
		}
	}

	@Override
	public List<? extends Space> getSpaces() {
		synchronized (spacesLock) {
			ArrayList<JpaSpace> result = Lists.newArrayList();
			if (spaces != null) {
				result.addAll(spaces);
			}
			return result;
		}
	}

	@Override
	public Space addSpace(Space space) {
		synchronized (spacesLock) {
			spaces.add((JpaSpace) space);
		}

		return this;
	}

	@Override
	public Space addSpaces(Space... spaces) {
		synchronized (spacesLock) {
			for (Space space : spaces) {
				this.spaces.add((JpaSpace) space);
			}
		}

		return this;
	}

	@Override
	public void removeSpace(Space space) {
		synchronized (spacesLock) {
			spaces.remove(space);
		}
	}

	@Override
	public void clearSpaces() {
		synchronized (spacesLock) {
			spaces.clear();
		}
	}

	@Override
	public List<? extends LiveActivityGroup> getActivityGroups() {
		synchronized (groupsLock) {
			ArrayList<JpaLiveActivityGroup> result = Lists.newArrayList();
			if (activityGroups != null) {
				result.addAll(activityGroups);
			}
			return result;
		}
	}

	@Override
	public Space addActivityGroup(LiveActivityGroup activityGroup) {
		synchronized (groupsLock) {
			activityGroups.add((JpaLiveActivityGroup) activityGroup);
		}

		return this;
	}

	@Override
	public Space addActivityGroups(LiveActivityGroup... activityGroups) {
		synchronized (groupsLock) {
			for (LiveActivityGroup activityGroup : activityGroups) {
				this.activityGroups.add((JpaLiveActivityGroup) activityGroup);
			}
		}

		return this;
	}

	@Override
	public void removeActivityGroup(LiveActivityGroup activityGroup) {
		synchronized (groupsLock) {
			if (activityGroups != null) {
				activityGroups.remove(activityGroup);
			}
		}
	}

	@Override
	public void clearActivityGroups() {
		synchronized (groupsLock) {
			if (activityGroups != null) {
				activityGroups.clear();
			}
		}
	}

	@Override
	public String toString() {
		return "JpaSpace [id=" + id + ", name=" + name + ", description="
				+ description + ", metadata=" + getMetadata() + ", subspaces="
				+ spaces + ", activityGroups=" + activityGroups + "]";
	}
}
