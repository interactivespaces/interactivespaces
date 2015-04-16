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

package interactivespaces.workbench.project.java;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.ProjectTaskContext;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * A project java compiler which uses the javax compiler.
 *
 * @author Keith M. Hughes
 */
public class JavaxProjectJavaCompiler implements ProjectJavaCompiler {

  /**
   * The file extension for a Java source file.
   */
  private static final String FILE_EXTENSION_JAVA_SOURCE = ".java";

  /**
   * The Java compiler flag that specifies which version of the Java runtime the compiler should target.
   */
  private static final String JAVA_COMPILER_FLAG_TARGET_VERSION = "-target";

  /**
   * The Java compiler flag that specifies which version of Java source compatibility the compiler should insist on.
   */
  private static final String JAVA_COMPILER_FLAG_SOURCE_VERSION = "-source";

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void compile(File compilationBuildDirectory, List<File> classpath, List<File> compilationFiles,
      List<String> compilerOptions) {

    StandardJavaFileManager fileManager = null;
    try {
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      if (compiler == null) {
        SimpleInteractiveSpacesException
            .throwFormattedException("Could not find Java compiler.  Verify java is being run "
                + "from the JDK and not JRE.");
      }
      fileManager = compiler.getStandardFileManager(null, null, null);
      fileManager.setLocation(StandardLocation.CLASS_PATH, classpath);
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Lists.newArrayList(compilationBuildDirectory));

      Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(compilationFiles);

      Boolean success = compiler.getTask(null, fileManager, null, compilerOptions, null, compilationUnits1).call();

      if (!success) {
        throw new SimpleInteractiveSpacesException("The Java compilation failed");
      }
    } catch (IOException e) {
      throw new InteractiveSpacesException("Error while compiling Java files", e);
    } finally {
      fileSupport.close(fileManager, false);
    }
  }

  @Override
  public List<String> getCompilerOptions(ProjectTaskContext context) {
    List<String> options = Lists.newArrayList();

    Configuration config = context.getProject().getConfiguration();

    String javaVersion = config.getPropertyString(CONFIGURATION_BUILDER_JAVA_VERSION, JAVA_VERSION_DEFAULT).trim();
    options.add(JAVA_COMPILER_FLAG_SOURCE_VERSION);
    options.add(javaVersion);
    options.add(JAVA_COMPILER_FLAG_TARGET_VERSION);
    options.add(javaVersion);

    String extraOptions = config.getPropertyString(CONFIGURATION_BUILDER_JAVA_COMPILEFLAGS);
    if (extraOptions != null) {
      String[] optionComponents = extraOptions.trim().split("\\s+");
      for (String optionComponent : optionComponents) {
        options.add(optionComponent);
      }
    }

    return options;
  }

  @Override
  public void getCompilationFiles(File baseSourceDirectory, List<File> files) {
    if (baseSourceDirectory.isDirectory()) {
      scanDirectory(baseSourceDirectory, files);
    }
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
        if (file.isDirectory()) {
          scanDirectory(file, files);
        } else if (shouldProcessFile(file)) {
          files.add(file);
        }
      }
    }
  }

  /**
   * Should the file be processed?
   *
   * @param file
   *          the file to check
   *
   * @return {@code true} if the file should be processed
   */
  private boolean shouldProcessFile(File file) {
    return file.getName().endsWith(FILE_EXTENSION_JAVA_SOURCE) && !file.isHidden();
  }
}
