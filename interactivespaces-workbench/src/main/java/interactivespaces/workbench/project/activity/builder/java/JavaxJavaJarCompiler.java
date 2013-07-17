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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.activity.ProjectBuildContext;

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Jar;

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
import javax.tools.JavaCompiler.CompilationTask;
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

  private static final String JAVA_SOURCE_SUBDIRECTORY = "src/main/java";

  @Override
  public boolean build(File jarDestinationFile, File compilationFolder,
      JavaProjectExtensions extensions, ProjectBuildContext context) throws Exception {
    try {
      if (compile(context.getProject(), compilationFolder, context, extensions)) {
        createJarFile(context.getProject(), jarDestinationFile, compilationFolder);

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
      ProjectBuildContext context, JavaProjectExtensions extensions) throws IOException {
    InteractiveSpacesWorkbench workbench = context.getWorkbench();
    List<File> classpath = Lists.newArrayList(workbench.getControllerClasspath());
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
    List<String> options = Lists.newArrayList();
    options.add("-source");
    options.add("1.6");
    options.add("-target");
    options.add("1.6");
    Boolean success =
        compiler.getTask(null, fileManager, null, options, null, compilationUnits1).call();

    fileManager.close();

    return success;
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
  private void createJarFile(Project project, File jarDestinationFile, File compilationFolder) {
    // Create a buffer for reading the files
    byte[] buf = new byte[1024];

    Manifest manifest = createManifest(project, compilationFolder);
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
  private Manifest createManifest(Project project, File compilationFolder) {
    try {
      Analyzer analyzer = new Analyzer();
      Jar bin = new Jar(compilationFolder);
      analyzer.setJar(bin);

      // analyzer.addClasspath( new
      // File("jar/spring.jar") );

      analyzer.setProperty("Bundle-SymbolicName", project.getIdentifyingName());

      String version = project.getVersion();
      int pos = version.indexOf("-");
      if (pos != -1) {
        version = version.substring(0, pos);
      }
      analyzer.setProperty("Bundle-Version", version);

      analyzer.setProperty("Export-Package", "*");

      // There are no good defaults, but this must be set
      analyzer.setProperty("Import-Package", "*");

      return analyzer.calcManifest();
    } catch (Exception e) {
      throw new InteractiveSpacesException("Could not create JAR manifest for project", e);
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
