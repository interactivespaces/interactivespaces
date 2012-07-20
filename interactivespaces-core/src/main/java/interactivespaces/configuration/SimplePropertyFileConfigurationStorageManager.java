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

package interactivespaces.configuration;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.evaluation.ExpressionEvaluator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * A file based configuration storage manager.
 * 
 * @author Keith M. Hughes
 */
public class SimplePropertyFileConfigurationStorageManager implements
		ConfigurationStorageManager {

	/**
	 * The properties object being handled.
	 */
	private Properties properties;

	/**
	 * The configuration being managed.
	 */
	private Configuration configuration;

	/**
	 * The file containing the configuration.
	 */
	private File configurationFile;

	/**
	 * {@code true} if this configuration must exist.
	 * 
	 * <p>
	 * Loading non-existent configurations is legal.
	 */
	private boolean required;

	/**
	 * Last time the file was modified.
	 */
	private long lastModifiedTime = 0;

	/**
	 * 
	 * <p>
	 * Loading non-existent configurations is legal if they are not required.
	 * 
	 * @param configuration
	 *            the configuration being managed
	 * @param required
	 *            {@code true} if this configuration must exist.
	 * @param configurationFile
	 *            the file containing the configuration
	 */
	public SimplePropertyFileConfigurationStorageManager(boolean required,
			File configurationFile, ExpressionEvaluator expressionEvaluator) {
		this.required = required;
		this.configurationFile = configurationFile;

		properties = new Properties();
		this.configuration = new PropertiesConfiguration(properties,
				expressionEvaluator);
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public void load() {
		if (configurationFile.exists()) {
			long newLastModified = configurationFile.lastModified();
			if (newLastModified > lastModifiedTime) {
				lastModifiedTime = newLastModified;

				// Configuration holds the same properties object,
				// so no need to clear configuration.
				properties.clear();

				FileReader reader = null;
				try {
					reader = new FileReader(configurationFile);
					properties.load(reader);
				} catch (Exception e) {
					throw new InteractiveSpacesException(String.format(
							"Cannot read configuration file %s",
							configurationFile.getAbsolutePath()));
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
							// Don't care
						}
					}
				}
			}
		} else {
			if (required) {
				throw new InteractiveSpacesException(String.format(
						"Cannot locate required configuration file %s",
						configurationFile.getAbsolutePath()));
			}
		}
	}

	@Override
	public void save() {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(configurationFile);

			properties.store(out, "Configuration updated");

			out.flush();
		} catch (IOException e) {
			throw new InteractiveSpacesException("Could not save configuration file "
					+ configurationFile, e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// Don't care.
				}
			}
		}
	}

	@Override
	public void update(Map<String, Object> update) {
		for (Entry<String, Object> entry : update.entrySet()) {
			properties.put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void clear() {
		properties.clear();
	}
}
