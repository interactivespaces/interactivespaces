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

import org.objectweb.asm.ClassReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A detector for classes with JUnit tests in them.
 *
 * @author Keith M. Hughes
 */
public class JunitTestClassDetector {

  /**
   * File extension for class files.
   */
  public static final String CLASS_FILE_EXTENSION = ".class";

  /**
   * Find all test classes in the given directory.
   *
   * @param directory
   *          the directory to start looking for classes
   *
   * @return list of all test classes in the directory
   */
  public List<JunitTestClassVisitor> findTestClasses(File directory) {
    List<JunitTestClassVisitor> testClasses = new ArrayList<JunitTestClassVisitor>();

    scanDirectory(directory, testClasses);

    return testClasses;
  }

  /**
   * Get all test classes.
   *
   * @param directory
   *          the directory to scan
   * @param testClasses
   *          a list to put the found classes in
   */
  private void scanDirectory(File directory, List<JunitTestClassVisitor> testClasses) {
    File[] contents = directory.listFiles();
    if (contents != null) {
      for (File file : contents) {
        if (file.isFile()) {
          if (file.getName().endsWith(CLASS_FILE_EXTENSION)) {
            JunitTestClassVisitor classVisitor = newClassVisitor(file);
            if (classVisitor.isTestClass() && !classVisitor.isAbstractClass()) {
              testClasses.add(classVisitor);
            }
          }

        } else if (file.isDirectory()) {
          scanDirectory(file, testClasses);
        }
      }
    }
  }

  /**
   * Create a class visitor for the class to be examined for JUnit tests.
   *
   * @param testClassFile
   *          the file to be examined
   *
   * @return the class visitor for scanning the class
   */
  private JunitTestClassVisitor newClassVisitor(File testClassFile) {
    JunitTestClassVisitor classVisitor = new JunitTestClassVisitor();

    InputStream classStream = null;
    try {
      classStream = new BufferedInputStream(new FileInputStream(testClassFile));

      ClassReader classReader = new ClassReader(classStream);
      classReader.accept(classVisitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
    } catch (Throwable e) {
      throw new RuntimeException(String.format("Could not process class file %s", testClassFile.getAbsolutePath()), e);
    } finally {
      if (classStream != null) {
        try {
          classStream.close();
        } catch (IOException e) {
          // Don't care
        }
      }
    }

    return classVisitor;
  }
}
