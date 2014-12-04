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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * An {@link ActivityDescriptionReader} which uses JDOM to process the XML.
 *
 * @author Keith M. Hughes
 */
public class JdomActivityDescriptionReader implements ActivityDescriptionReader {

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
    Element dependenciesElement = rootElement.getChild("dependencies");

    if (dependenciesElement != null) {
      @SuppressWarnings("unchecked")
      List<Element> dependencyElements = dependenciesElement.getChildren("dependency");

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
    String identifyingName = dependencyElement.getAttributeValue("identifyingName");
    if (identifyingName == null) {
      identifyingName = dependencyElement.getAttributeValue("name");
      if (identifyingName == null) {
        errors.add("Dependency has no name");
        return null;
      }
    }

    String minimumVersion = dependencyElement.getAttributeValue("minimumVersion");
    String maximumVersion = dependencyElement.getAttributeValue("maximumVersion");

    if (minimumVersion != null) {
      if (maximumVersion == null) {
        maximumVersion = minimumVersion;
      }
    } else if (maximumVersion != null) {
      // If here was no minimum version
      minimumVersion = maximumVersion;
    } else {
      errors.add("Dependency has no version constraints");
      return null;
    }

    String requiredString = dependencyElement.getAttributeValue("required", "true");

    ActivityDependency dependency = new SimpleActivityDependency();

    dependency.setIdentifyingName(identifyingName);
    dependency.setMinimumVersion(minimumVersion);
    dependency.setMaximumVersion(maximumVersion);
    dependency.setRequired("true".equals(requiredString));

    return dependency;
  }
}
