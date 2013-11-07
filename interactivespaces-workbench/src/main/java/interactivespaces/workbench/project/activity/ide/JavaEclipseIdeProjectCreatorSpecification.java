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

package interactivespaces.workbench.project.activity.ide;

import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.builder.ProjectBuildContext;
import interactivespaces.workbench.project.java.JavaProjectExtension;
import interactivespaces.workbench.project.java.JavaProjectType;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Specification for Java projects.
 *
 * @author Keith M. Hughes
 */
public class JavaEclipseIdeProjectCreatorSpecification implements
    EclipseIdeProjectCreatorSpecification {

  /**
   * The location of the eclipse classpath template file.
   */
  private static final String TEMPLATE_FILEPATH_ECLIPSE_CLASSPATH =
      "ide/eclipse/java-classpath.ftl";

  /**
   * The name of the Eclipse classpath file.
   */
  private static final String FILENAME_CLASSPATH_FILE = ".classpath";

  /**
   * The value for the Java builder in Eclipse.
   */
  private static final String ECLIPSE_BUILDER_JAVA = "org.eclipse.jdt.core.javabuilder";

  /**
   * The value for the Java nature for an eclpse project.
   */
  private static final String ECLIPSE_NATURE_JAVA = "org.eclipse.jdt.core.javanature";

  /**
   * List of sources for the project.
   */
  private final List<String> sources;

  /**
   * The Java activity extensions.
   *
   * <p>
   * Can be {@code null}.
   */
  private final JavaProjectExtension extensions;

  /**
   * Construct a specification with {@code null} extensions.
   *
   * @param sources
   *          the source directories
   */
  public JavaEclipseIdeProjectCreatorSpecification(List<String> sources) {
    this(sources, null);
  }

  /**
   * Construct a specification with extensions.
   *
   * @param sources
   *          list of source directories for the project
   * @param extensions
   *          the extensions to use (can be {@code null})
   */
  public JavaEclipseIdeProjectCreatorSpecification(List<String> sources,
      JavaProjectExtension extensions) {
    this.sources = sources;
    this.extensions = extensions;
  }

  @Override
  public void addSpecificationData(Project project, ProjectBuildContext context,
      Map<String, Object> freemarkerContext) {
    freemarkerContext.put(ECLIPSE_PROJECT_FIELD_NATURES, Lists.newArrayList(ECLIPSE_NATURE_JAVA));
    freemarkerContext.put(ECLIPSE_PROJECT_FIELD_BUILDER, ECLIPSE_BUILDER_JAVA);
  }

  @Override
  public void writeAdditionalFiles(Project project, ProjectBuildContext context,
      Map<String, Object> freemarkerContext, FreemarkerTemplater templater) throws Exception {
    JavaProjectType projectType = context.getProjectType();

    List<File> projectLibs = Lists.newArrayList();
    projectType.getProjectClasspath(projectLibs, extensions, context.getWorkbench());

    freemarkerContext.put("srcs", sources);
    freemarkerContext.put("libs", projectLibs);

    templater.writeTemplate(freemarkerContext, new File(project.getBaseDirectory(),
        FILENAME_CLASSPATH_FILE), TEMPLATE_FILEPATH_ECLIPSE_CLASSPATH);
  }
}
