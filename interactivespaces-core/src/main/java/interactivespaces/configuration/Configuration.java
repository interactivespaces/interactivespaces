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

package interactivespaces.configuration;

import java.util.Map;

/**
 * An Interactive Spaces configuration.
 * 
 * @author Keith M. Hughes
 */
public interface Configuration {

	/**
	 * True if the configuration contains the property, false otherwise.
	 * 
	 * @param property
	 *            Name of the property.
	 * 
	 * @return True if the configuration has the property, false otherwise.
	 */
	boolean containsProperty(String property);

	/**
	 * Get the value of the property as a string.
	 * 
	 * @param property
	 *            name of the property.
	 * @return The property value, or null if it wasn't found.
	 */
	String getPropertyString(String property);

	/**
	 * Get the value of the property as a string.
	 * 
	 * @param property
	 *            name of the property.
	 * @param defaultValue
	 *            Default value.
	 * @return Use the default value if the property isn't found.
	 */
	String getPropertyString(String property, String defaultValue);

	/**
	 * Evaluate a string against the configuration.
	 * 
	 * @param expression
	 *            the expression to evaluate
	 * 
	 * @return the value of the expression
	 */
	String evaluate(String expression);

	/**
	 * Get the value of the property as a string.
	 * 
	 * <p>
	 * Throws an exception if the property doesn't exist.
	 * 
	 * @param property
	 *            name of the property.
	 * @param defaultValue
	 *            Default value.
	 * @return Use the default value if the property isn't found.
	 */
	String getRequiredPropertyString(String property);

	/**
	 * Get the value of the property as an integer.
	 * 
	 * @param property
	 *            name of the property.
	 * @param defaultValue
	 *            Default value.
	 * @return Use the default value if the property isn't found.
	 */
	Integer getPropertyInteger(String property, Integer defaultValue);

	/**
	 * Get the value of the property as a long.
	 * 
	 * @param property
	 *            name of the property.
	 * @param defaultValue
	 *            Default value.
	 * @return Use the default value if the property isn't found.
	 */
	Long getPropertyLong(String property, Long defaultValue);

	/**
	 * Get the value of the property as a double.
	 * 
	 * @param property
	 *            name of the property.
	 * @param defaultValue
	 *            Default value.
	 * @return Use the default value if the property isn't found.
	 */
	Double getPropertyDouble(String property, Double defaultValue);

	/**
	 * Get the value of the property as a boolean.
	 * 
	 * @param property
	 *            name of the property.
	 * @param defaultValue
	 *            Default value.
	 * @return Use the default value if the property isn't found.
	 */
	Boolean getPropertyBoolean(String property, Boolean defaultValue);

	/**
	 * Set the value of a property.
	 * 
	 * @param property
	 * @param value
	 */
	void setValue(String property, String value);

	/**
	 * Set the parent of this configuration.
	 * 
	 * @param parent
	 */
	void setParent(Configuration parent);

	/**
	 * Get the parent of this configuration.
	 * 
	 * @return parent
	 */
	Configuration getParent();

	/**
	 * Get the property from the current configuation.
	 * 
	 * <p>
	 * This method does not go up the parent chain.
	 * 
	 * @param property
	 *            Name of the property.
	 * 
	 * @return The value of the property, or null if not found.
	 */
	String findValueLocally(String property);

	/**
	 * See if the current configuration contains the given property.
	 * 
	 * <p>
	 * This method does not go up the parent chain.
	 * 
	 * @param property
	 *            Name of the property.
	 * 
	 * @return True if the current configuration contains the property, false
	 *         otherwise.
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
	 * @return
	 */
	Map<String, String> getCollapsedMap();

	/**
	 * Add all entries for the parents then the local config.
	 * 
	 * @param map
	 *            the map to add things into
	 */
	void addEntries(Map<String, String> map);
}
