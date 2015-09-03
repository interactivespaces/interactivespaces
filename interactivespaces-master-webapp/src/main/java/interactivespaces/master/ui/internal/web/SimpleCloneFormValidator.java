/*
 * Copyright (C) 2015 Google Inc.
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

import org.springframework.validation.Errors;

/**
 * A form validator for the simple clone form.
 *
 * @author Keith M. Hughes
 */
public class SimpleCloneFormValidator extends FormObjectValidator {

  /**
   * The field name for the name prefix.
   */
  public static final String FIELD_NAME_NAME_PREFIX = "namePrefix";

  /**
   * Validate a clone form.
   *
   * @param form
   *          the clone form
   * @param errors
   *          the errors
   */
  public void validate(SimpleCloneForm form, Errors errors) {
    String name = form.getNamePrefix();
    if (!hasValue(name)) {
      errors.rejectValue(FIELD_NAME_NAME_PREFIX, FormObjectValidator.ERROR_CODE_REQUIRED,
          FormObjectValidator.ERROR_CODE_REQUIRED);
    }
  }
}
