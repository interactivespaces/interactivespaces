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

import org.apache.commons.logging.Log;

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
   * Logger to use for success/error messages.
   */
  private final Log log;

  /**
   * Construct a new processor.
   *
   * @param configuration
   *          the configuration the processor will use
   * @param log
   *          logger to use
   */
  public StandardConfigurationPropertyAnnotationProcessor(Configuration configuration, Log log) {
    this.configuration = configuration;
    this.log = log;
  }

  @Override
  public void process(Object obj) {
    log.info("Processing configuration annotations on " + obj);
    List<String> errors = Lists.newArrayList();
    List<String> successes = Lists.newArrayList();
    for (Class<?> c = obj.getClass(); c != null; c = c.getSuperclass()) {
      Field[] fields = c.getDeclaredFields();
      for (Field field : fields) {
        processField(obj, field, errors, successes);
      }
    }
    for (String success : successes) {
      log.debug(success);
    }
    if (!errors.isEmpty()) {
      SimpleInteractiveSpacesException
          .throwFormattedException("Errors while processing configuration annotations on %s:\n%s",
              obj, Joiner.on('\n').join(errors));
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
   * @param errors
   *          list container to accumulate errors
   * @param successes
   *          list container to accumulate successes
   */
  private void processField(Object obj, Field field, List<String> errors, List<String> successes) {
    ConfigurationProperty annotation = field.getAnnotation(ConfigurationProperty.class);
    if (annotation == null) {
      return;
    }
    int initialErrorSize = errors.size();
    String fieldName = field.getName();
    if (Modifier.isFinal(field.getModifiers())) {
      errors.add(String.format("Field '%s' is marked final and may have unpredictable effects", fieldName));
    }
    String property = annotation.name();
    if (property == null || property.isEmpty()) {
      property = annotation.value();
    }
    property = property.trim();
    if (property.isEmpty()) {
      errors.add(String.format("Field '%s' has property name that is all white space or empty", fieldName));
    }

    boolean required = annotation.required();
    String delimiter = annotation.delimiter();
    boolean accessible = field.isAccessible();
    if (!accessible) {
      field.setAccessible(true);
    }
    try {
      Object value = null;
      Class<?> type = field.getType();
      if (required) {
        Object defaultValue = field.get(obj);
        boolean valueIsNotDefault = !Objects.equal(defaultValue, Defaults.defaultValue(type));
        if (valueIsNotDefault) {
          errors.add(String.format("Field '%s' into which a required property '%s' "
                  + "is to be injected already has a value: '%s', set 'required = false', "
                  + "or set the value of the property in the configuration "
                  + "(do not initialize the field directly).",
              fieldName, property, defaultValue));
        }

        if (!configuration.containsProperty(property) && !property.isEmpty()) {
          errors.add(String.format("Field '%s' does not contain required property '%s'",
              fieldName, property));
        }
      }

      // If value is required but not present, an error has already been registered.
      if (type == int.class || type == Integer.class) {
        value = configuration.getPropertyInteger(property, null);
      } else if (type == long.class || type == Long.class) {
        value = configuration.getPropertyLong(property, null);
      } else if (type == double.class || type == Double.class) {
        value = configuration.getPropertyDouble(property, null);
      } else if (type == boolean.class || type == Boolean.class) {
        value = configuration.getPropertyBoolean(property, null);
      } else if (type == String.class) {
        value = configuration.getPropertyString(property);
      } else if (type.isAssignableFrom(List.class)) {
        value = configuration.getPropertyStringList(property, delimiter);
      } else if (type.isAssignableFrom(Set.class)) {
        value = configuration.getPropertyStringSet(property, delimiter);
      } else {
        errors.add(String.format(String.format("Field '%s' has unsupported type '%s'",
            fieldName, tryGetValueForErrorMessage(property))));
      }
      if (errors.size() != initialErrorSize) {
        return;
      }
      String header = "@" + ConfigurationProperty.class.getSimpleName();
      if (value != null) {
        successes.add(String.format("%s field '%s' injected property '%s' with value '%s'",
            header, fieldName, property, value));
        field.set(obj, value);
      } else {
        successes.add(String.format("%s field '%s' has no value from property '%s', skipping",
            header, fieldName, property));
      }
    } catch (IllegalAccessException e) {
      errors.add(String.format("Field '%s' can not be accessed: %s", fieldName, e.toString()));
    } catch (Exception e) {
      errors.add(String.format("Field '%s' with property '%s' encountered error: %s",
          fieldName, tryGetValueForErrorMessage(property), e.toString()));
    } finally {
      if (!accessible) {
        field.setAccessible(false);
      }
    }
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
