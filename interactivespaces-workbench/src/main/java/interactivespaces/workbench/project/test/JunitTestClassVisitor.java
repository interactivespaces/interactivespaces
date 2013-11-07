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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * An ASM class visitor for detecting JUnit classes.
 *
 * @author Keith M. Hughes
 */
public class JunitTestClassVisitor extends ClassVisitor {

  /**
   * The classname for the JUnit Test annotation formatted as ASM will see it.
   */
  private static final String JUNIT_ANNOTATION_TEST = "L"
      + Test.class.getName().replaceAll("\\.", "/") + ";";

  /**
   * The classname for the JUnit RunWith annotation formatted as ASM will see it.
   */
  private static final String JUNIT_ANNOTATION_RUNWITH = "L"
      + RunWith.class.getName().replaceAll("\\.", "/") + ";";

  /**
   * The class name of the visited class.
   */
  private String className;

  /**
   * The name of the superclass of the visited class.
   */
  private String superClassName;

  /**
   * {@code true} if the class being visited is abstract.
   */
  private boolean abstractClass;

  /**
   * {@code true} if the class being visited contains JUnit tests.
   */
  private boolean testClass;

  /**
   * Construct a new class visitor.
   */
  public JunitTestClassVisitor() {
    super(Opcodes.ASM4);
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName,
      String[] interfaces) {
    abstractClass = (access & Opcodes.ACC_ABSTRACT) != 0;

    this.className = name;
    this.superClassName = superName;
  }

  @Override
  public void visitInnerClass(String name, String outerName, String innerName, int access) {
    if (name.equals(className) && (access & Opcodes.ACC_STATIC) == 0) {
      abstractClass = true;
    }
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    if (JUNIT_ANNOTATION_RUNWITH.equals(desc)) {
      testClass = true;
    }

    return null;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature,
      String[] exceptions) {
    if (!testClass) {
      return new JunitTestMethodVisitor();
    } else {
      return null;
    }
  }

  /**
   * Get the name of the class.
   *
   * @return the name of the class
   */
  public String getClassName() {
    return className;
  }

  /**
   * Get the name of the superclass.
   *
   * @return the name of the superclass
   */
  public String getSuperClassName() {
    return superClassName;
  }

  /**
   * Is the class abstract?
   *
   * @return {@code true} if the class is abstract
   */
  public boolean isAbstractClass() {
    return abstractClass;
  }

  /**
   * Is this a class that contains tests?
   *
   * @return {@code true} if the class contains tests
   */
  public boolean isTestClass() {
    return testClass;
  }

  /**
   * Set whether the class is a test class.
   *
   * @param testClass
   *          {@code true} if a test class
   */
  private void setTestClass(boolean testClass) {
    this.testClass = testClass;
  }

  /**
   * An ASM method visitor for finding methods annotated with the JUnit test
   * annotation.
   *
   * @author Keith M. Hughes
   */
  public class JunitTestMethodVisitor extends MethodVisitor {

    /**
     * Construct a method visitor.
     */
    JunitTestMethodVisitor() {
      super(Opcodes.ASM4);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      if (JUNIT_ANNOTATION_TEST.equals(desc)) {
        setTestClass(true);
      }

      return null;
    }
  }
}
