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

package interactivespaces.domain.support;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.basic.ActivityDependency;
import interactivespaces.domain.basic.pojo.SimpleActivityDependency;
import interactivespaces.resource.VersionRange;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * An {@link ActivityDescriptionReader} which uses JDOM to process the XML.
 *
 * @author Keith M. Hughes
 */
public class JdomActivityDescriptionReader implements ActivityDescriptionReader {

  /**
   * The value in a project file for true.
   */
  public static final String ACTIVITY_VALUE_TRUE = "true";

  /**
   * The value in a project file for false.
   */
  public static final String ACTIVITY_VALUE_FALSE = "false";

  /**
   * Project definition file element name for dependencies.
   */
  public static final String ACTIVITY_ELEMENT_NAME_DEPENDENCIES = "dependencies";

  /**
   * Project definition file element name for a dependency item.
   */
  public static final String ACTIVITY_ELEMENT_NAME_DEPENDENCY_ITEM = "dependency";

  /**
   * Project definition file attribute name for the identifying name of a dependency.
   */
  public static final String ACTIVITY_ATTRIBUTE_NAME_DEPENDENCY_ITEM_IDENTIFYING_NAME = "identifyingName";

  /**
   * Project definition file attribute name for the version range of a dependency.
   */
  public static final String ACTIVITY_ATTRIBUTE_NAME_DEPENDENCY_ITEM_VERSION = "version";

  /**
   * Project definition file attribute name for whether a dependency is required.
   */
  public static final String ACTIVITY_ATTRIBUTE_NAME_DEPENDENCY_ITEM_REQUIRED = "required";

  /**
   * Project definition file attribute default value for whether a dependency is required.
   */
  public static final String ACTIVITY_ATTRIBUTE_VALUE_DEFAULT_DEPENDENCY_ITEM_REQUIRED = ACTIVITY_VALUE_TRUE;

  @Override
  public ActivityDescription readDescription(InputStream activityDescriptionStream) {
    try {
      SAXBuilder builder = new SAXBuilder();
      Document doc = builder.build(activityDescriptionStream);

      Element rootElement = doc.getRootElement();

      ActivityDescription description = new ActivityDescription();

      List<String> errors = Lists.newArrayList();

      getMainData(description, rootElement, errors);
      getMetadata(description, rootElement, errors);
      getDependencies(description, rootElement, errors);

      return description;
    } catch (Exception e) {
      throw new InteractiveSpacesException("Unable to read activity descriptiuon", e);
    }

  }

  /**
   * Get the main data from the document.
   *
   * @param adescription
   *          the activity description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the activity data
   * @param errors
   *          any errors found in the metadata
   */
  private void getMainData(ActivityDescription adescription, Element rootElement, List<String> errors) {
    String name = rootElement.getChildText("name");
    adescription.setName(new String(name.trim()));

    String description = rootElement.getChildText("description");
    if (description != null) {
      adescription.setDescription(new String(description.trim()));
    }

    String identifyingName = rootElement.getChildText("identifyingName");
    adescription.setIdentifyingName(new String(identifyingName.trim()));

    String version = rootElement.getChildText("version");
    adescription.setVersion(new String(version.trim()));

    String builder = rootElement.getAttributeValue("builder");
    adescription.setBuilderType(builder);
  }

  /**
   * Get the metadata from the document.
   *
   * @param adescription
   *          the activity description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the activity data
   * @param errors
   *          any errors found in the metadata
   */
  private void getMetadata(ActivityDescription adescription, Element rootElement, List<String> errors) {
    Element metadataElement = rootElement.getChild("metadata");

    if (metadataElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> itemElements = metadataElement.getChildren("item");

      Map<String, Object> metadata = Maps.newHashMap();
      for (Element itemElement : itemElements) {
        String name = itemElement.getAttributeValue("name");
        String value = itemElement.getTextNormalize();
        metadata.put(name, value);
      }

      adescription.setMetadata(metadata);
    }
  }

  /**
   * Get the dependencies from the document.
   *
   * @param adescription
   *          the activity description whose data is being read
   * @param rootElement
   *          root element of the XML DOM containing the activity data
   * @param errors
   *          any errors found in the metadata
   */
  private void getDependencies(ActivityDescription adescription, Element rootElement, List<String> errors) {
    Element dependenciesElement = rootElement.getChild(ACTIVITY_ELEMENT_NAME_DEPENDENCIES);
    if (dependenciesElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> dependencyElements = dependenciesElement.getChildren(ACTIVITY_ELEMENT_NAME_DEPENDENCY_ITEM);

      List<ActivityDependency> dependencies = Lists.newArrayList();
      for (Element dependencyElement : dependencyElements) {
        ActivityDependency dependency = getDependency(dependencyElement, errors);
        if (dependency != null) {
          dependencies.add(dependency);
        }
      }

      adescription.setDependencies(dependencies);
    }
  }

  /**
   * Get an activity dependency from the dependency element.
   *
   * @param dependencyElement
   *          the element containing the data
   * @param errors
   *          any errors found in the metadata
   *
   * @return the dependency found in the element, or {@code null} if an error
   */
  private ActivityDependency getDependency(Element dependencyElement, List<String> errors) {
    String identifyingName =
        dependencyElement.getAttributeValue(ACTIVITY_ATTRIBUTE_NAME_DEPENDENCY_ITEM_IDENTIFYING_NAME);
    if (identifyingName == null) {
      errors.add("Dependency has no identifyingName");
      return null;
    }

    VersionRange version = null;
    String versionStr = dependencyElement.getAttributeValue(ACTIVITY_ATTRIBUTE_NAME_DEPENDENCY_ITEM_VERSION);
    if (versionStr != null) {
      version = VersionRange.parseVersionRange(versionStr);
    } else {
      errors.add("Dependency has no version range");
      return null;
    }

    String requiredString =
        dependencyElement.getAttributeValue(ACTIVITY_ATTRIBUTE_VALUE_DEFAULT_DEPENDENCY_ITEM_REQUIRED,
            ACTIVITY_ATTRIBUTE_VALUE_DEFAULT_DEPENDENCY_ITEM_REQUIRED);

    ActivityDependency dependency = new SimpleActivityDependency();

    dependency.setIdentifyingName(identifyingName);
    dependency.setMinimumVersion(version.getMinimum().toString());
    dependency.setMaximumVersion(version.getMaximum().toString());
    dependency.setRequired(ACTIVITY_VALUE_TRUE.equals(requiredString));

    return dependency;
  }
}
