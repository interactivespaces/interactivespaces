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

package interactivespaces.workbench.activity.project.builder.java;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.basic.pojo.SimpleActivity;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.activity.project.ActivityProject;
import interactivespaces.workbench.activity.project.ActivityProjectBuildContext;
import interactivespaces.workbench.activity.project.builder.ActivityBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipOutputStream;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Jar;

import com.google.common.collect.Lists;

/**
 * An {@link ActivityBuilder} for java projects.
 * 
 * @author Keith M. Hughes
 */
public class JavaActivityBuilder implements ActivityBuilder {

	private static final String JAVA_SOURCE_SUBDIRECTORY = "src/main/java";

	/**
	 * File extension to give the build artifact
	 */
	private static final String JAR_FILE_EXTENSION = "jar";

	/**
	 * The extensions for this builder.
	 */
	private JavaActivityExtensions extensions;

	/**
	 * Construct a builder with no extensions.
	 */
	public JavaActivityBuilder() {
		this(null);
	}

	/**
	 * Construct a builder with the given extensions.
	 * 
	 * @param extensions
	 *            the extensions to use, can be {@code null}
	 */
	public JavaActivityBuilder(JavaActivityExtensions extensions) {
		this.extensions = extensions;
	}

	@Override
	public void build(ActivityProject project,
			ActivityProjectBuildContext context) {
		try {
			File buildDirectory = context.getBuildDirectory();
			File compilationFolder = getOutputDirectory(buildDirectory);

			compile(project, compilationFolder, context);
			File buildArtifact = createJarFile(project, buildDirectory,
					compilationFolder);

			if (extensions != null) {
				extensions.postProcessJar(context, buildArtifact);
			}

			context.addArtifact(buildArtifact);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Compile the project.
	 * 
	 * @param project
	 *            the project being compiled
	 * @param compilationBuildDirectory
	 *            the build folder for compilation artifacts
	 * @param context
	 *            the project build context
	 * 
	 * @throws IOException
	 */
	private void compile(ActivityProject project,
			File compilationBuildDirectory, ActivityProjectBuildContext context)
			throws IOException {
		InteractiveSpacesWorkbench workbench = context.getWorkbench();
		List<File> classpath = Lists.newArrayList(workbench
				.getControllerClasspath());
		if (extensions != null) {
			extensions.addToClasspath(classpath, workbench);
		}

		List<File> compilationFiles = getCompilationFiles(project);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(
				null, null, null);
		fileManager.setLocation(StandardLocation.CLASS_PATH, classpath);
		fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
				Lists.newArrayList(compilationBuildDirectory));

		Iterable<? extends JavaFileObject> compilationUnits1 = fileManager
				.getJavaFileObjectsFromFiles(compilationFiles);
		List<String> options = Lists.newArrayList();
		options.add("-source");
		options.add("1.6");
		options.add("-target");
		options.add("1.6");
		compiler.getTask(null, fileManager, null, options, null,
				compilationUnits1).call();

		fileManager.close();
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

	private List<File> getCompilationFiles(ActivityProject project) {
		List<File> files = Lists.newArrayList();

		File baseSourceDirectory = new File(project.getBaseDirectory(),
				JAVA_SOURCE_SUBDIRECTORY);
		scanDirectory(baseSourceDirectory, files);

		return files;
	}

	/**
	 * Scan the given directory for files to add.
	 * 
	 * <p>
	 * This method will recurse into subdirectories.
	 * 
	 * @param directory
	 *            the directory to scan
	 * @param files
	 *            collection to add found files in
	 */
	private void scanDirectory(File directory, List<File> files) {
		for (File file : directory.listFiles()) {
			if (!file.getName().startsWith(".")) {
				if (file.isDirectory()) {
					scanDirectory(file, files);
				} else {
					files.add(file);
				}
			}
		}
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
	private File getBuildDestinationFile(ActivityProject project,
			File buildDirectory) {
		SimpleActivity activity = project.getActivityDescription();
		return new File(buildDirectory, activity.getIdentifyingName() + "-"
				+ activity.getVersion() + "." + JAR_FILE_EXTENSION);
	}

	/**
	 * Create the JAR file for the artifact.
	 * 
	 * @param project
	 *            the project being built
	 * @param buildDirectory
	 *            the folder where the artifact is going
	 * @param compilationFolder
	 *            folder that the Java class files were compiled into
	 * 
	 * @return the created jar file
	 */
	private File createJarFile(ActivityProject project, File buildDirectory,
			File compilationFolder) {
		// Create a buffer for reading the files
		byte[] buf = new byte[1024];

		Manifest manifest = createManifest(project, compilationFolder);
		JarOutputStream out = null;
		try {
			// Create the ZIP file
			File buildDestinationFile = getBuildDestinationFile(project,
					buildDirectory);
			out = new JarOutputStream(
					new FileOutputStream(buildDestinationFile), manifest);

			writeJarFile(compilationFolder, buf, out, "");

			// Complete the ZIP file
			out.flush();

			return buildDestinationFile;
		} catch (IOException e) {
			throw new InteractiveSpacesException(
					"Failed writing Activity Build file", e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// Don't care
				}
			}
		}
	}

	/**
	 * Create a manifest for the object
	 * 
	 * @param project
	 *            the project being created
	 * @param compilationFolder
	 *            folder where the Java files were compiled to
	 * 
	 * @return the manifest
	 */
	private Manifest createManifest(ActivityProject project,
			File compilationFolder) {
		try {
			Analyzer analyzer = new Analyzer();
			Jar bin = new Jar(compilationFolder);
			analyzer.setJar(bin);

			// analyzer.addClasspath( new
			// File("jar/spring.jar") );

			analyzer.setProperty("Bundle-SymbolicName", project
					.getActivityDescription().getIdentifyingName());
			analyzer.setProperty("Export-Package", "*");
			analyzer.setProperty("Bundle-Version", project
					.getActivityDescription().getVersion());

			// There are no good defaults, but this must be set
			analyzer.setProperty("Import-Package", "*");

			return analyzer.calcManifest();
		} catch (Exception e) {
			throw new InteractiveSpacesException(
					"Could not create JAR manifest for project", e);
		}
	}

	/**
	 * Write out the contents of the folder to the distribution file.
	 * 
	 * @param activityFolder
	 *            folder being written to the build
	 * @param buf
	 *            a buffer for caching info
	 * @param jarOutputStream
	 *            the stream where the jar is being written
	 * @param parentPath
	 *            path up to this point
	 * @throws IOException
	 */
	private void writeJarFile(File directory, byte[] buf,
			ZipOutputStream jarOutputStream, String parentPath)
			throws IOException {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				writeJarFile(file, buf, jarOutputStream,
						parentPath + file.getName() + "/");
			} else {
				FileInputStream in = new FileInputStream(file);

				// Add ZIP entry to output stream.
				jarOutputStream.putNextEntry(new JarEntry(parentPath
						+ file.getName()));

				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					jarOutputStream.write(buf, 0, len);
				}

				// Complete the entry
				jarOutputStream.closeEntry();
				in.close();
			}
		}
	}
}
