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

import interactivespaces.domain.system.NamedScript;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * A JPA implementation of an {@link NamedScript}.
 * 
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "named_scripts")
@NamedQueries({
	@NamedQuery(name="namedScriptAll", query="select s from JpaNamedScript s"),
})
public class JpaNamedScript implements NamedScript {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4597355313687577793L;

	/**
	 * The persistence ID for the script.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(nullable = false, length = 64)
	private String id;

	/**
	 * The name of the script.
	 */
	@Column( nullable = false, length = 512 )
	private String name;

	/**
	 * The description of the script.
	 */
	@Column(nullable = true, length = 2048)
	private String description;

	/**
	 * The language of the script.
	 */
	@Column(nullable = false, length = 128)
	private String language;

	/**
	 * The content of the script.
	 */
	@Lob
	@Column(nullable = true)
	private String content;

	/**
	 * The schedule of the script.
	 */
	@Column(nullable = true, length = 256)
	private String schedule;
	
	/**
	 * {@code true} if the script is scheduled.
	 */
	@Column
	private boolean scheduled;

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
	public String getContent() {
		return content;
	}

	@Override
	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String getSchedule() {
		return schedule;
	}

	@Override
	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	@Override
	public boolean getScheduled() {
		return scheduled;
	}

	@Override
	public void setScheduled(boolean scheduled) {
		this.scheduled = scheduled;
	}

	@Override
	public String toString() {
		return "JpaNamedScript [id=" + id + ", name=" + name + ", description="
				+ description + ", language=" + language + ", scheduled="
						+ scheduled + ", schedule="
				+ schedule + ", content=\n" + content + "\n]";
	}
}
