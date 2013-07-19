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

package interactivespaces.launcher.bootstrap;

import interactivespaces.system.core.configuration.ConfigurationProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * A configuration provider from a file.
 * 
 * @author Keith M. Hughes
 */
public class FileConfigurationProvider implements ConfigurationProvider {

	/**
	 * Extensions on config files.
	 */
	private static final String CONFIGURATION_FILES_EXTENSION = ".conf";

	/**
	 * The initial configuration
	 */
	private File configFolder;
	
	/**
	 * The current configuration.
	 */
	private Map<String, String> currentConfiguration;

	public FileConfigurationProvider(File configFolder) {
		this.configFolder = configFolder;
	}

	@Override
	public Map<String, String> getInitialConfiguration() {
		return currentConfiguration;
	}

	/**
	 * Load all conf files in the configuration folder.
	 */
	public void load() {
		currentConfiguration = new HashMap<String, String>();
		
		// Look in the specified bundle directory to create a list
		// of all JAR files to install.
		File[] files = configFolder.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(
								CONFIGURATION_FILES_EXTENSION);
					}
				});
		if (files == null || files.length == 0) {
			System.err.format("Couldn't load config files from %s\n", configFolder);
		}

		for (File file : files) {
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(file));
				for (Entry<Object, Object> p : props.entrySet()) {
					currentConfiguration.put((String) p.getKey(), (String) p.getValue());
				}
			} catch (IOException e) {
				System.err.format("Couldn't load config file %s\n", file);
			}
		}
	}
}
