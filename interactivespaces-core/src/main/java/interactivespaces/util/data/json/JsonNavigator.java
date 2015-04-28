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

import java.util.Map;
import java.util.Set;

/**
 * A navigator for JSON objects.
 *
 * @author Keith M. Hughes
 */
public interface JsonNavigator {

  /**
   * Get the root object of the navigator.
   *
   * @return the root object
   */
  Map<String, Object> getRoot();

  /**
   * Get the current type of the current navigation point.
   *
   * @return the current type
   */
  JsonType getCurrentType();

  /**
   * If the current level is a object, get a string field from the object.
   *
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an object
   */
  String getString(String name) throws JsonInteractiveSpacesException;

  /**
   * If the current level is a object, get an integer field from the object.
   *
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an object
   */
  Integer getInteger(String name) throws JsonInteractiveSpacesException;

  /**
   * If the current level is a object, get a double field from the object.
   *
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an object
   */
  Double getDouble(String name) throws JsonInteractiveSpacesException;

  /**
   * If the current level is a object, get a boolean field from the object.
   *
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an object
   */
  Boolean getBoolean(String name) throws JsonInteractiveSpacesException;

  /**
   * If the current level is a object, get the names of all properties for that object.
   *
   * @return names of all properties for the object
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an object
   */
  Set<String> getProperties() throws JsonInteractiveSpacesException;

  /**
   * If the current level is a object, get an object field from the object.
   *
   * @param <T>
   *          expected type of the result
   * @param name
   *          name of the field
   *
   * @return value of the field, or {@code null} if nothing for that key
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an object
   */
  <T> T getItem(String name) throws JsonInteractiveSpacesException;

  /**
   * If the current level is a object, get a string field from the object.
   *
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an array
   */
  String getString(int pos) throws JsonInteractiveSpacesException;

  /**
   * If the current level is a object, get an integer field from the object.
   *
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an array
   */
  Integer getInteger(int pos) throws JsonInteractiveSpacesException;

  /**
   * If the current level is a object, get a double field from the object.
   *
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an array
   */
  Double getDouble(int pos) throws JsonInteractiveSpacesException;

  /**
   * If the current level is a object, get a boolean field from the object.
   *
   * @param pos
   *          position in the array
   *
   * @return value of the field
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an array
   */
  Boolean getBoolean(int pos) throws JsonInteractiveSpacesException;

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
   *           the current level is not an array
   */
  <T> T getItem(int pos) throws JsonInteractiveSpacesException;

  /**
   * If the current level is an array, get the size of the array.
   *
   * @return size of the current array
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an array
   */
  int getSize() throws JsonInteractiveSpacesException;

  /**
   * If the current level is a object, get that object.
   *
   * @return the current object
   *
   * @throws JsonInteractiveSpacesException
   *           the current level was not an object
   */
  Map<String, Object> getCurrentItem() throws JsonInteractiveSpacesException;

  /**
   * If the current level is a object, get an object field from the object.
   *
   * @return the JsonBuilder for the current level
   *
   * @throws JsonInteractiveSpacesException
   *           the current level was not an object
   */
  JsonBuilder getCurrentAsJsonBuilder() throws JsonInteractiveSpacesException;

  /**
   * Does the current object contain a property with the given name?
   *
   * @param name
   *          the name of the property to check for
   *
   * @return {@code true} if the property exists
   *
   * @throws JsonInteractiveSpacesException
   *           the current level was not an object
   */
  boolean containsProperty(String name) throws JsonInteractiveSpacesException;

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
   *
   * @throws JsonInteractiveSpacesException
   *           the level moved to is neither an object or an array, or the current level is not an object
   */
  JsonNavigator down(String name) throws JsonInteractiveSpacesException;

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
   *           the level moved to is neither an object or an array, or the current level is not an array
   */
  JsonNavigator down(int pos) throws JsonInteractiveSpacesException;

  /**
   * Move up one level in the navigation.
   *
   * @return this navigator object
   *
   * @throws JsonInteractiveSpacesException
   *           the navigator was at the root level
   */
  JsonNavigator up() throws JsonInteractiveSpacesException;

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
   *
   * @throws JsonInteractiveSpacesException
   *           could not navigate
   */
  Object traversePath(String path) throws JsonInteractiveSpacesException;
}
