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

import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A builder for creating JSON objects.
 * 
 * @author Keith M. Hughes
 */
public class JsonBuilder {

	/**
	 * The type of an object is a map.
	 */
	private static final int TYPE_OBJECT = 0;

	/**
	 * The type of an object is an array.
	 */
	private static final int TYPE_ARRAY = 1;

	/**
	 * The root object.
	 */
	private Map<String, Object> root = Maps.newHashMap();

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
	private Map<String, Object> currentObject;

	/**
	 * The current object, if it is a map.
	 */
	private List<Object> currentArray;

	public JsonBuilder() {
		currentObject = root;
		currentType = TYPE_OBJECT;
	}

	/**
	 * Put in a name/value pair in an object.
	 * 
	 * @param name
	 *            name of the value
	 * @param value
	 *            the value
	 * 
	 * @return this builder
	 */
	public JsonBuilder put(String name, Object value) {
		if (currentType == TYPE_OBJECT) {
			currentObject.put(name, value);
		} else {
			// Must be an array

			throw new InteractiveSpacesException(
					"Cannot put named item into an array");
		}

		return this;
	}

	/**
	 * Put in a name/value pair in an object.
	 * 
	 * @param data
	 *            map of keys and values to add to the current object
	 * 
	 * @return this builder
	 */
	public JsonBuilder putAll(Map<String, Object> data) {
		if (currentType == TYPE_OBJECT) {
			currentObject.putAll(data);
		} else {
			// Must be an array

			throw new InteractiveSpacesException(
					"Cannot put a map of values into an array");
		}

		return this;
	}

	/**
	 * Put a value pair in an array.
	 * 
	 * @param value
	 *            the value
	 * 
	 * @return this builder
	 */
	public JsonBuilder put(Object value) {
		if (currentType == TYPE_ARRAY) {
			currentArray.add(value);
		} else {
			// Must be an object

			throw new InteractiveSpacesException(
					"Cannot put unnamed item into an object");
		}

		return this;
	}

	/**
	 * Add a new object into the current object.
	 * 
	 * @param name
	 *            name of the newobject
	 * 
	 * @return the builder
	 */
	public JsonBuilder newObject(String name) {
		if (currentType == TYPE_OBJECT) {
			Map<String, Object> newObject = Maps.newHashMap();

			currentObject.put(name, newObject);

			nav.push(currentObject);

			currentObject = newObject;
		} else {
			// Must be an array

			throw new InteractiveSpacesException(
					"Cannot put named item into an array");
		}

		return this;
	}

	/**
	 * Add a new array into the current object.
	 * 
	 * @param name
	 *            name of the new object
	 * 
	 * @return the builder
	 */
	public JsonBuilder newArray(String name) {
		if (currentType == TYPE_OBJECT) {
			List<Object> newObject = Lists.newArrayList();

			currentObject.put(name, newObject);

			nav.push(currentObject);

			currentArray = newObject;
			currentType = TYPE_ARRAY;
		} else {
			// Must be an array

			throw new InteractiveSpacesException(
					"Cannot put named item into an array");
		}

		return this;
	}

	/**
	 * Add a new array into the current array.
	 * 
	 * @param name
	 *            name of the new object
	 * 
	 * @return the builder
	 */
	public JsonBuilder newArray() {
		if (currentType == TYPE_ARRAY) {
			List<Object> newObject = Lists.newArrayList();

			currentArray.add(newObject);

			nav.push(currentArray);

			currentArray = newObject;
		} else {
			// Must be an object

			throw new InteractiveSpacesException(
					"Cannot put unnamed item into an object");
		}

		return this;
	}

	/**
	 * Add a new array into the current array.
	 * 
	 * @param name
	 *            name of the new object
	 * 
	 * @return the builder
	 */
	public JsonBuilder newObject() {
		if (currentType == TYPE_ARRAY) {
			Map<String, Object> newObject = Maps.newHashMap();

			currentArray.add(newObject);

			nav.push(currentArray);

			currentObject = newObject;
			currentType = TYPE_OBJECT;
		} else {
			// Must be an object

			throw new InteractiveSpacesException(
					"Cannot put unnamed item into an object");
		}

		return this;
	}

	/**
	 * Move up a level.
	 * 
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	public JsonBuilder up() {
		if (!nav.isEmpty()) {
			Object newObject = nav.pop();
			
			if (newObject instanceof Map) {
				currentArray = null;
				currentObject = (Map<String, Object>)newObject;
				currentType = TYPE_OBJECT;
			} else {
				currentObject = null;
				currentArray = (List<Object>)newObject;
				currentType = TYPE_ARRAY;
			}
		} else {
			throw new InteractiveSpacesException(
					"Cannot move up in builder, nothing left");
		}

		return this;
	}

	/**
	 * Get the final object.
	 * 
	 * @return the fully built object.
	 */
	public Map<String, Object> build() {
		return root;
	}

	@Override
	public String toString() {
		return root.toString();
	}
}
