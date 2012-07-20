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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * JPO implementation of a metadata item for a {@link JpaActivity}
 *
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "activity_metadata")
public class JpaActivityMetadataItem {

	@ManyToOne(optional = false, fetch=FetchType.EAGER)
	private JpaActivity activity;
	
	/**
	 * Name of the metadata item.
	 */
	@Column(nullable = false, length = 512)
	private String name;
	
	/**
	 * Value of the metadata item.
	 */
	@Column(nullable = false, length = 2048)
	private String value;

	public JpaActivityMetadataItem() { }

	JpaActivityMetadataItem(JpaActivity activity, String name, String value) {
		this.activity = activity;
		this.name = name;
		this.value = value;
	}

	/**
	 * @return the activity
	 */
	public JpaActivity getActivity() {
		return activity;
	}

	/**
	 * @param activity the activity to set
	 */
	public void setActivity(JpaActivity activity) {
		this.activity = activity;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
