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

package interactivespaces.workbench.project.ide;

import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectDependency;
import interactivespaces.workbench.project.ProjectTaskContext;
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
public class JavaEclipseIdeProjectCreatorSpecification implements EclipseIdeProjectCreatorSpecification {

  /**
   * Freemarker context variable for libraries which are part of the project.
   */
  private static final String FREEMARKER_CONTEXT_LIBS = "libs";

  /**
   * Freemarker context variable for sources which are part of the project.
   */
  private static final String FREEMARKER_CONTEXT_SOURCES = "srcs";

  /**
   * Freemarker context variable for dynamic projects which are part of the project.
   */
  private static final String FREEMARKER_CONTEXT_DYNAMIC_PROJECTS = "dynamicProjects";

  /**
   * The location of the eclipse classpath template file.
   */
  private static final String TEMPLATE_FILEPATH_ECLIPSE_CLASSPATH = "ide/eclipse/java-classpath.ftl";

  /**
   * The name of the Eclipse classpath file.
   */
  private static final String FILENAME_CLASSPATH_FILE = ".classpath";

  /**
   * The value for the Java builder in Eclipse.
   */
  private static final String ECLIPSE_BUILDER_JAVA = "org.eclipse.jdt.core.javabuilder";

  /**
   * The value for the Java nature for an Eclipse project.
   */
  private static final String ECLIPSE_NATURE_JAVA = "org.eclipse.jdt.core.javanature";

  /**
   * List of required sources for the project.
   */
  private final List<String> sourcesRequired;

  /**
   * List of optional sources for the project.
   */
  private final List<String> sourcesOptional;

  /**
   * The Java activity extensions.
   *
   * <p>
   * Can be {@code null}.
   */
  private final JavaProjectExtension extensions;

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a specification with {@code null} extensions.
   *
   * @param sourcesRequired
   *          the required source directories
   * @param sourcesOptional
   *          optional resources for the project
   */
  public JavaEclipseIdeProjectCreatorSpecification(List<String> sourcesRequired, List<String> sourcesOptional) {
    this(sourcesRequired, sourcesOptional, null);
  }

  /**
   * Construct a specification with extensions.
   *
   * @param sourcesRequired
   *          list of source directories for the project
   * @param sourcesOptional
   *          optional resources for the project
   * @param extensions
   *          the extensions to use, can be {@code null}
   */
  public JavaEclipseIdeProjectCreatorSpecification(List<String> sourcesRequired, List<String> sourcesOptional,
      JavaProjectExtension extensions) {
    this.sourcesRequired = sourcesRequired;
    this.sourcesOptional = sourcesOptional;
    this.extensions = extensions;
  }

  @Override
  public void addSpecificationData(Project project, ProjectTaskContext context, Map<String, Object> freemarkerContext) {
    freemarkerContext.put(ECLIPSE_PROJECT_FIELD_NATURES, Lists.newArrayList(ECLIPSE_NATURE_JAVA));
    freemarkerContext.put(ECLIPSE_PROJECT_FIELD_BUILDER, ECLIPSE_BUILDER_JAVA);
  }

  @Override
  public void writeAdditionalFiles(Project project, ProjectTaskContext context, Map<String, Object> freemarkerContext,
      FreemarkerTemplater templater) throws Exception {
    JavaProjectType projectType = context.getProjectType();

    List<Project> dynamicProjects = Lists.newArrayList();
    for (ProjectDependency dependency : project.getDependencies()) {
      if (dependency.isDynamic()) {
        Project dependencyProject = context.getWorkbenchTaskContext().getDynamicProjectFromProjectPath(dependency);
        if (dependencyProject != null) {
          dynamicProjects.add(dependencyProject);
        }
      }
    }

    List<File> projectLibs = Lists.newArrayList();
    projectType.getProjectClasspath(false, context, projectLibs, extensions, context.getWorkbenchTaskContext());

    List<String> sources = Lists.newArrayList(sourcesRequired);
    addNecessaryOptionalSources(project, sources);
    freemarkerContext.put(FREEMARKER_CONTEXT_SOURCES, sources);
    freemarkerContext.put(FREEMARKER_CONTEXT_LIBS, projectLibs);
    freemarkerContext.put(FREEMARKER_CONTEXT_DYNAMIC_PROJECTS, dynamicProjects);

    templater.writeTemplate(freemarkerContext,
        fileSupport.newFile(project.getBaseDirectory(), FILENAME_CLASSPATH_FILE), TEMPLATE_FILEPATH_ECLIPSE_CLASSPATH);
  }

  /**
   * Add any needed optional sources.
   *
   * <p>
   * These are added by checking to see if the source folder exists.
   *
   * @param project
   *          the project being checked
   * @param sources
   *          the sources list which will be added
   */
  private void addNecessaryOptionalSources(Project project, List<String> sources) {
    for (String sourceOptional : sourcesOptional) {
      File location = fileSupport.newFile(project.getBaseDirectory(), sourceOptional);
      if (location.exists()) {
        sources.add(sourceOptional);
      }
    }
  }
}
