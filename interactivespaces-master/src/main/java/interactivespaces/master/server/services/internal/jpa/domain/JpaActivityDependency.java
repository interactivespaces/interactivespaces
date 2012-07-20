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

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityDependency;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * A JPA implementation of an {@link ActivityDependency}.
 * 
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "activity_dependencies")
public class JpaActivityDependency implements ActivityDependency {

	/**
	 * The activity which has the dependency.
	 */
	@ManyToOne(optional = false, fetch=FetchType.EAGER)
	private JpaActivity activity;

	/**
	 * The name of the dependency.
	 */
	@Column(nullable = false, length = 512)
	private String name;

	/**
	 * The minimum version necessary for the activity.
	 * 
	 * @return
	 */
	@Column(nullable = true, length = 32)
	private String minimumVersion;

	/**
	 * The maximum version necessary for the activity.
	 */
	@Column(nullable = true, length = 32)
	private String maximumVersion;

	/**
	 * Is the dependency required?
	 * 
	 * <p>
	 * {@code true} if the dependency is required
	 */
	@Column
	private boolean required;

	/**
	 * The database version. Used for detecting concurrent modifications.
	 */
	@Version
	private long databaseVersion;

	@Override
	public Activity getActivity() {
		return activity;
	}

	@Override
	public void setActivity(Activity activity) {
		this.activity = (JpaActivity)activity;
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
	public String getMinimumVersion() {
		return minimumVersion;
	}

	@Override
	public void setMinimumVersion(String minimumVersion) {
		this.minimumVersion = minimumVersion;
	}

	@Override
	public String getMaximumVersion() {
		return maximumVersion;
	}

	@Override
	public void setMaximumVersion(String maximumVersion) {
		this.maximumVersion = maximumVersion;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public void setRequired(boolean required) {
		this.required = required;
	}

	@Override
	public String toString() {
		return "JpaActivityDependency [name=" + name
				+ ", minimumVersion=" + minimumVersion + ", maximumVersion="
				+ maximumVersion + ", required=" + required + "]";
	}
}
