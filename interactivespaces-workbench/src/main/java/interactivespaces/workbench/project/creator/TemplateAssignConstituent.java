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

package interactivespaces.workbench.project.creator;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectContext;
import interactivespaces.workbench.project.constituent.BaseProjectConstituentBuilder;
import interactivespaces.workbench.project.constituent.ContainerConstituent;
import interactivespaces.workbench.project.constituent.ProjectConstituent;
import org.jdom.Element;

import java.io.File;

/**
 * A simple input/output pair specification.
 *
 * @author Trevor Pering
 */
public class TemplateAssignConstituent extends ContainerConstituent {

  /**
   * Project type for a template assign entry.
   */
  public static final String TYPE_NAME = "assign";

  /**
   * Attribute name for a name.
   */
  public static final String NAME_ATTRIBUTE_NAME = "name";

  /**
   * Attribute name for a value.
   */
  public static final String VALUE_ATTRIBUTE_NAME = "value";

  /**
   * Attribute name for an export clause.
   */
  public static final String EXPORT_ATTRIBUTE_NAME = "export";

  /**
   * The variable name.
   */
  private final String name;

  /**
   * The variable value.
   */
  private final String value;

  /**
   * The variable export target.
   */
  private final String export;

  /**
   * Create a new template variable entry.
   *
   * @param name
   *          var name
   * @param value
   *          assign value
   * @param export
   *          export target
   */
  public TemplateAssignConstituent(String name, String value, String export) {
    this.name = name;
    this.value = value;
    this.export = export;
  }

  /**
   * @return output path
   */
  public String getName() {
    return name;
  }

  /**
   * @return file input template path
   */
  public String getValue() {
    return value;
  }

  /**
   * @return export target
   */
  public String getExport() {
    return export;
  }

  @Override
  public void processConstituent(Project project, File stagingDirectory, ProjectContext context) {
    ProjectCreationContext projectCreationContext = (ProjectCreationContext) context;
    FreemarkerTemplater templater = context.getWorkbench().getTemplater();
    int evaluationPasses = 1;
    templater.processStringTemplate(projectCreationContext.getTemplateData(), getValue(), getName(), evaluationPasses);
    if (getExport() != null) {
      project.addAttribute(getExport(), getValue());
    }
  }

  /**
   * Factory for the constituent components.
   */
  public static class TemplateAssignConstituentFactory implements ProjectConstituentFactory {
    @Override
    public String getName() {
      return TYPE_NAME;
    }

    @Override
    public ProjectConstituentBuilder newBuilder() {
      return new TemplateAssignConstituentBuilder();
    }
  }

  /**
   * Builder class for new templates..
   */
  private static class TemplateAssignConstituentBuilder extends BaseProjectConstituentBuilder {

    @Override
    public ProjectConstituent buildConstituentFromElement(Element element, Project project) {
      String name = element.getAttributeValue(NAME_ATTRIBUTE_NAME);
      String value = element.getAttributeValue(VALUE_ATTRIBUTE_NAME);
      String export = element.getAttributeValue(EXPORT_ATTRIBUTE_NAME);
      if (name == null || value == null) {
        throw new SimpleInteractiveSpacesException(
            String.format("Assignment has missing name/value %s/%s", name, value));
      }
      return new TemplateAssignConstituent(name, value, export);
    }
  }
}
