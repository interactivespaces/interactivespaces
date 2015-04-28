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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Standard implementation of the JSON Builder.
 *
 * @author Keith M. Hughes
 */
public class StandardJsonBuilder implements JsonBuilder {

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
  private JsonType currentType;

  /**
   * The current object, if it is a map.
   */
  private Map<String, Object> currentObject;

  /**
   * The current object, if it is a map.
   */
  private List<Object> currentArray;

  /**
   * Construct a new builder.
   */
  public StandardJsonBuilder() {
    currentObject = root;
    currentType = JsonType.OBJECT;
  }

  @Override
  public JsonBuilder put(String name, Object value) {
    if (currentType == JsonType.OBJECT) {
      currentObject.put(name, value);
    } else {
      // Must be an array

      throw new JsonInteractiveSpacesException("Cannot put named item into an array");
    }

    return this;
  }

  @Override
  public JsonBuilder putAll(Map<String, Object> data) {
    if (currentType == JsonType.OBJECT) {
      currentObject.putAll(data);
    } else {
      // Must be an array

      throw new JsonInteractiveSpacesException("Cannot put a map of values into an array");
    }

    return this;
  }

  @Override
  public JsonBuilder put(Object value) {
    if (currentType == JsonType.ARRAY) {
      currentArray.add(value);
    } else {
      // Must be an object

      throw new JsonInteractiveSpacesException("Cannot put unnamed item into an object");
    }

    return this;
  }

  @Override
  public JsonBuilder newObject(String name) {
    if (currentType == JsonType.OBJECT) {
      Map<String, Object> newObject = Maps.newHashMap();

      currentObject.put(name, newObject);

      nav.push(currentObject);

      currentObject = newObject;
    } else {
      // Must be an array

      throw new JsonInteractiveSpacesException("Cannot put named item into an array");
    }

    return this;
  }

  @Override
  public JsonBuilder newArray(String name) {
    if (currentType == JsonType.OBJECT) {
      List<Object> newObject = Lists.newArrayList();

      currentObject.put(name, newObject);

      nav.push(currentObject);

      currentArray = newObject;
      currentType = JsonType.ARRAY;
    } else {
      // Must be an array

      throw new JsonInteractiveSpacesException("Cannot put named item into an array");
    }

    return this;
  }

  @Override
  public JsonBuilder newArray() {
    if (currentType == JsonType.ARRAY) {
      List<Object> newObject = Lists.newArrayList();

      currentArray.add(newObject);

      nav.push(currentArray);

      currentArray = newObject;
    } else {
      // Must be an object

      throw new JsonInteractiveSpacesException("Cannot put unnamed item into an object");
    }

    return this;
  }

  @Override
  public JsonBuilder newObject() {
    if (currentType == JsonType.ARRAY) {
      Map<String, Object> newObject = Maps.newHashMap();

      currentArray.add(newObject);

      nav.push(currentArray);

      currentObject = newObject;
      currentType = JsonType.OBJECT;
    } else {
      // Must be an object

      throw new JsonInteractiveSpacesException("Cannot put unnamed item into an object");
    }

    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public JsonBuilder up() {
    if (!nav.isEmpty()) {
      Object newObject = nav.pop();

      if (newObject instanceof Map) {
        currentArray = null;
        currentObject = (Map<String, Object>) newObject;
        currentType = JsonType.OBJECT;
      } else {
        currentObject = null;
        currentArray = (List<Object>) newObject;
        currentType = JsonType.ARRAY;
      }
    } else {
      throw new JsonInteractiveSpacesException("Cannot move up in builder, nothing left");
    }

    return this;
  }

  @Override
  public Map<String, Object> build() {
    return root;
  }

  @Override
  public String toString() {
    return root.toString();
  }
}
