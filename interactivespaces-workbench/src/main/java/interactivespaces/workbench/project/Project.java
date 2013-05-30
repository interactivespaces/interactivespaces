/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.workbench.project;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * An Interactive Spaces project.
 *
 * @author Keith M. Hughes
 */
public class Project {
	
	/**
	 * The base directory for the file.
	 */
	private File baseDirectory;

	/**
	 * The type of builder for the project.
	 */
	private String builderType;
	
	/**
	 * The type of the project.
	 */
	private String type;

	/**
	 * The identifying name of the project.
	 */
	private String identifyingName;

	/**
	 * The descriptive name of the project.
	 */
	private String name;

	/**
	 * The description of the project.
	 */
	private String description;

	/**
	 * Version of the project.
	 */
	private String version;

	/**
	 * The dependencies the project has.
	 */
	private List<ProjectDependency> dependencies = Lists.newArrayList();

	/**
	 * The resources the project requires.
	 */
	private List<ProjectResource> resources = Lists.newArrayList();
	
	/**
	 * The meta data for this project.
	 */
	private Map<String, Object> metadata = Maps.newHashMap();

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Set the base directory for the project.
	 * 
	 * @param baseDirectory
	 */
	public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	/**
	 * @return the baseDirectory
	 */
	public File getBaseDirectory() {
		return baseDirectory;
	}

	/**
	 * @return the builderType
	 */
	public String getBuilderType() {
		return builderType;
	}

	/**
	 * @param builderType the builderType to set
	 */
	public void setBuilderType(String builderType) {
		this.builderType = builderType;
	}

	/**
	 * Get the identifying name for the project.
	 * 
	 * @return The identifying name
	 */
	public String getIdentifyingName() {
		return identifyingName;
	}

	/**
	 * Set the identifying name for the project.
	 * 
	 * @param name
	 *            The identifying name
	 */
	public void setIdentifyingName(String identifyingName) {
		this.identifyingName = identifyingName;
	}

	/**
	 * Get the descriptive name for the project.
	 * 
	 * @return The descriptive name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the descriptive name for the project.
	 * 
	 * @param name
	 *            The descriptive name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the description of the project.
	 * 
	 * @return the description. Can be {@code null}.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of the project.
	 * 
	 * @param description
	 *            the description. Can be {@code null}.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the version for the project.
	 * 
	 * @return The version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Set the version for the project.
	 * 
	 * @param version
	 *            the version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Add a dependency to the project.
	 * 
	 * @param dependency
	 *            the dependency to add
	 */
	public void addDependency(ProjectDependency dependency) {
		dependencies.add(dependency);
	}

	/**
	 * Get a list of all dependencies the project has.
	 * 
	 * @return
	 */
	public List<ProjectDependency> getDependencies() {
		return Lists.newArrayList(dependencies);
	}

	/**
	 * Add a resource to the project.
	 * 
	 * @param resource
	 *            the resource to add
	 */
	public void addResource(ProjectResource resource) {
		resources.add(resource);
	}

	/**
	 * Get a list of all resources the project has.
	 * 
	 * @return
	 */
	public List<ProjectResource> getResources() {
		return Lists.newArrayList(resources);
	}

	/**
	 * Set the metadata for the project.
	 * 
	 * <p>
	 * This removes the old metadata completely.
	 * 
	 * @param metadata
	 * 		the metadata for the project (can be {@link null}
	 */
	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	/**
	 * Get the metadata for the project.
	 * 
	 * @return the project's meta data
	 */
	public Map<String, Object> getMetadata() {
		return metadata;
	}
}
