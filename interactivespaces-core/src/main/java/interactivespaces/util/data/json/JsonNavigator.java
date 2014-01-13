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

import interactivespaces.SimpleInteractiveSpacesException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * A navigator for JSON objects.
 *
 * @author Keith M. Hughes
 */
public class JsonNavigator {

  /**
   * The type of data context currently being examined.
   */
  public enum JsonType {
    /**
     * The current type is an object.
     */
    OBJECT,

    /**
     * The current type is an array.
     */
    ARRAY;
  }

  /**
   * The root object.
   */
  private Map<String, Object> root;

  /**
   * A stack of objects as we walk the graph.
   */
  private final Stack<Object> nav = new Stack<Object>();

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

  /**
   * Current position in array if in an array.
   */
  private int currentArrayPosition;

  /**
   * Current size of array if in an array.
   */
  private int currentArraySize;

  /**
   * Construct a navigator with a Map.
   *
   * @param root
   *          the root map
   */
  public JsonNavigator(Map<String, Object> root) {
    this.root = root;
    currentType = JsonType.OBJECT;
    currentObject = root;
  }

  /**
   * Construct a navigator.
   *
   * @param root
   *          the root object, must be a map
   */
  public JsonNavigator(Object root) {
    if (root instanceof Map) {
      this.root = checkedValue(root, "constructor");
      currentType = JsonType.OBJECT;
      currentObject = this.root;
    } else {
      throw new JsonInteractiveSpacesException("Non object JSON data not supported");
    }
  }

  /**
   * Get the root object of the navigator.
   *
   * @return the root object
   */
  public Map<String, Object> getRoot() {
    return root;
  }

  /**
   * Get the current type of the current navigation point.
   *
   * @return the current type
   */
  public JsonType getCurrentType() {
    return currentType;
  }

  /**
   * If the current level is a object, get a string field from the object.
   *
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   */
  public String getString(String name) {
    if (currentType == JsonType.OBJECT) {
      String value = (String) currentObject.get(name);
      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a object for name %s", name));
    }
  }

  /**
   * If the current level is a object, get an integer field from the object.
   *
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   */
  public Integer getInteger(String name) {
    if (currentType == JsonType.OBJECT) {
      Integer value = (Integer) currentObject.get(name);
      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a object for name %s", name));
    }
  }

  /**
   * If the current level is a object, get a double field from the object.
   *
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   */
  public Double getDouble(String name) {
    if (currentType == JsonType.OBJECT) {
      Object value = currentObject.get(name);
      if (Double.class.isInstance(value)) {
        return (Double) value;
      } else if (Integer.class.isInstance(value)) {

        return ((Integer) value).doubleValue();
      } else if (value == null) {
        return null;
      } else {
        throw new JsonInteractiveSpacesException(String.format("Object field %s is not numeric", name));
      }
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a object for name %s", name));
    }
  }

  /**
   * If the current level is a object, get a boolean field from the object.
   *
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   */
  public Boolean getBoolean(String name) {
    if (currentType == JsonType.OBJECT) {
      Boolean value = (Boolean) currentObject.get(name);
      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a object for name %s", name));
    }
  }

  /**
   * If the current level is a object, get the names of all properties for that
   * object.
   *
   * @return names of all properties for the object
   */
  public Set<String> getProperties() {
    if (currentType == JsonType.OBJECT) {
      return currentObject.keySet();
    } else {
      throw new JsonInteractiveSpacesException("Current level is not a object");
    }
  }

  /**
   * If the current level is a object, get an object field from the object.
   *
   * @param <T>
   *          expected type of the result
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   */
  public <T> T getItem(String name) {
    if (currentType == JsonType.OBJECT) {
      T value = checkedValue(currentObject.get(name), name);

      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a object for name %s", name));
    }
  }

  /**
   * If the current level is a object, get a string field from the object.
   *
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws JsonInteractiveSpacesException
   *           not an array
   */
  public String getString(int pos) throws JsonInteractiveSpacesException {
    if (currentType == JsonType.ARRAY) {
      String value = (String) currentArray.get(pos);
      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a array for position %d", pos));
    }
  }

