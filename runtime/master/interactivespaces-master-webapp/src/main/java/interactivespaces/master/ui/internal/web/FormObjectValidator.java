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

package interactivespaces.master.ui.internal.web;


/**
 * A base class with support for form validation for domain objects.
 *
 * @author Keith M. Hughes
 */
public abstract class FormObjectValidator {

  /**
   * The error code for a required field.
   */
  public static final String ERROR_CODE_REQUIRED = "required";

  /**
   * Check to see if a string has a value. By value, it cannot be null and it
   * must contain some non-whitespace characters.
   *
   * @param value
   *          the string to check
   *
   * @return {@code true} if the string has a value according to the above
   *         definition
   */
  protected boolean hasValue(String value) {
    return value != null && !value.trim().isEmpty();
  }
}
