/*
 * Copyright (C) 2014 Google Inc.
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

import org.apache.commons.logging.Log;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Run all Java unit tests for a project.
 *
 * <p>
 * This class is meant to be run inside its own classloader.
 *
 * @author Keith M. Hughes
 */
public class IsolatedJavaTestRunner {

  /**
   * Run the given tests in the given classLoader.
   *
   * @param testCompilationFolder
   *          the folder for
   * @param classLoader
   *          class loader to use for running tests
   * @param log
   *          logger for the test run
   *
   * @return {@code true} if all tests passed
   */
  public boolean runTests(File testCompilationFolder, URLClassLoader classLoader, Log log) {
    // Get all JUnit tests
    JunitTestClassDetector detector = new JunitTestClassDetector();
    List<JunitTestClassVisitor> testClasses = detector.findTestClasses(testCompilationFolder);
    List<String> testClassNames = new ArrayList<String>();

    if (!testClasses.isEmpty()) {
      for (JunitTestClassVisitor testClass : testClasses) {
        testClassNames.add(testClass.getClassName().replaceAll("\\/", "."));
      }

      return runJunitTests(classLoader, testClassNames, log);
    } else {
      log.warn(String.format("No JUnit test classes found in %s", testCompilationFolder.getAbsolutePath()));

      // No tests. Claim they successfully ran anyway.
      return true;
    }
  }

  /**
   * Run all JUnit tests.
   *
   * @param classLoader
   *          the classloader for loading test classes
   * @param testClassNames
   *          the names of the detected JUnit classes
   * @param log
   *          logger for the test run
   *
   * @return {@code true} if all tests succeeded
   */
  private boolean runJunitTests(URLClassLoader classLoader, List<String> testClassNames, final Log log) {
    JUnitCore junit = new JUnitCore();

    junit.addListener(new RunListener() {
      @Override
      public void testRunStarted(Description description) throws Exception {
        // Don't care
      }

      @Override
      public void testRunFinished(Result result) throws Exception {
        // Don't care
      }

      @Override
      public void testStarted(Description description) throws Exception {
        // Don't care
      }

      @Override
      public void testFailure(Failure failure) throws Exception {
        reportFailure(failure, log);
      }

      @Override
      public void testAssumptionFailure(Failure failure) {
        reportFailure(failure, log);
      }

      @Override
      public void testFinished(Description description) throws Exception {
        // Don't care
      }

      @Override
      public void testIgnored(Description description) throws Exception {
        // Don't care
      }
    });

    log.info("Starting JUnit tests");

    boolean allSuceeded = true;
    for (String testClassName : testClassNames) {
      try {
        Class<?> testClass = classLoader.loadClass(testClassName);
        Result result = junit.run(testClass);

        log.info(String.format("Ran %2d tests in %4dms: %d failed, %d ignored. (%s)", result.getRunCount(),
            result.getRunTime(), result.getFailureCount(), result.getIgnoreCount(), testClassName));

        allSuceeded &= result.wasSuccessful();
      } catch (Exception e) {
        log.error(String.format("Error while running test class %s", testClassName), e);
      }
    }

    log.info("JUnit tests complete");

    return allSuceeded;
  }

  /**
   * Report a test failure.
   *
   * @param testFailure
   *          the test failure
   * @param log
   *          logger for the test run
   */
  private void reportFailure(Failure testFailure, Log log) {
    log.error(String.format("Test %s failed\n%s\n", testFailure.getTestHeader(), trimStackTrace(testFailure)));
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
}
