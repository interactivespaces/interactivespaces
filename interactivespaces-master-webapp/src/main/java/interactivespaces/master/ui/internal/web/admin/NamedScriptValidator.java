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

package interactivespaces.master.ui.internal.web.admin;

import interactivespaces.domain.system.NamedScript;
import interactivespaces.domain.system.pojo.SimpleNamedScript;
import interactivespaces.master.ui.internal.web.FormObjectValidator;

import org.springframework.validation.Errors;

/**
 * A validator for {@link NamedScript} instances.
 *
 * @author Keith M. Hughes
 */
public class NamedScriptValidator extends FormObjectValidator {

  /**
   * Validate a named script.
   *
   * @param namedScript
   *          the named script
   * @param errors
   *          the errors
   */
  public void validate(NamedScript namedScript, Errors errors) {
    String name = namedScript.getName();
    if (!hasValue(name)) {
      errors.rejectValue("name", "required", "required");
    }
    String language = namedScript.getLanguage();
    if (!hasValue(language)) {
      errors.rejectValue("language", "required", "required");
    }
    if (namedScript.getScheduled()) {
      String schedule = namedScript.getSchedule();
      if (!hasValue(schedule)) {
        errors.rejectValue("schedule", "required", "required");
      }
    }
  }

  /**
   * Validate a named script.
   *
   * @param namedScript
   *          the named script
   * @param errors
   *          the errors
   */
  public void validate(SimpleNamedScript namedScript, Errors errors) {
    // This little annoyance is because Spring wants to have direct class
    // equality when finding which validation method to call.
    validate((NamedScript) namedScript, errors);
  }
}
