/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package interactivespaces.configuration;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.evaluation.EvaluationEnvironment;
import interactivespaces.evaluation.EvaluationInteractiveSpacesException;
import interactivespaces.evaluation.ExpressionEvaluator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
   *          the expression evaluator for this configuration.
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
      throw new InteractiveSpacesException(String.format("Required property %s does not exist",
          property));
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
  public Integer getRequiredPropertyInteger(String property) {
    return Integer.valueOf(getRequiredPropertyString(property));
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
  public Long getRequiredPropertyLong(String property) {
    return Long.valueOf(getRequiredPropertyString(property));
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
  public Double getRequiredPropertyDouble(String property) {
    return Double.valueOf(getRequiredPropertyString(property));
  }

  @Override
  public Boolean getPropertyBoolean(String property, Boolean defaultValue) {
    String value = getValue(property);
    if (value != null) {
      return getBooleanValue(value);
    } else {
      return defaultValue;
    }
  }

  @Override
  public Boolean getRequiredPropertyBoolean(String property) {
    return getBooleanValue(getRequiredPropertyString(property));
  }

  @Override
  public List<String> getPropertyStringList(String property, String delineator) {
    String value = getValue(property);
    if (value != null) {
      return Lists.newArrayList(value.split(delineator));
    } else {
      return null;
    }
  }

  @Override
  public Set<String> getPropertyStringSet(String property, String delineator) {
    String value = getValue(property);
    if (value != null) {
      return Sets.newHashSet(Arrays.asList(value.split(delineator)));
    } else {
      return null;
    }
  }

  /**
   * Get the boolean value for the given string.
   *
   * @param value
   *          the string
   *
   * @return {@code true} if the string represents an Interactive Spaces true
   *         value
   */
  private boolean getBooleanValue(String value) {
    return "true".equalsIgnoreCase(value);
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
   *          Name of the property.
   *
   * @return The value of the property, or null if not found.
   */
  private String getValue(String property) {
    String value = null;

    Configuration current = this;
    while (current != null && ((value = current.findValueLocally(property)) == null)) {
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
  public void setValues(Map<String, String> values) {
    for (Entry<String, String> entry : values.entrySet()) {
      setValue(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public Map<String, String> getCollapsedMap() {
    Map<String, String> map = Maps.newHashMap();

    addCollapsedEntries(map);

    return map;
  }
}
