package interactivespaces.util;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.net.URLClassLoader;
import java.util.List;

/**
 * This is a bridge class that cleanly isolates the JUnit testing from the rest of the system. This is necessary
 * to provide a clean class loader boundary for isolating classes. Although nominally only used by the build
 * environment (workbench), this needs to be in the core module that is included by both the workbench and
 * controller, such that it appears to be part of the eventual runtime environment.
 *
 * @author Trevor Pering
 */
public class TestRunnerBridge {

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

    boolean allSuceeded = true;
    for (String testClass : testClassNames) {
      try {
        Class<?> clazz = classLoader.loadClass(testClass);
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
  }

}
