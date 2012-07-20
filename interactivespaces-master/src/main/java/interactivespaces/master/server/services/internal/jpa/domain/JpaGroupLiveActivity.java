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

import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * A JPA implementation of a {@link GroupLiveActivity}.
 * 
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "group_live_activities")
public class JpaGroupLiveActivity implements GroupLiveActivity {
	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -420426607253663965L;

	/**
	 * The activity group this activity is part of.
	 */
	@ManyToOne(optional = false, fetch=FetchType.EAGER)
	private JpaLiveActivityGroup activityGroup;

	/**
	 * The activity this represents.
	 */
	@ManyToOne(optional = true, fetch=FetchType.EAGER)
	private JpaLiveActivity activity;

	/**
	 * How the activity group depends on the activity.
	 */
	@Enumerated(EnumType.STRING)
	private GroupLiveActivityDependency dependency;

	/**
	 * The database version. Used for detecting concurrent modifications.
	 */
	@Version
	private long databaseVersion;
	
	public JpaGroupLiveActivity() { }

	JpaGroupLiveActivity(JpaLiveActivityGroup activityGroup,
			JpaLiveActivity activity, GroupLiveActivityDependency dependency) {
		this.activityGroup = activityGroup;
		this.activity = activity;
		this.dependency = dependency;
	}

	@Override
	public LiveActivityGroup getActivityGroup() {
		return activityGroup;
	}

	@Override
	public void setActivityGroup(LiveActivityGroup activityGroup) {
		this.activityGroup = (JpaLiveActivityGroup)activityGroup;
	}

	@Override
	public LiveActivity getActivity() {
		return activity;
	}

	@Override
	public void setActivity(LiveActivity activity) {
		this.activity = (JpaLiveActivity)activity;
	}

	@Override
	public GroupLiveActivityDependency getDependency() {
		return dependency;
	}

	@Override
	public void setDependency(GroupLiveActivityDependency dependency) {
		this.dependency = dependency;
	}

	@Override
	public String toString() {
		return "JpaGroupLiveActivity [activity=" + activity + ", dependency="
				+ dependency + "]";
	}
}
