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

import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.util.data.json.JsonMapper;
import interactivespaces.util.io.Files;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A simple JSON file which stores only 1 map.
 *
 * <p>
 * This is a simpler interface than the {@link SimpleMapPersister} if you have
 * only 1 may to store.
 *
 * <p>
 * This has fair locking. It allows multiple readers and a single writer.
 *
 * @author Keith M. Hughes
 */
public class ConcurrentJsonFile {

  /**
   * The JSON mapper.
   */
  private static final JsonMapper MAPPER;

  static {
    MAPPER = new JsonMapper();
  }

  /**
   * The file which stores the JSON.
   */
  private File file;

  /**
   * The read/write lock.
   *
   * <p>
   * This lock will be fair between reader and writer threads.
   */
  private ReadWriteLock rwlock = new ReentrantReadWriteLock(true);

  /**
   * The map.
   */
  private Map<String, Object> map = Maps.newHashMap();

  public ConcurrentJsonFile(File file) {
    this.file = file;
  }

  /**
   * Read the map.
   *
   * @return {@code true} if the map existed and was properly read,
   *         {@code false} if the file idn't exist.
   *
   * @throws InteractiveSpacesException
   *           if there was an error while reading the file
   */
  public boolean load() {
    rwlock.readLock().lock();
    try {
      if (file.exists()) {
        String value = Files.readFile(file);
        map = MAPPER.parseObject(value);
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not read %s", file), e);
    } finally {
      rwlock.readLock().unlock();
    }
  }

  /**
   * Get a value from the map.
   *
   * @param key
   *          key for the required value
   *
   * @return the stored value, or {@code null} if there is no entry for the
   *         given key
   */
  public Object get(String key) {
    rwlock.readLock().lock();
    try {
      return map.get(key);
    } finally {
      rwlock.readLock().unlock();
    }
  }

  /**
   * Get all values from the map.
   *
   * @return the entire map
   */
  public Map<String, Object> getAll() {
    rwlock.readLock().lock();
    try {
      return Maps.newHashMap(map);
    } finally {
      rwlock.readLock().unlock();
    }
  }

  /**
   * Save the map to the file.
   *
   * @throws InteractiveSpacesException
   *           if there was an error while writing the file
   */
  public void save() {
    rwlock.writeLock().lock();
    try {
      Files.writeFile(file, MAPPER.toString(map));
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not read %s", file), e);
    } finally {
      rwlock.writeLock().unlock();
    }
  }

  /**
   * Replace all contents of the map.
   *
   * @param newData
   *          a map of the new data
   */
  public void replaceAll(Map<String, Object> newData) {
    rwlock.writeLock().lock();
    try {
      map.clear();
      map.putAll(newData);
    } finally {
      rwlock.writeLock().unlock();
    }
  }

  /**
   * Put a new value in the map.
   *
   * @param key
   *          the key of the value
   * @param value
   *          the value to associate with the key
   */
  public void put(String key, Object value) {
    rwlock.writeLock().lock();
    try {
      map.put(key, value);
    } finally {
      rwlock.writeLock().unlock();
    }
  }

  /**
   * Put a collection of new values into the map.
   *
   * @param values
   *          the values to add
   */
  public void putAll(Map<String, Object> values) {
    rwlock.writeLock().lock();
    try {
      for (Entry<String, Object> entry : values.entrySet()) {
        map.put(entry.getKey(), entry.getValue());
      }
    } finally {
      rwlock.writeLock().unlock();
    }
  }
}
