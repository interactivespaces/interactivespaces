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

package interactivespaces.util.data.json;

import interactivespaces.InteractiveSpacesException;

import java.util.Map;
import java.util.Stack;

/**
 * A navigator for JSON objects
 * 
 * @author Keith M. Hughes
 */
public class JsonNavigator {

	/**
	 * The type of an object is a map.
	 */
	private static final int TYPE_MAP = 0;
	
	/**
	 * The type of an object is an array.
	 */
	private static final int TYPE_ARRAY = 1;

	/**
	 * The root object.
	 */
	private Map<String, Object> root;

	/**
	 * A stack of objects as we walk the graph.
	 */
	private Stack<Object> nav = new Stack<Object>();

	/**
	 * Type of the current object.
	 */
	private int currentType;

	/**
	 * The current object, if it is a map.
	 */
	private Map<String, Object> currentMap;

	public JsonNavigator(Map<String, Object> root) {
		this.root = root;
		currentType = TYPE_MAP;
		currentMap = root;
	}

	/**
	 * If the current level is a map, get a string field from the map.
	 * 
	 * @param name
	 *            name of the field
	 * 
	 * @return value of the field, or {@code null} if nothing for that key
	 */
	public String getString(String name) {
		if (currentType == TYPE_MAP) {
			String value = (String) currentMap.get(name);
			return value;
		} else {
			throw new InteractiveSpacesException(String.format(
					"Current level is not a map for name %s", name));
		}
	}

	/**
	 * If the current level is a map, get an integer field from the map.
	 * 
	 * @param name
	 *            name of the field
	 * 
	 * @return value of the field, or {@code null} if nothing for that key
	 */
	public Integer getInteger(String name) {
		if (currentType == TYPE_MAP) {
			Integer value = (Integer) currentMap.get(name);
			return value;
		} else {
			throw new InteractiveSpacesException(String.format(
					"Current level is not a map for name %s", name));
		}
	}

	/**
	 * If the current level is a map, get a double field from the map.
	 * 
	 * @param name
	 *            name of the field
	 * 
	 * @return value of the field, or {@code null} if nothing for that key
	 */
	public Double getDouble(String name) {
		if (currentType == TYPE_MAP) {
			Double value = (Double) currentMap.get(name);

			return value;
		} else {
			throw new InteractiveSpacesException(String.format(
					"Current level is not a map for name %s", name));
		}
	}
	/**
	 * If the current level is a map, get a boolean field from the map.
	 * 
	 * @param name
	 *            name of the field
	 * 
	 * @return value of the field, or {@code null} if nothing for that key
	 */
	public Boolean getBoolean(String name) {
		if (currentType == TYPE_MAP) {
			Boolean value = (Boolean) currentMap.get(name);

			return value;
		} else {
			throw new InteractiveSpacesException(String.format(
					"Current level is not a map for name %s", name));
		}
	}

	/**
	 * If the current level is a map, get an object field from the map.
	 * 
	 * @param name
	 *            name of the field
	 * 
	 * @return value of the field, or {@code null} if nothing for that key
	 */
	public <T> T getObject(String name) {
		if (currentType == TYPE_MAP) {
			@SuppressWarnings("unchecked")
			T value = (T) currentMap.get(name);

			return value;
		} else {
			throw new InteractiveSpacesException(String.format(
					"Current level is not a map for name %s", name));
		}
	}

	/**
	 * Move into the object to the object at a given name.
	 * 
	 * <p>
	 * The next level must be a collection, not a primitive.
	 * 
	 * @param name
	 *            the name to move down to
	 * 
	 * @return this navigator object
	 */
	@SuppressWarnings("unchecked")
	public JsonNavigator down(String name) {
		if (currentType == TYPE_MAP) {
			Object value = currentMap.get(name);

			if (value instanceof Map) {
				nav.push(currentMap);
				currentMap = (Map<String, Object>) value;

				// Type already a MAP
			}

			return this;
		} else {
			throw new InteractiveSpacesException(String.format(
					"Current level is not a map for name %s", name));
		}
	}

	/**
	 * Move up one level in the navigation.
	 * 
	 * @return this navigator object
	 */
	@SuppressWarnings("unchecked")
	public JsonNavigator up() {
		if (!nav.isEmpty()) {
			Object value = nav.pop();

			if (value instanceof Map) {
				currentMap = (Map<String, Object>) value;

				currentType = TYPE_MAP;
			}

			return this;
		} else {
			throw new InteractiveSpacesException("Could not go up, was at root");
		}
	}
}
