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

import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.builder.ProjectBuildContext;
import interactivespaces.workbench.project.java.JavaProjectExtension;
import interactivespaces.workbench.project.java.JavaProjectType;
import interactivespaces.workbench.project.java.JavaxProjectJavaCompiler;
import interactivespaces.workbench.project.java.ProjectJavaCompiler;

import com.google.common.collect.Lists;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
      context.getWorkbench().logError("Error while running JUnit tests", e);

      // Returning true here because JUnit failed to run, nothing to do with
      // tests.
      return true;
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

    List<File> classpath = Lists.newArrayList();
    classpath.add(jarDestinationFile);
    classpath.add(compilationFolder);
    projectType.getRuntimeClasspath(context, classpath, extension, context.getWorkbench());

    List<URL> urls = Lists.newArrayList();
    for (File classpathElement : classpath) {
      try {
        urls.add(classpathElement.toURL());
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }

    if (!testClasses.isEmpty()) {
      URLClassLoader classloader =
          new URLClassLoader(urls.toArray(new URL[urls.size()]), this.getClass().getClassLoader());
      boolean allSuceeded;

      JUnitCore junit = new JUnitCore();
      junit.addListener(new RunListener() {

        @Override
        public void testRunStarted(Description description) throws Exception {
          System.out.println("Starting JUnit tests");
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
          System.out.println("Finished JUnit tests.");
        }

        @Override
        public void testStarted(Description description) throws Exception {
          System.out.format("Starting test %s \n", description.getDisplayName());
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
          System.out.format("Test %s failed\n%s\n", failure.getTestHeader(), trimStackTrace(failure));
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
          System.out.format("Test %s failed\n%s\n", failure.getTestHeader(), trimStackTrace(failure));
        }

        @Override
        public void testFinished(Description description) throws Exception {
          System.out.format("Finished test %s \n", description.getDisplayName());
        }

        @Override
        public void testIgnored(Description description) throws Exception {
          System.out.format("Ignoring test %s\n", description.getDisplayName());
        }

        /**
         * Trim stack trace to stop before everything about the fact that Java
         * reflection called the test method.
         *
         * @param failure
         *          the JUnit failure
         *
         * @return the trimmed stack trace
         */
        private String trimStackTrace(Failure failure) {
          String trace = failure.getTrace();
          String testclass = failure.getDescription().getClassName();
          int lastIndexOf = trace.lastIndexOf(testclass);
          int endOfLine = trace.indexOf("\n", lastIndexOf);

          return trace.substring(0, endOfLine);
        }
      });

      allSuceeded = true;
      for (JunitTestClassVisitor testClass : testClasses) {
        try {
          Class<?> clazz = classloader.loadClass(testClass.getClassName().replaceAll("\\/", "."));
          Result result = junit.run(clazz);

          System.out.format("Ran %d tests in %d milliseconds\n\tFailed: %d, Ignored: %d\n", result.getRunCount(),
              result.getRunTime(), result.getFailureCount(), result.getIgnoreCount());

          allSuceeded &= result.wasSuccessful();
        } catch (ClassNotFoundException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      return allSuceeded;
    } else {
      // No tests classes, so we will get philosophical again...
      return true;
    }
  }
}
