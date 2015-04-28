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

/**
 * A builder for creating JSON objects.
 *
 * @author Keith M. Hughes
 */
public interface JsonBuilder {

  /**
   * Put in a name/value pair in an object.
   *
   * @param name
   *          name of the value
   * @param value
   *          the value
   *
   * @return this builder
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an object
   */
  JsonBuilder put(String name, Object value) throws JsonInteractiveSpacesException;

  /**
   * Put in a name/value pair in an object.
   *
   * @param data
   *          map of keys and values to add to the current object
   *
   * @return this builder
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an object
   */
  JsonBuilder putAll(Map<String, Object> data) throws JsonInteractiveSpacesException;

  /**
   * Put a value pair in an array.
   *
   * @param value
   *          the value
   *
   * @return this builder
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an array
   */
  JsonBuilder put(Object value) throws JsonInteractiveSpacesException;

  /**
   * Add a new object into the current object.
   *
   * @param name
   *          name of the new object
   *
   * @return the builder
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an object
   */
  JsonBuilder newObject(String name) throws JsonInteractiveSpacesException;

  /**
   * Add a new array into the current object.
   *
   * @param name
   *          name of the new object
   *
   * @return the builder
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an object
   */
  JsonBuilder newArray(String name) throws JsonInteractiveSpacesException;

  /**
   * Add a new array into the current array.
   *
   * @return the builder
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an array
   */
  JsonBuilder newArray() throws JsonInteractiveSpacesException;

  /**
   * Add a new array into the current array.
   *
   * @return the builder
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is not an array
   */
  JsonBuilder newObject() throws JsonInteractiveSpacesException;

  /**
   * Move up a level.
   *
   * @return this builder
   *
   * @throws JsonInteractiveSpacesException
   *           the current level is already the root
   */
  JsonBuilder up() throws JsonInteractiveSpacesException;

  /**
   * Get the final object.
   *
   * @return the fully built object.
   */
  Map<String, Object> build();
}
