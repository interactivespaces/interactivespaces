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

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * A navigator for JSON objects
 * 
 * @author Keith M. Hughes
 */
public class JsonNavigator {

	public enum JsonType {
		OBJECT, ARRAY;
	}

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
	private JsonType currentType;

	/**
	 * The current object, if it is a object.
	 */
	private Map<String, Object> currentObject;

	/**
	 * The current list, if it is a list.
	 */
	private List<Object> currentArray;

	public JsonNavigator(Map<String, Object> root) {
		this.root = root;
		currentType = JsonType.OBJECT;
		currentObject = root;
	}

	@SuppressWarnings("unchecked")
	public JsonNavigator(Object root) {
		if (root instanceof Map) {
			this.root = (Map<String, Object>) root;
			currentType = JsonType.OBJECT;
			currentObject = this.root;
		} else {
			throw new JsonInteractiveSpacesException(
					"Non object JSON data not supported");
		}
	}

	/**
	 * Get the current type of the current navigation point.
	 * 
	 * @return
	 */
	public JsonType getCurrentType() {
		return currentType;
	}

	/**
	 * If the current level is a object, get a string field from the object.
	 * 
	 * @param name
	 *            name of the field
	 * 
	 * @return value of the field, or {@code null} if nothing for that key
	 */
	public String getString(String name) {
		if (currentType == JsonType.OBJECT) {
			String value = (String) currentObject.get(name);
			return value;
		} else {
			throw new JsonInteractiveSpacesException(String.format(
					"Current level is not a object for name %s", name));
		}
	}

	/**
	 * If the current level is a object, get an integer field from the object.
	 * 
	 * @param name
	 *            name of the field
	 * 
	 * @return value of the field, or {@code null} if nothing for that key
	 */
	public Integer getInteger(String name) {
		if (currentType == JsonType.OBJECT) {
			Integer value = (Integer) currentObject.get(name);
			return value;
		} else {
			throw new JsonInteractiveSpacesException(String.format(
					"Current level is not a object for name %s", name));
		}
	}

	/**
	 * If the current level is a object, get a double field from the object.
	 * 
	 * @param name
	 *            name of the field
	 * 
	 * @return value of the field, or {@code null} if nothing for that key
	 */
	public Double getDouble(String name) {
		if (currentType == JsonType.OBJECT) {
			Double value = (Double) currentObject.get(name);

			return value;
		} else {
			throw new JsonInteractiveSpacesException(String.format(
					"Current level is not a object for name %s", name));
		}
	}

	/**
	 * If the current level is a object, get a boolean field from the object.
	 * 
	 * @param name
	 *            name of the field
	 * 
	 * @return value of the field, or {@code null} if nothing for that key
	 */
	public Boolean getBoolean(String name) {
		if (currentType == JsonType.OBJECT) {
			Boolean value = (Boolean) currentObject.get(name);

			return value;
		} else {
			throw new JsonInteractiveSpacesException(String.format(
					"Current level is not a object for name %s", name));
		}
	}

	/**
	 * If the current level is a object, get an object field from the object.
	 * 
	 * @param name
	 *            name of the field
	 * 
	 * @return value of the field, or {@code null} if nothing for that key
	 */
	public <T> T getItem(String name) {
		if (currentType == JsonType.OBJECT) {
			@SuppressWarnings("unchecked")
			T value = (T) currentObject.get(name);

			return value;
		} else {
			throw new JsonInteractiveSpacesException(String.format(
					"Current level is not a object for name %s", name));
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
		if (currentType == JsonType.OBJECT) {
			Object value = currentObject.get(name);

			if (value instanceof Map) {
				nav.push(currentObject);
				currentObject = (Map<String, Object>) value;

				// Type already a MAP
			} else if (value instanceof List) {
				nav.push(currentObject);
				currentArray = (List<Object>) value;
				currentType = JsonType.ARRAY;
			} else {
				throw new JsonInteractiveSpacesException(String.format(
						"The named item %s is neither an object or an array",
						name));
			}

			return this;
		} else {
			throw new JsonInteractiveSpacesException(String.format(
					"Current level is not a object for name %s", name));
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
				currentObject = (Map<String, Object>) value;
				currentArray = null;

				currentType = JsonType.OBJECT;
			} else if (value instanceof List) {
				currentArray = (List<Object>) value;
				currentObject = null;

				currentType = JsonType.ARRAY;
			}

			return this;
		} else {
			throw new JsonInteractiveSpacesException(
					"Could not go up, was at root");
		}
	}

	@SuppressWarnings("unchecked")
	Object traversePath(String path) {
		Object curObject = null;

		if (currentType == JsonType.OBJECT) {
			curObject = currentObject;
		} else {
			curObject = currentArray;
		}

		String[] elements = path.split("\\.");

		for (int i = 0; i < elements.length; i++) {
			String element = elements[i].trim();

			if (element.isEmpty()) {
				throw new JsonInteractiveSpacesException(String.format(
						"Empty element in path %s", path));
			}

			if (element.equals("$")) {
				curObject = root;
			} else if (element.startsWith("[")) {
				if (curObject instanceof List) {
					if (element.endsWith("]")) {
						int index = Integer.parseInt(element.substring(1,
								element.length() - 1));

						curObject = ((List<Object>) curObject).get(index);
					} else {
						throw new JsonInteractiveSpacesException(String.format(
								"Path element %s does not end in a ]", element));
					}
				} else if (curObject instanceof Map) {
					throw new JsonInteractiveSpacesException(
							"Attempt to use an array index in an object");
				} else if (i < elements.length) {
					throw new JsonInteractiveSpacesException(
							"Non array or object in the middle of a path");
				}
			} else {
				// Have a result name
				if (curObject instanceof Map) {
					curObject = ((Map<String, Object>) curObject).get(element);
				} else if (curObject instanceof List) {
					throw new JsonInteractiveSpacesException(
							"Attempt to use an name index in an array");
				} else if (i < elements.length) {
					throw new JsonInteractiveSpacesException(
							"Non array or object in the middle of a path");
				}
			}
		}

		return curObject;
	}
}
