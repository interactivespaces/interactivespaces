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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.project.JdomProjectReader;
import interactivespaces.workbench.project.Project;
import org.apache.commons.logging.Log;
import org.jdom.Element;

import java.io.File;
import java.util.List;

/**
 * @author Trevor Pering
 */
public class JdomConfederacyReader {

  public static final String CONFEDERACY_ELEMENT_NAME = "confederacy";


  private static final String BASE_DIRECTORY_ELEMENT_NAME = "baseDirectory";

  private final PrototypeManager prototypeManager = new PrototypeManager();

  /**
   * Log for errors.
   */
  private final Log log;

  /**
   * {@code true} if read was successful.
   */
  private boolean failure;

  /**
   * Construct a project reader.
   *
   * @param log
   *          the logger to use
   */
  public JdomConfederacyReader(Log log) {
    this.log = log;
  }

  public void processSpecification(Confederacy confederacy, Element rootElement) {
    if (!CONFEDERACY_ELEMENT_NAME.equals(rootElement.getName())) {
      throw new SimpleInteractiveSpacesException("Illegal root element name " + rootElement.getName());
    }

    @SuppressWarnings("unchecked")
    List<Element> children = (List<Element>) rootElement.getChildren();
    for (Element child : children) {
      addElementToSpec(confederacy, child);
    }

    if (failure) {
      throw new SimpleInteractiveSpacesException("Project had errors");
    }
  }

  private void addElementToSpec(Confederacy spec, Element child) {
    String name = child.getName();

    try {
      if (BASE_DIRECTORY_ELEMENT_NAME.equals(name)) {
        spec.setBaseDirectory(new File(child.getTextTrim()));
      } else if (JdomProjectReader.PROJECT_ELEMENT_NAME.equals(name)) {
        addProjectElementToSpec(spec, child);
      } else if (PrototypeManager.PROTOTYPE_ELEMENT_NAME.equals(name)) {
        addPrototypeElementToSpec(spec, child);
      } else {
        throw new SimpleInteractiveSpacesException("Unrecognized element");
      }
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("While processing confederacy element: " + name, e);
    }
  }

  private void addPrototypeElementToSpec(Confederacy spec, Element child) {
    prototypeManager.addPrototypeElement(child);
  }

  private void addProjectElementToSpec(Confederacy spec, Element child) {
    if (spec.getBaseDirectory() == null) {
      throw new SimpleInteractiveSpacesException("Confederacy base directory not defined before projects");
    }
    Project project = new JdomProjectReader(log, prototypeManager).processSpecification(child);
    project.setBaseDirectory(new File(spec.getBaseDirectory(), project.getIdentifyingName()));
    project.setSpecificationSource(spec.getSpecificationSource());
    spec.addProject(project);
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
