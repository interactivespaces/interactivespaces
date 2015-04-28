/*
 * Copyright (C) 2015 Google Inc.
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
 * The standard navigator for JSON objects.
 *
 * @author Keith M. Hughes
 */
public class StandardJsonNavigator implements JsonNavigator {

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
  public StandardJsonNavigator(Map<String, Object> root) {
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
  public StandardJsonNavigator(Object root) {
    if (root instanceof Map) {
      this.root = checkedValue(root, "constructor");
      currentType = JsonType.OBJECT;
      currentObject = this.root;
    } else {
      throw new JsonInteractiveSpacesException("Non object JSON data not supported");
    }
  }

  @Override
  public Map<String, Object> getRoot() {
    return root;
  }

  @Override
  public JsonType getCurrentType() {
    return currentType;
  }

  @Override
  public String getString(String name) {
    if (currentType == JsonType.OBJECT) {
      String value = (String) currentObject.get(name);
      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a object for name %s", name));
    }
  }

  @Override
  public Integer getInteger(String name) {
    if (currentType == JsonType.OBJECT) {
      Integer value = (Integer) currentObject.get(name);
      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a object for name %s", name));
    }
  }

  @Override
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

  @Override
  public Boolean getBoolean(String name) {
    if (currentType == JsonType.OBJECT) {
      Boolean value = (Boolean) currentObject.get(name);
      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a object for name %s", name));
    }
  }

  @Override
  public Set<String> getProperties() {
    if (currentType == JsonType.OBJECT) {
      return currentObject.keySet();
    } else {
      throw new JsonInteractiveSpacesException("Current level is not a object");
    }
  }

  @Override
  public <T> T getItem(String name) {
    if (currentType == JsonType.OBJECT) {
      T value = checkedValue(currentObject.get(name), name);

      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a object for name %s", name));
    }
  }

  @Override
  public String getString(int pos) throws JsonInteractiveSpacesException {
    if (currentType == JsonType.ARRAY) {
      String value = (String) currentArray.get(pos);
      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a array for position %d", pos));
    }
  }

  @Override
  public Integer getInteger(int pos) throws JsonInteractiveSpacesException {
    if (currentType == JsonType.ARRAY) {
      Integer value = (Integer) currentArray.get(pos);
      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a array for position %d", pos));
    }
  }

  @Override
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

  @Override
  public Boolean getBoolean(int pos) throws JsonInteractiveSpacesException {
    if (currentType == JsonType.ARRAY) {
      Boolean value = (Boolean) currentArray.get(pos);

      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a array for position %d", pos));
    }
  }

  @Override
  public <T> T getItem(int pos) throws JsonInteractiveSpacesException {
    if (currentType == JsonType.ARRAY) {
      T value = checkedValue(currentArray.get(pos), "array[%s]", pos);

      return value;
    } else {
      throw new JsonInteractiveSpacesException(String.format("Current level is not a array for position %d", pos));
    }
  }

  @Override
  public int getSize() throws JsonInteractiveSpacesException {
    if (currentType == JsonType.ARRAY) {
      return currentArraySize;
    } else {
      throw new JsonInteractiveSpacesException("Current level is not array");
    }
  }

  @Override
  public Map<String, Object> getCurrentItem() throws JsonInteractiveSpacesException {
    if (currentType == JsonType.OBJECT) {
      return currentObject;
    } else {
      throw new JsonInteractiveSpacesException("Current level is not a object");
    }
  }

  @Override
  public JsonBuilder getCurrentAsJsonBuilder() {
    if (currentType == JsonType.OBJECT) {
      return new StandardJsonBuilder().putAll(currentObject);
    } else {
      throw new JsonInteractiveSpacesException("Current level is not a object");
    }
  }

  @Override
  public boolean containsProperty(String name) {
    if (currentType == JsonType.OBJECT) {
      return currentObject.containsKey(name);
    } else {
      throw new JsonInteractiveSpacesException(String.format(
          "Current level is not a object when checking for property name %s", name));
    }
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
  public Object traversePath(String path) {
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
   * @throws JsonInteractiveSpacesException
   *           if there was a typecast or other error
   */
  @SuppressWarnings("unchecked")
  private <T> T checkedValue(Object value, String format, int index) throws JsonInteractiveSpacesException {
    try {
      return (T) value;
    } catch (Exception e) {
      throw new JsonInteractiveSpacesException(String.format(format, index), e);
    }
  }
}
