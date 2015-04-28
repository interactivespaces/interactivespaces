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
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.data.json.JsonMapper;
import interactivespaces.util.data.json.StandardJsonMapper;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import java.io.File;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A {@link SimpleMapPersister} which persists the data as JSON.
 *
 * @author Keith M. Hughes
 */
public class JsonSimpleMapPersister implements SimpleMapPersister {

  /**
   * The JSON mapper.
   */
  private static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * The read/write lock.
   *
   * <p>
   * This lock will be fair between reader and writer threads.
   */
  private ReadWriteLock rwlock = new ReentrantReadWriteLock(true);

  /**
   * The base directory where the map files will be stored.
   */
  private File baseMapDirectory;

  /**
   * The file support for file operations.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new persister.
   *
   * @param baseMapDirectory
   *          the base directory where the map files will be stored
   */
  public JsonSimpleMapPersister(File baseMapDirectory) {
    this.baseMapDirectory = baseMapDirectory;
    if (!fileSupport.exists(baseMapDirectory)) {
      if (!fileSupport.mkdirs(baseMapDirectory)) {
        throw new SimpleInteractiveSpacesException(String.format("Could not create directory %s", baseMapDirectory));
      }
    }
  }

  @Override
  public Map<String, Object> getMap(String name) {
    rwlock.readLock().lock();

    try {
      File mapFile = getMapFile(name);
      if (fileSupport.exists(mapFile)) {
        String content = fileSupport.readFile(mapFile);

        return MAPPER.parseObject(content);
      } else {
        return null;
      }
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not read map %s", name), e);
    } finally {
      rwlock.readLock().unlock();
    }
  }

  @Override
  public void putMap(String name, Map<String, Object> map) {
    rwlock.writeLock().lock();

    try {
      fileSupport.writeFile(getMapFile(name), MAPPER.toString(map));
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not write map %s", name), e);
    } finally {
      rwlock.writeLock().unlock();
    }
  }

  @Override
  public boolean removeMap(String name) {
    rwlock.writeLock().lock();

    try {
      File mapFile = getMapFile(name);
      if (fileSupport.exists(mapFile)) {
        fileSupport.delete(mapFile);
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not write map %s", name), e);
    } finally {
      rwlock.writeLock().unlock();
    }
  }

  /**
   * Get the file which stores the particular map file.
   *
   * @param name
   *          the name of the map
   *
   * @return the location of the map file
   */
  private File getMapFile(String name) {
    return fileSupport.newFile(baseMapDirectory, name + ".json");
  }
}
