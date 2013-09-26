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

package interactivespaces.workbench.project.activity.builder.java;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectDependency;
import interactivespaces.workbench.project.activity.ProjectBuildContext;

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

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

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
   * The default version of Java that items are compiled for.
   */
  public static final String JAVA_VERSION_DEFAULT = "1.6";

  /**
   * configuration property for adding options to the JavaC compiler.
   */
  public static final String CONFIGURATION_BUILDER_JAVA_COMPILEFLAGS =
      "interactivespaces.workbench.builder.java.compileflags";

  /**
   * The version of Java being compiled for.
   */
  private String javaVersion = JAVA_VERSION_DEFAULT;

  @Override
  public boolean build(File jarDestinationFile, File compilationFolder,
      JavaProjectExtensions extensions, ProjectBuildContext context) throws Exception {
    List<File> classpath = Lists.newArrayList(context.getWorkbench().getControllerClasspath());
    try {
      if (compile(context.getProject(), compilationFolder, context, extensions, classpath)) {
        createJarFile(context.getProject(), jarDestinationFile, compilationFolder, classpath);

        if (extensions != null) {
          extensions.postProcessJar(context, jarDestinationFile);
        }

        context.addArtifact(jarDestinationFile);

        return true;
      } else {
        return false;
      }
    } catch (SimpleInteractiveSpacesException e) {
      System.err.println(e.getMessage());
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Compile the project.
   * 
   * @param project
   *          the project being compiled
   * @param compilationBuildDirectory
   *          the build folder for compilation artifacts
   * @param context
   *          the project build context
   * 
   * @return {@code true} if the compilation was successful
   * 
   * @throws IOException
   */
  private boolean compile(Project project, File compilationBuildDirectory,
      ProjectBuildContext context, JavaProjectExtensions extensions, List<File> classpath)
          throws IOException {
    InteractiveSpacesWorkbench workbench = context.getWorkbench();
    if (extensions != null) {
      extensions.addToClasspath(classpath, workbench);
    }

    List<File> compilationFiles = getCompilationFiles(project);
    if (compilationFiles.isEmpty()) {
      throw new SimpleInteractiveSpacesException("No Java source files for Java project");
    }

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    fileManager.setLocation(StandardLocation.CLASS_PATH, classpath);
    fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
        Lists.newArrayList(compilationBuildDirectory));

    Iterable<? extends JavaFileObject> compilationUnits1 =
        fileManager.getJavaFileObjectsFromFiles(compilationFiles);

    List<String> compilerOptions = getCompilerOptions(context);

    Boolean success =
        compiler.getTask(null, fileManager, null, compilerOptions, null, compilationUnits1).call();

    fileManager.close();

    return success;
  }

  /**
   * Get all compiler options to be used.
   * 
   * @param context
   *          the build context
   * 
   * @return the complete compiler options
   */
  private List<String> getCompilerOptions(ProjectBuildContext context) {
    List<String> options = Lists.newArrayList();
    options.add("-source");
    options.add(javaVersion);
    options.add("-target");
    options.add(javaVersion);

    String extraOptions =
        context.getWorkbench().getWorkbenchConfig()
        .getPropertyString(CONFIGURATION_BUILDER_JAVA_COMPILEFLAGS);
    if (extraOptions != null) {
      String[] optionComponents = extraOptions.split("\\s+");
      for (String optionComponent : optionComponents) {
        options.add(optionComponent);
      }
    }
    return options;
  }

  /**
   * get a list of files to compile.
   * 
   * @param project
   *          the project being built
   * 
   * @return a list of all Java files to be handed to the Java compiler
   */
  private List<File> getCompilationFiles(Project project) {
    List<File> files = Lists.newArrayList();

    File baseSourceDirectory = new File(project.getBaseDirectory(), JAVA_SOURCE_SUBDIRECTORY);
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
   *          the directory to scan
   * @param files
   *          collection to add found files in
   */
  private void scanDirectory(File directory, List<File> files) {
    File[] directoryListing = directory.listFiles();
    if (directoryListing != null) {
      for (File file : directoryListing) {
        // Check for hidden directories, we don't want those.
        if (!file.getName().startsWith(".")) {
          if (file.isDirectory()) {
            scanDirectory(file, files);
          } else {
            files.add(file);
          }
        }
      }
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
   */
  private void createJarFile(Project project, File jarDestinationFile, File compilationFolder,
      List<File> classpath) {
    // Create a buffer for reading the files
    byte[] buf = new byte[1024];

    Manifest manifest = createManifest(project, compilationFolder, classpath);
    JarOutputStream out = null;
    try {
      // Create the ZIP file
      out = new JarOutputStream(new FileOutputStream(jarDestinationFile), manifest);

      writeJarFile(compilationFolder, buf, out, "");

      // Complete the ZIP file
      out.flush();
    } catch (IOException e) {
      throw new InteractiveSpacesException(String.format("Failed creating jar file %s",
          jarDestinationFile.getAbsolutePath()), e);
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
   *          the project being created
   * @param compilationFolder
   *          folder where the Java files were compiled to
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

      String version = project.getVersion();
      int pos = version.indexOf(BUNDLE_NAME_VERSION_SEPARATOR);
      if (pos != -1) {
        version = version.substring(0, pos);
      }
      analyzer.setProperty(Constants.BUNDLE_VERSION, version);

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
   * @param activityFolder
   *          folder being written to the build
   * @param buf
   *          a buffer for caching info
   * @param jarOutputStream
   *          the stream where the jar is being written
   * @param parentPath
   *          path up to this point
   * @throws IOException
   */
  private void writeJarFile(File directory, byte[] buf, ZipOutputStream jarOutputStream,
      String parentPath) throws IOException {
    for (File file : directory.listFiles()) {
      if (file.isDirectory()) {
        writeJarFile(file, buf, jarOutputStream, parentPath + file.getName() + "/");
      } else {
        FileInputStream in = new FileInputStream(file);

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
      }
    }
  }
}
