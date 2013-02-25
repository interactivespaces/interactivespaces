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
import interactivespaces.util.io.Files;

import java.io.File;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * A {@link SimpleMapPersister} which persists the data as JSON.
 * 
 * @author Keith M. Hughes
 */
public class JsonSimpleMapPersister implements SimpleMapPersister {

	/**
	 * The JSON mapper.
	 */
	private static final ObjectMapper MAPPER;

	static {
		MAPPER = new ObjectMapper();
		MAPPER.getJsonFactory().enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
	}

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
	 * @param baseMapDirectory
	 *            the base directory where the map files will be stored
	 */
	public JsonSimpleMapPersister(File baseMapDirectory) {
		this.baseMapDirectory = baseMapDirectory;
		if (!baseMapDirectory.exists()) {
			if (!baseMapDirectory.mkdirs()) {
				throw new InteractiveSpacesException(String.format(
						"Could not create directory %s", baseMapDirectory));
			}
		}
	}

	@Override
	public Map<String, Object> getMap(String name) {
		rwlock.readLock().lock();

		try {
			File mapFile = getMapFile(name);
			if (mapFile.exists()) {
				String content = Files.readFile(mapFile);
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) MAPPER
						.readValue(content, Map.class);
				return map;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Could not read map %s", name), e);
		} finally {
			rwlock.readLock().unlock();
		}
	}

	@Override
	public void putMap(String name, Map<String, Object> map) {
		rwlock.writeLock().lock();

		try {
			Files.writeFile(getMapFile(name), MAPPER.writeValueAsString(map));
		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Could not write map %s", name), e);
		} finally {
			rwlock.writeLock().unlock();
		}
	}

	/**
	 * Get the file which stores the particular map file.
	 * 
	 * @param name
	 *            the name of the map
	 * 
	 * @return the location of the map file
	 */
	private File getMapFile(String name) {
		return new File(baseMapDirectory, name + ".json");
	}
}
