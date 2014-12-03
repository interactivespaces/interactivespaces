/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.activity.annotation;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;

import com.google.common.base.Defaults;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

/**
 * An annotation processor that injects configuration values into fields marked with
 * {@link interactivespaces.activity.annotation.ConfigurationProperty}.
 *
 * @author Oleksandr Kelepko
 */
public class StandardConfigurationPropertyAnnotationProcessor implements ConfigurationPropertyAnnotationProcessor {

  /**
   * The configuration where values are obtained.
   */
  private final Configuration configuration;

  /**
   * Construct a new processor.
   *
   * @param configuration
   *          the configuration the processor will use
   */
  public StandardConfigurationPropertyAnnotationProcessor(Configuration configuration) {
    this.configuration = configuration;
  }

  /* (non-Javadoc)
   * @see interactivespaces.activity.annotation.ConfigurationPropertyAnnotationProcessor#process(java.lang.Object)
   */
  @Override
  public void process(Object obj) {
    List<String> errors = Lists.newArrayList();
    for (Class<?> c = obj.getClass(); c != null; c = c.getSuperclass()) {
      Field[] fields = c.getDeclaredFields();
      for (Field field : fields) {
        String error = processField(obj, field);
        if (error != null) {
          errors.add(error);
        }
      }
    }
    if (!errors.isEmpty()) {
      throw new SimpleInteractiveSpacesException(Joiner.on('\n').join(errors));
    }
  }

  /**
   * Injects config parameters into a given field of a given object, if the field is marked with
   * {@link interactivespaces.activity.annotation.ConfigurationProperty}.
   *
   * @param obj
   *          object that contains the field into which config parameters will be injected
   * @param field
   *          field into which a config parameter will be injected
   *
   * @return {@code null} if the config value was successfully injected; error message, otherwise.
   */
  private String processField(Object obj, Field field) {
    ConfigurationProperty annotation = field.getAnnotation(ConfigurationProperty.class);
    if (annotation == null) {
      return null;
    }
    if (Modifier.isFinal(field.getModifiers())) {
      return "Modifying a final field may have unpredictable effects: " + field;
    }
    String property = annotation.name();
    if (property == null || property.isEmpty()) {
      property = annotation.value();
    }
    if (property == null) {
      return "Property name is null: " + field;
    }
    property = property.trim();
    if (property.isEmpty()) {
      return "Property name is all white space or empty: " + field;
    }

    boolean required = annotation.required();
    String delimiter = annotation.delimiter();
    boolean accessible = field.isAccessible();
    if (!accessible) {
      field.setAccessible(true);
    }
    try {
      Class<?> type = field.getType();
      if (required) {
        Object value = field.get(obj);
        boolean valueIsNotDefault = !Objects.equal(value, Defaults.defaultValue(type));
        if (valueIsNotDefault) {
          return String.format("The field '%s' into which a required property '%s' "
              + "is to be injected already has a value: '%s'. Set 'required = false', "
              + "or set the value of the property in the configuration " + "(do not initialize the field directly).",
              field, property, value);
        }
      }
      Object value;
      if (type == int.class || type == Integer.class) {
        value =
            required ? configuration.getRequiredPropertyInteger(property) : configuration.getPropertyInteger(property,
                null);
      } else if (type == long.class || type == Long.class) {
        value =
            required ? configuration.getRequiredPropertyLong(property) : configuration.getPropertyLong(property, null);
      } else if (type == double.class || type == Double.class) {
        value =
            required ? configuration.getRequiredPropertyDouble(property) : configuration.getPropertyDouble(property,
                null);
      } else if (type == boolean.class || type == Boolean.class) {
        value =
            required ? configuration.getRequiredPropertyBoolean(property) : configuration.getPropertyBoolean(property,
                null);
      } else if (type == String.class) {
        value =
            required ? configuration.getRequiredPropertyString(property) : configuration.getPropertyString(property);
      } else if (type.isAssignableFrom(List.class)) {
        value = configuration.getPropertyStringList(property, delimiter);
        if (value == null && required) {
          requiredPropertyDoesNotExist(property);
        }
      } else if (type.isAssignableFrom(Set.class)) {
        value = configuration.getPropertyStringSet(property, delimiter);
        if (value == null && required) {
          requiredPropertyDoesNotExist(property);
        }
      } else {
        return String.format("Cannot inject config value '%s' - unsupported type of the field: %s",
            tryGetValueForErrorMessage(property), field);
      }
      if (value != null) {
        field.set(obj, value);
      }
    } catch (IllegalAccessException e) {
      return String.format("Cannot access %s: %s", field, e.toString());
    } catch (Exception e) {
      return String.format("Cannot inject configuration value '%s' into %s: %s", tryGetValueForErrorMessage(property),
          field, e.toString());
    } finally {
      if (!accessible) {
        field.setAccessible(false);
      }
    }
    return null;
  }

  /**
   * Throw an exception for a required property.
   *
   * @param property
   *          required property for which there's no value in the configuration
   */
  private void requiredPropertyDoesNotExist(String property) {
    // there's no getRequiredPropertyString[List|Set]; throwing an exception
    throw new SimpleInteractiveSpacesException(String.format("Required property %s does not exist", property));
  }

  /**
   * Retrieves config value for a given property as a String (for logging).
   *
   * @param property
   *          property whose value will be retrieved
   *
   * @return config value for the given property or the empty string if there was an exception
   */
  private String tryGetValueForErrorMessage(String property) {
    try {
      return configuration.getPropertyString(property);
    } catch (Exception e) {
      return "";
    }
  }
}