  /**
   * If the current level is a object, get an integer field from the object.
   *
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws JsonInteractiveSpacesException
   *           not an array
   */
  public Integer getInteger(int pos) throws JsonInteractiveSpacesException {
    if (currentType == JsonType.ARRAY) {
      Integer value = (Integer) currentArray.get(pos);
      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a array for position %d", pos));
    }
  }

  /**
   * If the current level is a object, get a double field from the object.
   *
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws JsonInteractiveSpacesException
   *           not an array
   */
  public Double getDouble(int pos) throws JsonInteractiveSpacesException {
    if (currentType == JsonType.ARRAY) {
      Object value = currentArray.get(pos);
      if (Double.class.isInstance(value)) {
        return (Double) value;
      } else if (Integer.class.isInstance(value)) {

        return ((Integer) value).doubleValue();
      } else if (value == null) {
        return null;
      } else {
        throw new JsonInteractiveSpacesException(String.format("Array field %d is not numeric", pos));
      }
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a array for position %d", pos));
    }
  }

  /**
   * If the current level is a object, get a boolean field from the object.
   *
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws JsonInteractiveSpacesException
   *           not an array
   */
  public Boolean getBoolean(int pos) throws JsonInteractiveSpacesException {
    if (currentType == JsonType.ARRAY) {
      Boolean value = (Boolean) currentArray.get(pos);

      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a array for position %d", pos));
    }
  }

  /**
   * If the current level is a object, get an object field from the object.
   *
   * @param <T>
   *          the expected type of the result
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws JsonInteractiveSpacesException
   *           not an array
   */
  public <T> T getItem(int pos) throws JsonInteractiveSpacesException {
    if (currentType == JsonType.ARRAY) {
      T value = checkedValue(currentArray.get(pos), "array[%s]", pos);

      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a array for position %d", pos));
    }
  }

  /**
   * If the current level is an array, get the size of the array.
   *
   * @return size of the current array
   *
   * @throws JsonInteractiveSpacesException
   *           not an array
   */
  public int getSize() throws JsonInteractiveSpacesException {
    if (currentType == JsonType.ARRAY) {
      return currentArraySize;
    } else {
      throw new JsonInteractiveSpacesException("Current level is not array");
    }
  }

  /**
   * If the current level is a object, get that object.
   *
   * @return the current object
   *
   * @throws JsonInteractiveSpacesException
   *           the current level was not an object
   */
  public Map<String, Object> getCurrentItem() throws JsonInteractiveSpacesException {
    if (currentType == JsonType.OBJECT) {
      return currentObject;
    } else {
      throw new JsonInteractiveSpacesException("Current level is not a object");
    }
  }

  /**
   * If the current level is a object, get an object field from the object.
   *
   * @return the JsonBuilder for the current level
   */
  public JsonBuilder getCurrentAsJsonBuilder() {
    if (currentType == JsonType.OBJECT) {
      return new JsonBuilder().putAll(currentObject);
    } else {
      throw new JsonInteractiveSpacesException("Current level is not a object");
    }
  }

  /**
   * Does the current object contain a property with the given name?
   *
   * @param name
   *          the name of the property to check for
   *
   * @return {@code true} if the property exists
   */
  public boolean containsProperty(String name) {
    if (currentType == JsonType.OBJECT) {
      return currentObject.containsKey(name);
    } else {
      throw new JsonInteractiveSpacesException(String.format(
          "Current level is not a object when checking for property name %s", name));
    }
  }

  /**
   * Move into the object to the object at a given name.
   *
   * <p>
   * The next level must be a collection, not a primitive.
   *
   * @param name
   *          the name to move down to
   *
   * @return this navigator object
   */
  public JsonNavigator down(String name) {
    if (currentType == JsonType.OBJECT) {
      Object value = currentObject.get(name);

      if (value instanceof Map) {
        nav.push(currentObject);
        currentObject = checkedValue(value, name);

        // Type already a MAP
      } else if (value instanceof List) {
        nav.push(currentObject);
        currentArray = checkedValue(value, name);
        currentType = JsonType.ARRAY;
        currentArraySize = currentArray.size();
        currentArrayPosition = 0;
      } else {
        throw new JsonInteractiveSpacesException(String.format("The named item %s is neither an object or an array",
            name));
      }

      return this;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a object for name %s", name));
    }
  }

