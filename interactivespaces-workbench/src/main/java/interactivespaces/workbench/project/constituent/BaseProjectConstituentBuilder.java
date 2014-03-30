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

package interactivespaces.workbench.project.constituent;

import org.apache.commons.logging.Log;
import org.jdom.Element;

/**
 * Base implementation of a project constituent builder.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseProjectConstituentBuilder implements ProjectConstituent.ProjectConstituentBuilder {

  /**
   * The logger for this builder.
   */
  private Log log;

  /**
   * {@code true} if the builder had errors.
   */
  private boolean errors;

  /**
   * Convenience function to help more easily build constituent parts.
   *
   * @param resourceElement
   *          input element
   * @param propertyName
   *          property name to extract
   * @param fallback
   *          fallback value to use if none supplied
   *
   * @return property value, else fallback
   */
  public static String getChildTextNormalize(Element resourceElement, String propertyName, String fallback) {
    String value = resourceElement.getChildTextNormalize(propertyName);
    return (value == null || (value.isEmpty() && fallback != null)) ? fallback : value;
  }

  /**
   * Add an error to the builder.
   *
   * @param message
   *          the error message
   */
  protected void addError(String message) {
    log.error(message);
    errors = true;
  }

  /**
   * Add a warn to the builder.
   *
   * @param message
   *          the warn message
   */
  protected void addWarn(String message) {
    log.warn(message);
  }

  /**
   * Were there errors?
   *
   * @return {@code true} if there were errors
   */
  protected boolean hasErrors() {
    return errors;
  }

  /**
   * Set the logging provider.
   *
   * @param log
   *          logging provider
   */
  public void setLog(Log log) {
    this.log = log;
  }
}
