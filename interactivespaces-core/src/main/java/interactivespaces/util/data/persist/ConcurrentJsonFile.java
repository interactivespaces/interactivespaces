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

package interactivespaces.util.data.persist;

import interactivespaces.InteractiveSpacesException;

import java.util.Map;

/**
 * A simple JSON file which stores only 1 map.
 *
 * <p>
 * This is a simpler interface than the {@link SimpleMapPersister} if you have only 1 may to store.
 *
 * <p>
 * This has fair locking. It allows multiple readers and a single writer.
 *
 * @author Keith M. Hughes
 */
public interface ConcurrentJsonFile {

  /**
   * Read the map.
   *
   * @return {@code true} if the map existed and was properly read, {@code false} if the file idn't exist.
   *
   * @throws InteractiveSpacesException
   *           if there was an error while reading the file
   */
  boolean load() throws InteractiveSpacesException;

  /**
   * Get a value from the map.
   *
   * @param key
   *          key for the required value
   *
   * @return the stored value, or {@code null} if there is no entry for the given key
   */
  Object get(String key);

  /**
   * Get all values from the map.
   *
   * @return the entire map
   */
  Map<String, Object> getAll();

  /**
   * Save the map to the file.
   *
   * @throws InteractiveSpacesException
   *           if there was an error while writing the file
   */
  void save() throws InteractiveSpacesException;

  /**
   * Replace all contents of the map.
   *
   * @param newData
   *          a map of the new data
   */
  void replaceAll(Map<String, Object> newData);

  /**
   * Put a new value in the map.
   *
   * @param key
   *          the key of the value
   * @param value
   *          the value to associate with the key
   */
  void put(String key, Object value);

  /**
   * Put a collection of new values into the map.
   *
   * @param values
   *          the values to add
   */
  void putAll(Map<String, Object> values);
}
