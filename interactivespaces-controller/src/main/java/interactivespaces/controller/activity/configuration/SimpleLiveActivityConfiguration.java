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

import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.ConfigurationStorageManager;
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.evaluation.EvaluationInteractiveSpacesException;
import interactivespaces.evaluation.ExpressionEvaluator;

import java.util.Map;

/**
 * A configuration specifically for activities.
 * 
 * @author Keith M. Hughes
 */
public class SimpleLiveActivityConfiguration implements LiveActivityConfiguration {

	/**
	 * Storage manager for the base activity configuration.
	 */
	private ConfigurationStorageManager baseStorageManager;

	/**
	 * Storage manager for the installed activity configuration.
	 */
	private ConfigurationStorageManager installedActivityStorageManager;

	/**
	 * The temporary configuration is the root. All delegated calls go to this
	 * one.
	 */
	private SimpleConfiguration temporary;

	/**
	 * The system configuration.
	 */
	private Configuration systemConfiguration;

	/**
	 * Parent of this configuration.
	 */
	private Configuration parent;

	/**
	 * @param baseStorageManager
	 *            the storage manager for the base configuration
	 * @param installedActivityStorageManager
	 *            the storage manager for the installed activity
	 * @param expressionEvaluator
	 *            the expression evaluator for this configuration
	 * @param systemConfiguration
	 *            the system configuration
	 */
	public SimpleLiveActivityConfiguration(
			ConfigurationStorageManager baseStorageManager,
			ConfigurationStorageManager installedActivityStorageManager,
			ExpressionEvaluator expressionEvaluator,
			Configuration systemConfiguration) {
		this.baseStorageManager = baseStorageManager;
		this.installedActivityStorageManager = installedActivityStorageManager;
		this.systemConfiguration = systemConfiguration;

		temporary = new SimpleConfiguration(expressionEvaluator);

		Configuration installedConfiguration = installedActivityStorageManager
				.getConfiguration();
		Configuration baseConfiguration = baseStorageManager.getConfiguration();
		baseConfiguration.setParent(systemConfiguration);
		installedConfiguration.setParent(baseConfiguration);
		temporary.setParent(installedConfiguration);
	}

	@Override
	public void load() {
		temporary.clear();
		baseStorageManager.load();
		installedActivityStorageManager.load();
	}

	@Override
	public void update(Map<String, Object> update) {
		installedActivityStorageManager.clear();
		installedActivityStorageManager.update(update);
		installedActivityStorageManager.save();
	}

	@Override
	public boolean containsPropertyLocally(String property) {
		return temporary.containsPropertyLocally(property);
	}

	@Override
	public String findValueLocally(String property) {
		return temporary.findValueLocally(property);
	}

	@Override
	public void setValue(String property, String value) {
		temporary.setValue(property, value);
	}

	@Override
	public void setParent(Configuration parent) {
		this.parent = parent;
	}

	@Override
	public Configuration getParent() {
		return parent;
	}

	@Override
	public String evaluate(String expression) {
		return temporary.evaluate(expression);
	}

	@Override
	public String getPropertyString(String property) {
		return temporary.getPropertyString(property);
	}

	@Override
	public String getPropertyString(String property, String defaultValue) {
		return temporary.getPropertyString(property, defaultValue);
	}

	@Override
	public String getRequiredPropertyString(String property) {
		return temporary.getRequiredPropertyString(property);
	}

	@Override
	public Integer getPropertyInteger(String property, Integer defaultValue) {
		return temporary.getPropertyInteger(property, defaultValue);
	}

	@Override
	public Integer getRequiredPropertyInteger(String property) {
		return temporary.getRequiredPropertyInteger(property);
	}

	@Override
	public Long getPropertyLong(String property, Long defaultValue) {
		return temporary.getPropertyLong(property, defaultValue);
	}

	@Override
	public Long getRequiredPropertyLong(String property) {
		return temporary.getRequiredPropertyLong(property);
	}

	@Override
	public Double getPropertyDouble(String property, Double defaultValue) {
		return temporary.getPropertyDouble(property, defaultValue);
	}

	@Override
	public Double getRequiredPropertyDouble(String property) {
		return temporary.getRequiredPropertyDouble(property);
	}

	@Override
	public Boolean getPropertyBoolean(String property, Boolean defaultValue) {
		return temporary.getPropertyBoolean(property, defaultValue);
	}

	@Override
	public Boolean getRequiredPropertyBoolean(String property) {
		return temporary.getRequiredPropertyBoolean(property);
	}

	@Override
	public boolean containsProperty(String property) {
		return temporary.containsProperty(property);
	}

	@Override
	public String lookupVariableValue(String variable)
			throws EvaluationInteractiveSpacesException {
		return temporary.lookupVariableValue(variable);
	}

	@Override
	public void clear() {
		// Clear the entire chain except the system configuration.
		for (Configuration current = temporary; current != systemConfiguration; current = current
				.getParent()) {
			current.clear();
		}
	}

	@Override
	public Map<String, String> getCollapsedMap() {
		return temporary.getCollapsedMap();
	}

	@Override
	public void addCollapsedEntries(Map<String, String> map) {
		temporary.addCollapsedEntries(map);
	}
}
