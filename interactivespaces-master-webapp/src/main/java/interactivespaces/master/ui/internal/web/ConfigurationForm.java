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

package interactivespaces.master.ui.internal.web;

import com.google.common.collect.Maps;

import org.springframework.validation.Errors;

import java.util.Map;

/**
 * A form for playing with configurations.
 *
 * @author Keith M. Hughes
 */
public class ConfigurationForm {

  /**
   * The string which contains the values.
   */
  private String values;

  /**
   * @return the values
   */
  public String getValues() {
    return values;
  }

  /**
   * @param values
   *          the values to set
   */
  public void setValues(String values) {
    this.values = values;
  }

  /**
   * Get a map of the submitted parameters.
   *
   * @param form
   *          the form
   *
   * @return a map of the names to values
   */
  public Map<String, Object> getSubmittedMap() {
    Map<String, Object> map = Maps.newHashMap();

    String[] lines = values.split("\n");

    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty())
        continue;

      int pos = line.indexOf('=');
      map.put(new String(line.substring(0, pos).trim()), new String(line.substring(pos + 1).trim()));
    }

    return map;
  }

  /**
   * Get a map of the submitted parameters.
   *
   * @param form
   *          the form
   * @param blankValuesAllowed
   *          {@code true} if blank values are allowed
   * @param errorCodePrefix
   *          prefix to put on error codes
   *
   * @return a map of the config names to values
   */
  public void validate(Errors errors, boolean blankValuesAllowed, String errorCodePrefix) {
    String[] lines = getValues().split("\n");
    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty())
        continue;

      int pos = line.indexOf('=');
      if (pos == -1) {
        errors.rejectValue("values", errorCodePrefix + ".error.field.format", "error");
        return;
      }
      if (line.substring(0, pos).trim().isEmpty()) {
        errors.rejectValue("values", errorCodePrefix + ".error.name.blank", "error");
        return;
      }
      if (!blankValuesAllowed && line.substring(pos + 1).trim().isEmpty()) {
        errors.rejectValue("values", errorCodePrefix + ".error.value.blank", "error");
        return;
      }
    }
  }
}
