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

import com.google.common.collect.Maps;
import interactivespaces.InteractiveSpacesException;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectConfigurationProperty;
import interactivespaces.workbench.project.ProjectContext;
import interactivespaces.workbench.project.constituent.BaseProjectConstituentBuilder;
import interactivespaces.workbench.project.constituent.ProjectConstituent;
import org.jdom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
   * Project definition file element name for a configuration item.
   */
  public static final String PROPERTY_ELEMENT_NAME = "property";

  /**
   * XML element or attribute (can be either) name giving the value of a
   * configuration property.
   */
  public static final String CONFIGURATION_PROPERTY_VALUE = "value";

  @Override
  public void processConstituent(Project project, File stagingDirectory, ProjectContext context) {
    // Nothing to do
  }

  @Override
  public String getSourceDirectory() throws InteractiveSpacesException {
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
    public ProjectConstituentBuilder newBuilder() {
      return new ActivityProjectBuilder();
    }
  }

  /**
   * Builder class for creating new activity instances.
   */
  private static class ActivityProjectBuilder extends BaseProjectConstituentBuilder {

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

      // Use a map internally, but return a list, to coalesce multiple properties with the same name together
      // into one item (e.g., if one comes from a prototype). Also use a tree map so the output is sorted, just for
      // convenience.
      Map<String, ProjectConfigurationProperty> properties = Maps.newTreeMap();

      if (configurationElement != null) {
        for (Element propertyElement : (List<Element>) configurationElement.getChildren(PROPERTY_ELEMENT_NAME)) {
          processConfigurationPropertyElement(propertyElement, properties);
        }
      }

      return new ArrayList(properties.values());
    }

    /**
     * Process a configuration property element.
     *
     * @param propertyElement
     *          the property element
     * @param properties
     *          the properties map to populate
     */
    private void processConfigurationPropertyElement(Element propertyElement,
        Map<String, ProjectConfigurationProperty> properties) {
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

      properties.put(name, new ProjectConfigurationProperty(name, description, required, value));
    }
  }
}
