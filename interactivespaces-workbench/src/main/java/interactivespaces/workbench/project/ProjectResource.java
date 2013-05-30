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

/**
 * A resource for a {@link Project}.
 *
 * @author Keith M. Hughes
 */
public class ProjectResource {
	
	/**
	 * A directory from which all contents will be copied.
	 */
	private String sourceDirectory;
	
	/**
	 * A file to be copied.
	 */
	private String sourceFile;
	
	/**
	 * The directory to which contents will be copied.
	 * 
	 * <p>
	 * This directory will be relative to the project's installed folder.
	 */
	private String destinationDirectory;
	
	/**
	 * The file to which a file will be copied.
	 * 
	 * <p>
	 * This file will be relative to the project's installed folder.
	 */
	private String destinationFile;

	/**
	 * @return the sourceDirectory
	 */
	public String getSourceDirectory() {
		return sourceDirectory;
	}

	/**
	 * @param sourceDirectory the sourceDirectory to set
	 */
	public void setSourceDirectory(String sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	/**
	 * @return the sourceFile
	 */
	public String getSourceFile() {
		return sourceFile;
	}

	/**
	 * @param sourceFile the sourceFile to set
	 */
	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	/**
	 * @return the destinationDirectory
	 */
	public String getDestinationDirectory() {
		return destinationDirectory;
	}

	/**
	 * @param destinationDirectory the destinationDirectory to set
	 */
	public void setDestinationDirectory(String destinationDirectory) {
		this.destinationDirectory = destinationDirectory;
	}

	/**
	 * @return the destinationFile
	 */
	public String getDestinationFile() {
		return destinationFile;
	}

	/**
	 * @param destinationFile the destinationFile to set
	 */
	public void setDestinationFile(String destinationFile) {
		this.destinationFile = destinationFile;
	}
}
