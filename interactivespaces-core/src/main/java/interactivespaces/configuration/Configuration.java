/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package interactivespaces.configuration;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.evaluation.ExpressionEvaluator;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An Interactive Spaces configuration.
 *
 * @author Keith M. Hughes
 */
public interface Configuration {

  /**
   * Separator between component names for a configuration property name.
   */
  String CONFIGURATION_NAME_SEPARATOR = ".";

  /**
   * True if the configuration contains the property, false otherwise.
   *
   * @param property
   *          Name of the property.
   *
   * @return True if the configuration has the property, false otherwise.
   */
  boolean containsProperty(String property);

  /**
   * Get the value of the property as a string.
   *
   * @param property
   *          name of the property
   *
   * @return The property value, or {@code null} if it wasn't found.
   */
  String getPropertyString(String property);

  /**
   * Get the value of the property as a string.
   *
   * @param property
   *          name of the property.
   * @param defaultValue
   *          Default value.
   * @return Use the default value if the property isn't found.
   */
  String getPropertyString(String property, String defaultValue);

  /**
   * Evaluate a string against the configuration.
   *
   * @param expression
   *          the expression to evaluate
   *
   * @return the value of the expression
   */
  String evaluate(String expression);

  /**
   * Return the expression evaluator used by this configuration.
   *
   * @return this configurations expression evaluator
   */
  ExpressionEvaluator getExpressionEvaluator();

  /**
   * Get the value of the property as a string.
   *
   * <p>
   * Throws an exception if the property doesn't exist.
   *
   * @param property
   *          name of the property.
   * @return Use the default value if the property isn't found.
   */
  String getRequiredPropertyString(String property);

  /**
   * Get the value of the property as an integer.
   *
   * @param property
   *          name of the property.
   * @param defaultValue
   *          Default value.
   * @return Use the default value if the property isn't found.
   */
  Integer getPropertyInteger(String property, Integer defaultValue);

  /**
   * Get the value of the property as an integer.
   *
   * @param property
   *          name of the property
   *
   * @return the value of the required property
   *
   * @throws InteractiveSpacesException
   *           if the property does not exist
   */
  Integer getRequiredPropertyInteger(String property) throws InteractiveSpacesException;

  /**
   * Get the value of the property as a long.
   *
   * @param property
   *          name of the property
   * @param defaultValue
   *          default value
   *
   * @return Use the default value if the property isn't found.
   */
  Long getPropertyLong(String property, Long defaultValue);

  /**
   * Get the value of the property as a long.
   *
   * @param property
   *          name of the property
   *
   * @return the value of the required property
   *
   * @throws InteractiveSpacesException
   *           if the property does not exist
   */
  Long getRequiredPropertyLong(String property) throws InteractiveSpacesException;

  /**
   * Get the value of the property as a double.
   *
   * @param property
   *          name of the property.
   * @param defaultValue
   *          Default value.
   * @return Use the default value if the property isn't found.
   */
  Double getPropertyDouble(String property, Double defaultValue);

  /**
   * Get the value of the property as a double.
   *
   * @param property
   *          name of the property
   *
   * @return the value of the required property
   *
   * @throws InteractiveSpacesException
   *           if the property does not exist
   */
  Double getRequiredPropertyDouble(String property) throws InteractiveSpacesException;

  /**
   * Get the value of the property as a boolean.
   *
   * @param property
   *          name of the property.
   * @param defaultValue
   *          Default value.
   * @return Use the default value if the property isn't found.
   */
  Boolean getPropertyBoolean(String property, Boolean defaultValue);

  /**
   * Get the value of the property as a boolean.
   *
   * @param property
   *          name of the property
   *
   * @return the value of the required property
   *
   * @throws InteractiveSpacesException
   *           if the property does not exist
   */
  Boolean getRequiredPropertyBoolean(String property) throws InteractiveSpacesException;

  /**
   * Get the value of the property as a list of strings seperated using the
   * given string as a delineator.
   *
   * @param property
   *          name of the property
   * @param delineator
   *          the string to use to separate the property value into a list of
   *          strings
   *
   * @return the list of values or {@code null} if no such property.
   *
   */
  List<String> getPropertyStringList(String property, String delineator);

  /**
   * Get the value of the property as a set of strings. Similar to
   * getPropertyStringList but order is not guaranteed and duplicates are not
   * allowed.
   *
   * @param property
   *          name of the property
   * @param delineator
   *          the string to use to separate the property value into seperate
   *          strings
   *
   * @return the set of distinct values or {@code null} if no such property.
   */
  Set<String> getPropertyStringSet(String property, String delineator);

  /**
   * Set the value of a property.
   *
   * @param property
   *          name of the property
   * @param value
   *          value of the property
   */
  void setValue(String property, String value);

  /**
   * Copy the key/value pairs from the map into the configuration.
   *
   * @param values
   *          the values to add
   */
  void setValues(Map<String, String> values);

  /**
   * Set the parent of this configuration.
   *
   * @param parent
   *          the parent of the configuration
   */
  void setParent(Configuration parent);

  /**
   * Get the parent of this configuration.
   *
   * @return the parent, or {@code null} if none
   */
  Configuration getParent();

  /**
   * Get the property from the current configuration.
   *
   * <p>
   * This method does not go up the parent chain.
   *
   * @param property
   *          name of the property
   *
   * @return The value of the property, or {@code null} if not found.
   */
  String findValueLocally(String property);

  /**
   * See if the current configuration contains the given property.
   *
   * <p>
   * This method does not go up the parent chain.
   *
   * @param property
   *          name of the property
   *
   * @return {@code true} if the current configuration contains the property
   */
  boolean containsPropertyLocally(String property);

  /**
   * Clear out the properties at this layer of the configuration.
   *
   * <p>
   * Any parents or children are left alone.
   */
  void clear();

  /**
   * Get a collapsed map of all configurations in the entire chain.
   *
   * <p>
   * Entries in children configs will properly shadow the same keys in the
   * parents.
   *
   * @return collapsed map of all entries for this configuration
   */
  Map<String, String> getCollapsedMap();

  /**
   * Add all entries for the parents then the local config.
   *
   * @param map
   *          the map to add things into
   */
  void addCollapsedEntries(Map<String, String> map);
}
