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

package interactivespaces.workbench.confederate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.resource.Version;
import interactivespaces.resource.VersionRange;
import interactivespaces.workbench.JdomReader;
import interactivespaces.workbench.project.JdomProjectReader;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectDependency;
import interactivespaces.workbench.project.ProjectDeployment;
import interactivespaces.workbench.project.ProjectReader;
import interactivespaces.workbench.project.ProjectTypes;
import interactivespaces.workbench.project.activity.ActivityProjectConstituent;
import interactivespaces.workbench.project.constituent.ProjectAssemblyConstituent;
import interactivespaces.workbench.project.constituent.ProjectBundleConstituent;
import interactivespaces.workbench.project.constituent.ProjectConstituent;
import interactivespaces.workbench.project.constituent.ProjectResourceConstituent;
import org.apache.commons.logging.Log;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Trevor Pering
 */
public class ConfederacyReader  {

  public static final String ROOT_ELEMENT_NAME = "confederacy";

  /**
   * Log for errors.
   */
  private final Log log;

  ConfederacySpecification spec = new ConfederacySpecification();

  /**
   * {@code true} if read was successful.
   */
  private boolean failure;

  private final Map<String, JdomReader> elementReaders = Maps.newHashMap();

  {
    elementReaders.put(JdomProjectReader.ROOT_ELEMENT_NAME, new JdomProjectReader(log) {
      @Override
      public void handleResult(Project result) {
        addProject(result);
      }
    });
  }

  /**
   * Construct a project reader.
   *
   * @param log
   *          the logger to use
   */
  public ConfederacyReader(Log log) {
    this.log = log;
  }


  public ConfederacySpecification readSpecification(File inputFile) {
    FileInputStream inputStream = null;
    try {
      Document doc = null;
      inputStream = new FileInputStream(inputFile);
      SAXBuilder builder = new SAXBuilder();
      doc = builder.build(inputStream);

      Element rootElement = doc.getRootElement();

      if (ROOT_ELEMENT_NAME.equals(rootElement.getName())) {
        throw new SimpleInteractiveSpacesException("Illegal root element name " + rootElement.getName());
      }

      @SuppressWarnings("unchecked")
      List<Element> children = (List<Element>) rootElement.getChildren();
      for (Element child : children) {
        @SuppressWarnings("unchecked")
        JdomReader<Object> reader = elementReaders.get(child.getName());
        if (reader == null) {
          throw new SimpleInteractiveSpacesException("Unrecognized element name: " + child.getName());
        }
        reader.handleResult(reader.processElement(child));
      }

      if (failure) {
        throw new SimpleInteractiveSpacesException("Project had errors");
      }

      return spec;
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException(String.format("Exception while processing confederacy file %s",
          inputFile.getAbsolutePath()), e);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
  }

  private void addProject(Project project) {
  }

  /**
   * An error has occurred.
   *
   * @param error
   *          text of the error message
   */
  private void addError(String error) {
    log.error(error);
    failure = true;
  }
}
