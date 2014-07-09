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

package interactivespaces.workbench.project.test;

import com.google.common.collect.Lists;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.builder.ProjectBuildContext;
import interactivespaces.workbench.project.java.JavaProjectExtension;
import interactivespaces.workbench.project.java.JavaProjectType;
import interactivespaces.workbench.project.java.JavaxProjectJavaCompiler;
import interactivespaces.workbench.project.java.ProjectJavaCompiler;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

/**
 * A test runner which finds a bunch of classes which are JUnit test classes and
 * runs them.
 *
 * @author Keith M. Hughes
 */
public class JunitTestRunner {

  /**
   * The project compiler.
   */
  private final ProjectJavaCompiler projectCompiler = new JavaxProjectJavaCompiler();

  /**
   * File support for class.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Run tests if there are any.
   *
   * @param jarDestinationFile
   *          the JAR file created for the project
   * @param extensions
   *          any Java extension added, can be {@code null}
   * @param context
   *          the build context for the project
   *
   * @return {@code true} if all tests were successful
   */
  public boolean runTests(File jarDestinationFile, JavaProjectExtension extensions, ProjectBuildContext context) {
    List<File> compilationFiles =
        projectCompiler.getCompilationFiles(new File(context.getProject().getBaseDirectory(),
            JavaProjectType.SOURCE_MAIN_TESTS));
    if (compilationFiles.isEmpty()) {
      // No tests mean they all succeeded in some weird philosophical sense.
      return true;
    }

    try {
      JavaProjectType projectType = context.getProjectType();
      List<File> classpath = Lists.newArrayList();
      classpath.add(jarDestinationFile);
      projectType.getProjectClasspath(context, classpath, extensions, context.getWorkbench());

      File compilationFolder = new File(context.getBuildDirectory(), ProjectJavaCompiler.BUILD_DIRECTORY_CLASSES_TESTS);
      fileSupport.directoryExists(compilationFolder);

      List<String> compilerOptions = projectCompiler.getCompilerOptions(context);

      if (projectCompiler.compile(compilationFolder, classpath, compilationFiles, compilerOptions)) {
        return runJUnit(compilationFolder, jarDestinationFile, projectType, extensions, context);
      } else {
        return false;
      }
    } catch (Exception e) {
      context.getWorkbench().handleError("Error while running JUnit tests", e);
      return false;
    }
  }

  /**
   * Detect and run any JUnit test classes.
   *
   * @param compilationFolder
   *          folder where the test classes were compiled
   * @param jarDestinationFile
   *          the jar that was built
   * @param projectType
   *          the project type
   * @param extension
   *          the extension, if any, for the project
   * @param context
   *          the build context
   *
   * @return {@code true} if all tests passed
   */
  private boolean runJUnit(File compilationFolder, File jarDestinationFile, JavaProjectType projectType,
      JavaProjectExtension extension, ProjectBuildContext context) {
    // Get all JUnit tests
    JunitTestClassDetector detector = new JunitTestClassDetector();
    List<JunitTestClassVisitor> testClasses = detector.findTestClasses(compilationFolder);
    List<String> testClassNames = new LinkedList<String>();

    for (JunitTestClassVisitor testClass : testClasses) {
      testClassNames.add(testClass.getClassName().replaceAll("\\/", "."));
    }

    List<File> classpath = Lists.newArrayList();
    classpath.add(compilationFolder);
    classpath.add(jarDestinationFile);
    projectType.getRuntimeClasspath(context, classpath, extension, context.getWorkbench());

    List<URL> urls = Lists.newArrayList();
    for (File classpathElement : classpath) {
      try {
        urls.add(classpathElement.toURL());
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }

    if (!testClassNames.isEmpty()) {
      URLClassLoader classLoader =
          new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());

      return runTests(testClassNames, classLoader);
    } else {
      // No tests classes, so we will get philosophical again...
      return true;
    }
  }

  /**
   * Run the given tests in the given class loader. This method is somewhat complicated, since it needs to
   * use reflection to isolate the test runner in a separate class loader that does not derive from the
   * current class.
   *
   * @param testClassNames
   *          names of classes to test
   * @param classLoader
   *          classLoader to use for running tests
   *
   * @return {@code true} if all tests passed
   */
  private boolean runTests(List<String> testClassNames, URLClassLoader classLoader) {
    try {
      Class<?> testRunner = classLoader.loadClass("interactivespaces.util.TestRunnerBridge");
      Method runner = testRunner.getMethod("runTests", List.class, URLClassLoader.class);
      Object result = runner.invoke(null, testClassNames, classLoader);
      return (Boolean) result;
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("Error running tests", e);
    }
  }
}
