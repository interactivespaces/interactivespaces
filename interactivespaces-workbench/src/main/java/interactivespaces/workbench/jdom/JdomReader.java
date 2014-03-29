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

package interactivespaces.workbench.jdom;

import com.google.common.io.Closeables;
import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import org.apache.commons.logging.Log;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * Base-class for facilitating reading XML documents using jdom.
 *
 * @author Trevor pering
 */
public class JdomReader {

  /**
   * {@code true} if read was successful.
   */
  protected boolean failure;

  /**
   * Interactive spaces workbench used by this reader.
   */
  private final InteractiveSpacesWorkbench workbench;

  /**
   * Create a new jdom reader.
   *
   * @param workbench
   *          containing workbench instance
   */
  public JdomReader(InteractiveSpacesWorkbench workbench) {
    this.workbench = workbench;
  }

  /**
   * Get the root element for a given input file.
   *
   * @param inputFile
   *          input project file
   *
   * @return top-level element
   */
  Element getRootElement(File inputFile) {
    Document doc;
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(inputFile);
      SAXBuilder builder = new SAXBuilder();
      doc = builder.build(inputStream);
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Exception while processing %s",
          inputFile.getAbsolutePath()), e);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
    return doc.getRootElement();
  }

  /**
   * Return the trimmed text of a child element.
   *
   * @param element
   *          container element
   * @param key
   *          variable key
   *
   * @return trimmed element text
   *
   * @throws InteractiveSpacesException
   *           if the child element is not provided
   */
  protected String getChildTextTrimmed(Element element, String key) throws InteractiveSpacesException {
    try {
      return element.getChildTextTrim(key);
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("Looking for text of child: " + key, e);
    }
  }

  /**
   * Return the trimmed text of a child element.
   *
   * @param element
   *          container element
   * @param key
   *          variable key
   * @param fallback
   *          fallback when not defined
   *
   * @return trimmed element text
   */
  protected String getChildTextTrimmed(Element element, String key, String fallback) {
    String value = getChildTextTrimmed(element, key);
    return (value == null || (!value.isEmpty() && fallback != null)) ? fallback : value;
  }

  /**
   * Return a required element attribute.
   *
   * @param element
   *          container element
   * @param key
   *          attribute key
   *
   * @return attribute value or {@code null} if none found
   */
  protected String getRequiredAttributeValue(Element element, String key) {
    String value = getAttributeValue(element, key);
    if (value == null) {
      getLog().error("Missing required attribute " + key);
    }
    return value;
  }

  /**
   * Return a given element attribute, using the default value if not found.
   *
   * @param element
   *          container element
   * @param key
   *          attribute key
   * @param fallback
   *          default attribute value
   *
   * @return attribute value
   */
  protected String getAttributeValue(Element element, String key, String fallback) {
    return element.getAttributeValue(key, fallback);
  }

  /**
   * Return a given element attribute.
   *
   * @param element
   *          container element
   * @param key
   *          attribute key
   *
   * @return attribute value, or {@code null} if not found.
   */
  protected String getAttributeValue(Element element, String key) {
    return getAttributeValue(element, key, null);
  }

  /**
   * Get the children of the given element.
   *
   * @param element
   *          element to return the children of
   *
   * @return element children
   */
  @SuppressWarnings("unchecked")
  protected List<Element> getChildren(Element element) {
    return element.getChildren();
  }

  /**
   * @return workbench used by this reader
   */
  public InteractiveSpacesWorkbench getWorkbench() {
    return workbench;
  }

  /**
   * An error has occurred.
   *
   * @param error
   *          text of the error message
   */
  protected void addError(String error) {
    getLog().error(error);
    failure = true;
  }

  /**
   * Logger to use while reading.
   *
   * @return logger
   */
  public Log getLog() {
    return workbench.getLog();
  }
}
