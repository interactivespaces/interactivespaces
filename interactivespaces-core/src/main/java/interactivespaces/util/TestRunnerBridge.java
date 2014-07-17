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

package interactivespaces.util;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.net.URLClassLoader;
import java.util.List;

/**
 * This is a bridge class that does JUnit testing for the workbench.
 *
 * @author Trevor Pering
 */
public class TestRunnerBridge extends RunListener {

  /**
   * Method name to use for running tests. This is used for reflection, but can't (easily) be specified in code.
   */
  public static final String RUNNER_METHOD_NAME = "runTests";

  /**
   * Run the given tests in the given classLoader.
   *
   * @param testClassNames
   *          names of classes to test
   * @param classLoader
   *          class loader to use for running tests
   *
   * @return {@code true} if all tests passed
   */
  public static boolean runTests(List<String> testClassNames, URLClassLoader classLoader) {
    JUnitCore junit = new JUnitCore();
    junit.addListener(new TestRunnerBridge());

    boolean allSuceeded = true;
    for (String testClass : testClassNames) {
      try {
        Class<?> clazz = classLoader.loadClass(testClass);
        Result result = junit.run(clazz);

        System.out.format("Ran %2d tests in %4dms: %d failed, %d ignored. (%s)\n",
            result.getRunCount(), result.getRunTime(), result.getFailureCount(), result.getIgnoreCount(), testClass);

        allSuceeded &= result.wasSuccessful();
      } catch (ClassNotFoundException e) {
        // TODO: replace with a proper logging mechanism.
        e.printStackTrace();
      }
    }
    return allSuceeded;
  }

  @Override
  public void testRunStarted(Description description) throws Exception {
    //System.out.println("Starting JUnit tests");
  }

  @Override
  public void testRunFinished(Result result) throws Exception {
    //System.out.println("Finished JUnit tests.");
  }

  @Override
  public void testStarted(Description description) throws Exception {
    //System.out.format("Starting test %s \n", description.getDisplayName());
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
    //System.out.format("Finished test %s \n", description.getDisplayName());
  }

  @Override
  public void testIgnored(Description description) throws Exception {
    //System.out.format("Ignoring test %s\n", description.getDisplayName());
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
