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

package interactivespaces.workbench.project.java;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.resource.Version;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectDependency;
import interactivespaces.workbench.project.builder.ProjectBuildContext;
import interactivespaces.workbench.project.constituent.ProjectConstituent;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Constants;
import aQute.lib.osgi.Jar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipOutputStream;

/**
 * A {@link JavaJarCompiler} using the system Java compiler.
 *
 * @author Keith M. Hughes
 */
public class JavaxJavaJarCompiler implements JavaJarCompiler {

  /**
   * The separator between a bundle's name and version in the bundle filename.
   */
  public static final String BUNDLE_NAME_VERSION_SEPARATOR = "-";

  /**
   * Size of IO buffers in bytes.
   */
  public static final int IO_BUFFER_SIZE = 1024;

  /**
   * The java compiler to use for the project.
   */
  private final ProjectJavaCompiler projectCompiler = new JavaxProjectJavaCompiler();

  @Override
  public boolean buildJar(File jarDestinationFile, File compilationFolder,
      JavaProjectExtension extensions, ProjectBuildContext context) {
    try {
      JavaProjectType projectType = context.getProjectType();

      List<File> classpath = Lists.newArrayList();
      projectType.getRuntimeClasspath(classpath, extensions, context.getWorkbench());

      Project project = context.getProject();
      File mainSourceDirectory = new File(project.getBaseDirectory(), JavaProjectType.SOURCE_MAIN_JAVA);
      List<File> compilationFiles = projectCompiler.getCompilationFiles(mainSourceDirectory);
      if (!project.getSources().isEmpty()) {
        System.out.format("Found %d files for main source directory %s\n",
            compilationFiles.size(), mainSourceDirectory.getAbsolutePath());
      }

      for (ProjectConstituent constituent : project.getSources()) {
        File addedSource = context.getProjectTarget(project.getBaseDirectory(), constituent.getSourceDirectory());
        List<File> additionalSources = projectCompiler.getCompilationFiles(addedSource);
        compilationFiles.addAll(additionalSources);
        System.out.format("Found %d files in added source directory %s\n",
            additionalSources.size(), addedSource.getAbsolutePath());
      }

      if (compilationFiles.isEmpty()) {
        throw new SimpleInteractiveSpacesException("No Java source files for Java project");
      }

      List<String> compilerOptions = projectCompiler.getCompilerOptions(context);

      if (projectCompiler.compile(compilationFolder, classpath, compilationFiles, compilerOptions)) {
        createJarFile(project, jarDestinationFile, compilationFolder, classpath);

        if (extensions != null) {
          extensions.postProcessJar(context, jarDestinationFile);
        }

        context.addArtifact(jarDestinationFile);

        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      context.getWorkbench().logError("Error while creating project", e);

      return false;
    }
  }

  /**
   * Create the JAR file for the artifact.
   *
   * @param project
   *          the project being built
   * @param jarDestinationFile
   *          the file where the jar file is going
   * @param compilationFolder
   *          folder that the Java class files were compiled into
   * @param classpath
   *          class-path to use for jar manifest file
   */
  private void createJarFile(Project project, File jarDestinationFile, File compilationFolder,
      List<File> classpath) {
    // Create a buffer for reading the files
    byte[] buf = new byte[IO_BUFFER_SIZE];

    Manifest manifest = createManifest(project, compilationFolder, classpath);
    JarOutputStream out = null;
    try {
      // Create the ZIP file
      out = new JarOutputStream(new FileOutputStream(jarDestinationFile), manifest);

      writeJarFile(compilationFolder, buf, out, "");

      // Complete the ZIP file
      out.close();
    } catch (IOException e) {
      throw new InteractiveSpacesException(String.format("Failed creating jar file %s",
          jarDestinationFile.getAbsolutePath()), e);
    } finally {
      Closeables.close(out, true);
    }
  }

  /**
   * Create a manifest for the object.
   *
   * @param project
   *          the project being created
   * @param compilationFolder
   *          folder where the Java files were compiled to
   * @param classpath
   *          class-path to use for jar manifest file
   *
   * @return the manifest
   */
  private Manifest createManifest(Project project, File compilationFolder, List<File> classpath) {
    // Map Java package names which are supplied by project dependencies to
    // those dependencies.
    Map<String, ProjectDependency> dependencyInfo = Maps.newHashMap();
    analyzeClasspathForDependencies(classpath, project, dependencyInfo);

    Analyzer analyzer = new Analyzer();
    try {
      Jar bin = new Jar(compilationFolder);
      analyzer.setJar(bin);

      analyzer.setClasspath(classpath.toArray(new File[classpath.size()]));

      analyzer.setProperty(Constants.BUNDLE_SYMBOLICNAME, project.getIdentifyingName());

      Version version = project.getVersion();
      analyzer.setProperty(Constants.BUNDLE_VERSION, version.toString());

      // This will make sure all packages from the activity will export and will
      // all be given the bundle version.
      analyzer.setProperty(Constants.EXPORT_PACKAGE, "*;version=\"" + version + "\"");

      // This will make sure any imports we miss will be added to the imports.
      analyzer.setProperty(Constants.IMPORT_PACKAGE, "*");
      analyzer.analyze();

      correctDependencyVersions(dependencyInfo, analyzer);

      Manifest manifest = analyzer.calcManifest();

      return manifest;
    } catch (Exception e) {
      throw new InteractiveSpacesException("Could not create JAR manifest for project", e);
    } finally {
      analyzer.close();
    }
  }

  /**
   * Make sure that all project dependencies are given the version range stated
   * in the project dependencies.
   *
   * @param dependencyInfo
   *          the collection of packages to the project dependency
   * @param analyzer
   *          the analyzer for the bundle being created
   */
  private void correctDependencyVersions(Map<String, ProjectDependency> dependencyInfo,
      Analyzer analyzer) {
    Map<String, Map<String, String>> importsInfo = analyzer.getImports();
    for (Entry<String, Map<String, String>> importInfo : importsInfo.entrySet()) {
      String importedPackageName = importInfo.getKey();
      ProjectDependency projectDependency = dependencyInfo.get(importedPackageName);
      if (projectDependency != null) {
        String osgiVersionString = projectDependency.getOsgiVersionString();
        importInfo.getValue().put(Constants.VERSION_ATTRIBUTE, osgiVersionString);
      }
    }
  }

  /**
   * Analyze the classpath and get all bundle information that will affect
   * project dependencies. The {@code dependencyInfo} map will be keyed by Java
   * package names coming from project dependencies and will have a value of the
   * project dependency itself.
   *
   * @param classpath
   *          the classpath to be analyzed
   * @param project
   *          the project being built
   * @param dependencyInfo
   *          the dependency map
   */
  private void analyzeClasspathForDependencies(List<File> classpath, Project project,
      Map<String, ProjectDependency> dependencyInfo) {
    BndBundleAnalyzer bundleAnalyzer = new BndBundleAnalyzer();

    Map<String, ProjectDependency> dependencies = Maps.newHashMap();
    for (ProjectDependency dependency : project.getDependencies()) {
      dependencies.put(dependency.getName(), dependency);

    }
    for (File bundle : classpath) {
      String initialName = bundle.getName();
      int posInitialname = initialName.lastIndexOf(BUNDLE_NAME_VERSION_SEPARATOR);
      if (posInitialname != -1) {
        initialName = initialName.substring(0, posInitialname);
      }
      ProjectDependency associatedDependency = dependencies.get(initialName);
      if (associatedDependency != null) {
        Set<String> analyze = bundleAnalyzer.analyze(bundle);
        if (analyze != null) {
          for (String pckage : analyze) {
            if (dependencyInfo.put(pckage, associatedDependency) != null) {
              System.out.format("WARNING: Package %s found in multiple classpath entities\n",
                  pckage);
            }
          }
        }
      }
    }
  }

  /**
   * Write out the contents of the folder to the distribution file.
   *
   * @param directory
   *          folder being written to the build
   * @param buf
   *          a buffer for caching info
   * @param jarOutputStream
   *          the stream where the jar is being written
   * @param parentPath
   *          path up to this point
   *
   * @throws IOException for io access errors
   */
  private void writeJarFile(File directory, byte[] buf, ZipOutputStream jarOutputStream,
      String parentPath) throws IOException {
    File[] files = directory.listFiles();
    if (files == null || files.length == 0) {
      System.err.println("WARNING: No source files found in " + directory.getAbsolutePath());
      return;
    }
    for (File file : files) {
      if (file.isDirectory()) {
        writeJarFile(file, buf, jarOutputStream, parentPath + file.getName() + "/");
      } else {
        FileInputStream in = null;
        try {
          in = new FileInputStream(file);

          // Add ZIP entry to output stream.
          jarOutputStream.putNextEntry(new JarEntry(parentPath + file.getName()));

          // Transfer bytes from the file to the ZIP file
          int len;
          while ((len = in.read(buf)) > 0) {
            jarOutputStream.write(buf, 0, len);
          }

          // Complete the entry
          jarOutputStream.closeEntry();
          in.close();
        } finally {
          Closeables.close(in, true);
        }
      }
    }
  }
}