  /**
   * Move into the array to the item at a given position.
   *
   * <p>
   * The next level must be a collection, not a primitive.
   *
   * @param pos
   *          the position in the array
   *
   * @return this navigator object
   *
   * @throws JsonInteractiveSpacesException
   *           not an array
   */
  public JsonNavigator down(int pos) throws JsonInteractiveSpacesException {
    if (currentType == JsonType.ARRAY) {
      Object value = currentArray.get(pos);

      if (value instanceof Map) {
        nav.push(currentArray);
        currentObject = checkedValue(value, "down[%s]", pos);

        currentType = JsonType.OBJECT;
      } else if (value instanceof List) {
        nav.push(currentArray);
        currentArray = checkedValue(value, "down[%s]", pos);
        // Already an array
        currentArraySize = currentArray.size();
        currentArrayPosition = 0;
      } else {
        throw new JsonInteractiveSpacesException(
            String.format("The posiiton %d is neither an object or an array", pos));
      }

      return this;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not an array for position %d", pos));
    }
  }

  /**
   * Move up one level in the navigation.
   *
   * @return this navigator object
   */
  public JsonNavigator up() {
    if (!nav.isEmpty()) {
      Object value = nav.pop();

      if (value instanceof Map) {
        currentObject = checkedValue(value, "parent object");
        currentArray = null;

        currentType = JsonType.OBJECT;
      } else if (value instanceof List) {
        currentArray = checkedValue(value, "parent object");
        currentObject = null;

        currentType = JsonType.ARRAY;
      }

      return this;
    } else {
      throw new JsonInteractiveSpacesException("Could not go up, was at root");
    }
  }

  /**
   * Traverse a string-based path to a position in the object.
   *
   * <p>
   * Not quite ready for prime time.
   *
   * @param path
   *          the path string
   *
   * @return the final object
   */
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
        throw new JsonInteractiveSpacesException(String.format("Empty element in path %s", path));
      }

      if (element.equals("$")) {
        curObject = root;
      } else if (element.startsWith("[")) {
        if (curObject instanceof List) {
          if (element.endsWith("]")) {
            int index = Integer.parseInt(element.substring(1, element.length() - 1));

            List<Object> objectList = checkedValue(curObject, path);
            curObject = objectList.get(index);
          } else {
            throw new JsonInteractiveSpacesException(String.format("Path element %s does not end in a ]", element));
          }
        } else if (curObject instanceof Map) {
          throw new JsonInteractiveSpacesException("Attempt to use an array index in an object");
        } else if (i < elements.length) {
          throw new JsonInteractiveSpacesException("Non array or object in the middle of a path");
        }
      } else {
        // Have a result name
        if (curObject instanceof Map) {
          Map<String, Object> objectMap = checkedValue(curObject, path);
          curObject = objectMap.get(element);
        } else if (curObject instanceof List) {
          throw new JsonInteractiveSpacesException("Attempt to use an name index in an array");
        } else if (i < elements.length) {
          throw new JsonInteractiveSpacesException("Non array or object in the middle of a path");
        }
      }
    }

    return curObject;
  }

  @Override
  public String toString() {
    return "JsonNavigator [root=" + root + "]";
  }

  /**
   * Cast the value to the appropriate type, providing a reasonable error.
   *
   * @param value
   *          value to cast
   * @param name
   *          name of property
   * @param <T>
   *          intended type
   *
   * @return value cast to intended type
   *
   * @throws SimpleInteractiveSpacesException
   *           if there was a typecast or other error
   */
  private <T> T checkedValue(Object value, String name) throws SimpleInteractiveSpacesException {
    return checkedValue(value, name, 0);
  }

  /**
   * Cast the value to the appropriate type, providing a reasonable error.
   *
   * @param value
   *          value to cast
   * @param format
   *          string format for error reporting
   * @param index
   *          index to be applied to the format
   * @param <T>
   *          intended type
   *
   * @return value cast to intended type
   *
   * @throws SimpleInteractiveSpacesException
   *           if there was a typecast or other error
   */
  @SuppressWarnings("unchecked")
  private <T> T checkedValue(Object value, String format, int index) throws SimpleInteractiveSpacesException {
    try {
      return (T) value;
    } catch (Exception e) {
      String message = String.format(format, index);
      throw new SimpleInteractiveSpacesException(message, e);
    }
  }
}
