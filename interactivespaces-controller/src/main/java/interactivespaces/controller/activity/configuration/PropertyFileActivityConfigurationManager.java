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

package interactivespaces.controller.activity.configuration;

import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.configuration.ConfigurationStorageManager;
import interactivespaces.configuration.SimplePropertyFileConfigurationStorageManager;
import interactivespaces.evaluation.ExpressionEvaluator;
import interactivespaces.evaluation.ExpressionEvaluatorFactory;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.io.File;

/**
 * A configuration manager which uses Java property files.
 * 
 * @author Keith M. Hughes
 */
public class PropertyFileActivityConfigurationManager implements
		ActivityConfigurationManager {

	/**
	 * File extension configuration files should have.
	 */
	public static final String CONFIGURATION_FILE_EXTENSION = "conf";

	/**
	 * The Interactive Spaces environment.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * Factory for expression evaluators.
	 */
	private ExpressionEvaluatorFactory expressionEvaluatorFactory;

	@Override
	public SimpleActivityConfiguration getConfiguration(
			ActivityFilesystem activityFilesystem) {
		ExpressionEvaluator expressionEvaluator = expressionEvaluatorFactory
				.newEvaluator();

		ConfigurationStorageManager baseConfigurationStorageManager = newConfiguration(
				activityFilesystem
						.getInstallFile(getConfigFileName("activity")),
				true, expressionEvaluator);

		ConfigurationStorageManager installedActivityConfigurationStorageManager = newConfiguration(
				activityFilesystem
						.getPermanentDataFile(getConfigFileName("activity")),
				false, expressionEvaluator);

		SimpleActivityConfiguration configuration = new SimpleActivityConfiguration(
				baseConfigurationStorageManager,
				installedActivityConfigurationStorageManager,
				expressionEvaluator, spaceEnvironment.getSystemConfiguration());
		expressionEvaluator.setEvaluationEnvironment(configuration);

		return configuration;
	}

	/**
	 * Create a new configuration storage manager.
	 * 
	 * @param configurationFile
	 *            where the configuration file resides
	 * @param required
	 *            {@code true} if the configuration is required to run
	 * @param expressionEvaluator
	 *            expression evaluator to be given to the configuration
	 * 
	 * @return a configuration storage manager
	 */
	private ConfigurationStorageManager newConfiguration(
			File configurationFile, boolean required,
			ExpressionEvaluator expressionEvaluator) {
		return new SimplePropertyFileConfigurationStorageManager(
				required,
				configurationFile, expressionEvaluator);
	}

	/**
	 * Get the configuration file name from the configuration type.
	 * 
	 * @param configType
	 * @return
	 */
	private String getConfigFileName(String configType) {
		return configType + "." + CONFIGURATION_FILE_EXTENSION;
	}

	/**
	 * @param expressionEvaluatorFactory
	 *            the expressionEvaluatorFactory to set
	 */
	public void setExpressionEvaluatorFactory(
			ExpressionEvaluatorFactory expressionEvaluatorFactory) {
		this.expressionEvaluatorFactory = expressionEvaluatorFactory;
	}

	/**
	 * set the Interactive Spaces environment to use.
	 * 
	 * @param spaceEnvironment
	 *            the spaceEnvironment to use
	 */
	public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}
}
