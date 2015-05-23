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

import org.apache.commons.logging.Log;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A detector for classes with JUnit tests in them.
 *
 * @author Keith M. Hughes
 */
public class JunitTestClassDetector {

  /**
   * The separator between a file name and the file extension.
   */
  public static final char FILE_EXTENSION_SEPARATOR = '.';

  /**
   * File extension for class files.
   */
  public static final String CLASS_FILE_EXTENSION = FILE_EXTENSION_SEPARATOR + "class";

  /**
   * The separator between the components of a Java package path.
   */
  public static final char CLASS_PACKAGE_COMPONENT_SEPARATOR = '.';

  /**
   * Find all test classes in the given directory.
   *
   * @param directory
   *          the directory to start looking for classes
   * @param classLoader
   *          the classloader for the test classes
   * @param log
   *          logger for the test run
   *
   * @return list of all test classes in the directory
   */
  public List<Class<?>> findTestClasses(File directory, ClassLoader classLoader, Log log) {
    List<Class<?>> testClasses = new ArrayList<Class<?>>();

    scanDirectory(directory, testClasses, new StringBuilder(), classLoader, log);

    return testClasses;
  }

  /**
   * Get all test classes.
   *
   * @param directory
   *          the directory to scan
   * @param testClasses
   *          a list to put the found classes in
   * @param packagePrefix
   *          the prefix for Java packages for files in this directory
   * @param classLoader
   *          the classloader for the test classes
   * @param log
   *          logger for the test run
   */
  private void scanDirectory(File directory, List<Class<?>> testClasses, StringBuilder packagePrefix,
      ClassLoader classLoader, Log log) {
    File[] contents = directory.listFiles();
    if (contents != null) {
      for (File file : contents) {
        String fileName = file.getName();
        if (file.isFile()) {
          if (fileName.endsWith(CLASS_FILE_EXTENSION)) {
            testClassForTests(packagePrefix, fileName, classLoader, testClasses, log);
          }

        } else if (file.isDirectory()) {
          int length = packagePrefix.length();
          if (packagePrefix.length() != 0) {
            packagePrefix.append(CLASS_PACKAGE_COMPONENT_SEPARATOR);
          }
          packagePrefix.append(fileName);
          scanDirectory(file, testClasses, packagePrefix, classLoader, log);
          packagePrefix.setLength(length);
        }
      }
    }
  }

  /**
   * Test the specified class for any test methods.
   *
   * <p>
   * Superclasses are searched.
   *
   * @param packagePrefix
   *          the prefix package path
   * @param classFileName
   *          the classname for the potential test class
   * @param classLoader
   *          the classloader for test classes
   * @param testClasses
   *          the growing list of classes to test
   * @param log
   *          the logger to use
   */
  private void testClassForTests(StringBuilder packagePrefix, String classFileName, ClassLoader classLoader,
      List<Class<?>> testClasses, Log log) {
    StringBuilder fullyQualifiedClassName = new StringBuilder(packagePrefix);
    if (fullyQualifiedClassName.length() != 0) {
      fullyQualifiedClassName.append(CLASS_PACKAGE_COMPONENT_SEPARATOR);
    }
    fullyQualifiedClassName.append(classFileName.substring(0, classFileName.indexOf(FILE_EXTENSION_SEPARATOR)));
    try {
      Class<?> potentialTestClass = classLoader.loadClass(fullyQualifiedClassName.toString());

      if (isTestClass(potentialTestClass, log)) {
        testClasses.add(potentialTestClass);
      }
    } catch (Exception e) {
      log.error(String.format("Could not test class %s for test methods", fullyQualifiedClassName));
    }
  }

  /**
   * Is the class a JUnit test class?
   *
   * @param potentialTestClass
   *          the class to test
   * @param log
   *          the logger to use
   *
   * @return {@code true} if the class is testable and can be instantiated
   */
  private boolean isTestClass(Class<?> potentialTestClass, Log log) {
    if (potentialTestClass.isInterface() || !classHasPublicConstructor(potentialTestClass, log)) {
      return false;
    }

    for (Method method : potentialTestClass.getMethods()) {
      if (method.isAnnotationPresent(Test.class)) {

        // No need to check any more if we have 1
        return true;
      }
    }

    return false;
  }

  /**
   * Does the class have a no argument public constructor?
   *
   * @param clazz
   *          the class to test
   * @param log
   *          the logger to use
   *
   * @return {@code true} if the class has a public no arg constructor
   */
  private boolean classHasPublicConstructor(Class<?> clazz, Log log) {
    try {
      clazz.getConstructor();

      return true;
    } catch (NoSuchMethodException e) {
      return false;
    } catch (SecurityException e) {
      log.warn(String.format("Potential test class %s caused a security exception. Ignoring.", clazz.getName()), e);

      return false;
    }
  }
}
