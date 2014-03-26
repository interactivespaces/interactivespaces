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

package interactivespaces.workbench.project.activity;

import com.google.common.collect.Lists;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectConfigurationProperty;
import interactivespaces.workbench.project.builder.ProjectBuildContext;
import interactivespaces.workbench.project.constituent.BaseProjectConstituentBuilder;
import interactivespaces.workbench.project.constituent.ProjectConstituent;
import org.apache.commons.logging.Log;
import org.jdom.Element;

import java.io.File;
import java.util.List;

/**
 * The project file constituent for activity projects.
 *
 * @author Keith M. Hughes
 */
public class ActivityProjectConstituent implements ProjectConstituent {

  /**
   * Element type for a resource.
   */
  public static final String TYPE_NAME = "activity";

  /**
   * XML entity name for an activity.
   */
  public static final String ACTIVITY_ELEMENT = "activity";

  /**
   * XML attribute on the activity tag for the activity type.
   */
  public static final String ACTIVITY_TYPE_ATTRIBUTE = "type";

  /**
   * XML attribute on the activity tag for the activity type.
   */
  public static final String LOG_LEVEL_ATTRIBUTE = "logLevel";

  /**
   * XML entity name giving the name of the activity.
   */
  public static final String ACTIVITY_NAME_ELEMENT = "name";

  /**
   * XML entity name giving the executable.
   */
  public static final String ACTIVITY_EXECUTABLE_ELEMENT = "executable";

  /**
   * XML entity name giving the java class.
   */
  public static final String ACTIVITY_CLASS_ELEMENT = "class";

  /**
   * XML entity name giving the configuration for the activity.
   */
  public static final String ACTIVITY_CONFIGURATION_ELEMENT = "configuration";

  /**
   * XML attribute name giving the name of a configuration property.
   */
  public static final String CONFIGURATION_PROPERTY_NAME_ATTRIBUTE = "name";

  /**
   * XML attribute name giving whether a configuration property is required.
   */
  public static final String CONFIGURATION_PROPERTY_REQUIRED_ATTRIBUTE = "required";

  /**
   * XML element name giving the description of a configuration property.
   */
  public static final String CONFIGURATION_PROPERTY_DESCRIPTION_ELEMENT = "description";

  /**
   * XML element or attribute (can be either) name giving the value of a
   * configuration property.
   */
  public static final String CONFIGURATION_PROPERTY_VALUE = "value";

  @Override
  public void processConstituent(Project project, File stagingDirectory, ProjectBuildContext context) {
    // Nothing to do
  }

  @Override
  public String getSourceDirectory() throws SimpleInteractiveSpacesException {
    return null;
  }

  /**
   * Factory for building the constituent builder.
   *
   * @author Keith M. Hughes
   */
  public static class ActivityProjectBuilderFactory implements ProjectConstituentFactory {
    @Override
    public String getName() {
      return TYPE_NAME;
    }

    @Override
    public ProjectConstituentBuilder newBuilder(Log log) {
      return new ActivityProjectBuilder(log);
    }
  }

  /**
   * Builder class for creating new activity instances.
   */
  private static class ActivityProjectBuilder extends BaseProjectConstituentBuilder {

    /**
     * Construct a new builder.
     *
     * @param log
     *          logger for the builder
     */
    ActivityProjectBuilder(Log log) {
      super(log);
    }

    @Override
    public ProjectConstituent buildConstituentFromElement(Element resourceElement, Project project) {
      ActivityProject aproject = (ActivityProject) project;

      aproject.setActivityType(
          resourceElement.getAttributeValue(ACTIVITY_TYPE_ATTRIBUTE, aproject.getActivityType()));
      aproject.setActivityRuntimeName(
          getChildTextNormalize(resourceElement, ACTIVITY_NAME_ELEMENT, aproject.getActivityRuntimeName()));
      aproject.setActivityExecutable(
          getChildTextNormalize(resourceElement, ACTIVITY_EXECUTABLE_ELEMENT, aproject.getActivityExecutable()));
      aproject.setActivityClass(
          getChildTextNormalize(resourceElement, ACTIVITY_CLASS_ELEMENT, aproject.getActivityClass()));

      List<ProjectConfigurationProperty> configurationProperties =
          getProjectConfigurationProperty(resourceElement.getChild(ACTIVITY_CONFIGURATION_ELEMENT));
      aproject.addConfigurationProperties(configurationProperties);

      return null;
    }

    /**
     * Get all configuration properties from the activity configuration element.
     *
     * @param configurationElement
     *          the configuration element, can be {@code null}
     *
     * @return a possibly empty list of properties
     */
    @SuppressWarnings("unchecked")
    private List<ProjectConfigurationProperty> getProjectConfigurationProperty(Element configurationElement) {
      List<ProjectConfigurationProperty> properties = Lists.newArrayList();

      if (configurationElement != null) {
        for (Element propertyElement : (List<Element>) configurationElement.getChildren("property")) {
          processConfigurationPropertyElement(propertyElement, properties);
        }
      }

      return properties;
    }

    /**
     * Process a configuration property element.
     *
     * @param propertyElement
     *          the property element
     * @param properties
     *          the list of properties being built
     */
    private void processConfigurationPropertyElement(Element propertyElement,
        List<ProjectConfigurationProperty> properties) {
      String name = propertyElement.getAttributeValue(CONFIGURATION_PROPERTY_NAME_ATTRIBUTE);
      String description = propertyElement.getChildTextNormalize(CONFIGURATION_PROPERTY_DESCRIPTION_ELEMENT);
      String requiredAttribute = propertyElement.getAttributeValue(CONFIGURATION_PROPERTY_REQUIRED_ATTRIBUTE);
      boolean required = "true".equals(requiredAttribute);

      String valueAttribute = propertyElement.getAttributeValue(CONFIGURATION_PROPERTY_VALUE);
      String valueChild = propertyElement.getChildTextNormalize(CONFIGURATION_PROPERTY_VALUE);
      String value = valueAttribute;
      if (valueAttribute != null) {
        if (valueChild != null) {
          addWarn(String.format(
              "Configuration property %s has both an attribute and child element giving the value. "
                  + "The child element is being used.", name));
          value = valueChild;
        }
      } else {
        value = valueChild;
      }

      properties.add(new ProjectConfigurationProperty(name, value, required, description));
    }
  }
}
