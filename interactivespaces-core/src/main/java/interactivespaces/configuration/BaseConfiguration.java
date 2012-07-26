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
import interactivespaces.evaluation.EvaluationEnvironment;
import interactivespaces.evaluation.EvaluationInteractiveSpacesException;
import interactivespaces.evaluation.ExpressionEvaluator;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Support for implementations of {@link Configuration}.
 * 
 * @author Keith M. Hughes
 */
public abstract class BaseConfiguration implements Configuration, EvaluationEnvironment {

	/**
	 * Parent configuration to this configuration.
	 */
	protected Configuration parent;

	/**
	 * The expression evaluator for this configuration.
	 */
	private ExpressionEvaluator expressionEvaluator;

	/**
	 * @param expressionEvaluator
	 *            the expression evaluator for this configuration.
	 */
	public BaseConfiguration(ExpressionEvaluator expressionEvaluator) {
		this.expressionEvaluator = expressionEvaluator;
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
		return expressionEvaluator.evaluateStringExpression(expression);
	}

	@Override
	public String getPropertyString(String property) {
		return getValue(property);
	}

	@Override
	public String getPropertyString(String property, String defaultValue) {
		String value = getValue(property);
		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}

	@Override
	public String getRequiredPropertyString(String property) {
		String value = getValue(property);
		if (value != null) {
			return value;
		} else {
			throw new InteractiveSpacesException("Required property not defined: " + property);
		}
	}

	@Override
	public Integer getPropertyInteger(String property, Integer defaultValue) {
		String value = getValue(property);
		if (value != null) {
			return Integer.valueOf(value);
		} else {
			return defaultValue;
		}
	}

	@Override
	public Long getPropertyLong(String property, Long defaultValue) {
		String value = getValue(property);
		if (value != null) {
			return Long.valueOf(value);
		} else {
			return defaultValue;
		}
	}

	@Override
	public Double getPropertyDouble(String property, Double defaultValue) {
		String value = getValue(property);
		if (value != null) {
			return Double.valueOf(value);
		} else {
			return defaultValue;
		}
	}

	@Override
	public Boolean getPropertyBoolean(String property, Boolean defaultValue) {
		String value = getValue(property);
		if (value != null) {
			return "true".equalsIgnoreCase(value);
		} else {
			return defaultValue;
		}
	}

	@Override
	public boolean containsProperty(String property) {
		Configuration current = this;
		while (current != null) {
			if (containsPropertyLocally(property)) {
				return true;
			}

			current = current.getParent();
		}

		return false;
	}

	/**
	 * Get the property from the actual implementation.
	 * 
	 * @param property
	 *            Name of the property.
	 * 
	 * @return The value of the property, or null if not found.
	 */
	private String getValue(String property) {
		String value = null;

		Configuration current = this;
		while (current != null
				&& ((value = current.findValueLocally(property)) == null)) {
			current = current.getParent();
		}

		if (value != null) {
			return expressionEvaluator.evaluateStringExpression(value);
		} else {
			return null;
		}
	}

	@Override
	public String lookupVariableValue(String variable) throws EvaluationInteractiveSpacesException {
		return getValue(variable);
	}

	@Override
	public Map<String, String> getCollapsedMap() {
		Map<String, String> map = Maps.newHashMap();
		
		return map;
	}
}
