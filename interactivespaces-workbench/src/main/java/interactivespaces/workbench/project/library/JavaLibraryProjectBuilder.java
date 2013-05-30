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

package interactivespaces.workbench.project.library;

import java.io.File;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.activity.ProjectBuildContext;
import interactivespaces.workbench.project.activity.builder.ProjectBuilder;
import interactivespaces.workbench.project.activity.builder.java.JavaJarCompiler;
import interactivespaces.workbench.project.activity.builder.java.JavaxJavaJarCompiler;

/**
 * A Java library project builder
 * 
 * @author Keith M. Hughes
 */
public class JavaLibraryProjectBuilder implements ProjectBuilder {

	/**
	 * File extension to give the build artifact
	 */
	private static final String JAR_FILE_EXTENSION = "jar";

	/**
	 * The compiler for Java JARs
	 */
	private JavaJarCompiler compiler = new JavaxJavaJarCompiler();

	@Override
	public void build(Project project, ProjectBuildContext context) {
		try {
			File buildDirectory = context.getBuildDirectory();
			File compilationFolder = getOutputDirectory(buildDirectory);
			File jarDestinationFile = getBuildDestinationFile(project,
					buildDirectory);

			compiler.build(jarDestinationFile, compilationFolder, null, context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Create the output directory for the activity compilation
	 * 
	 * @param buildDirectory
	 *            the root of the build folder
	 * 
	 * @return the output directory for building
	 */
	private File getOutputDirectory(File buildDirectory) {
		File outputDirectory = new File(buildDirectory, "classes");
		if (!outputDirectory.exists()) {
			if (!outputDirectory.mkdirs()) {
				throw new InteractiveSpacesException(String.format(
						"Cannot create Java compiler output directory %s",
						outputDirectory));
			}
		}

		return outputDirectory;
	}

	/**
	 * Get the build destination file.
	 * 
	 * <p>
	 * Any subdirectories needed will be created.
	 * 
	 * @param project
	 *            the project being built
	 * @param buildDirectory
	 *            where the artifact will be built
	 * 
	 * @return the file where the build should be written
	 */
	private File getBuildDestinationFile(Project project, File buildDirectory) {
		return new File(buildDirectory, project.getIdentifyingName() + "-"
				+ project.getVersion() + "." + JAR_FILE_EXTENSION);
	}

}
