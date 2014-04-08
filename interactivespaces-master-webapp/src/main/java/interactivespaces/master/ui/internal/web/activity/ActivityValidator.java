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

package interactivespaces.master.ui.internal.web.activity;

import interactivespaces.domain.basic.Activity;
import interactivespaces.master.ui.internal.web.FormObjectValidator;
import interactivespaces.master.ui.internal.web.activity.ActivityAction.ActivityForm;

import org.springframework.validation.Errors;

/**
 * A validator for {@link Activity} instances.
 *
 * @author Keith M. Hughes
 */
public class ActivityValidator extends FormObjectValidator {

  /**
   * Validate an activity.
   *
   * @param activity
   *          the activity
   * @param errors
   *          the errors
   */
  public void validate(Activity activity, Errors errors) {
    validateActivityPortion(activity, "", errors);
  }

  /**
   * Validate an activity form.
   *
   * @param activityForm
   *          the activity form
   * @param errors
   *          the errors
   */
  public void validate(ActivityForm activityForm, Errors errors) {
    // Nothing to do
  }

  /**
   * Validate an activity portion of a Spring submission.
   *
   * @param activity
   *          the activity
   * @param pathPrefix
   *          form path to get to the activity
   * @param errors
   *          the errors
   */
  private void validateActivityPortion(Activity activity, String pathPrefix, Errors errors) {
    String name = activity.getName();
    if (!hasValue(name)) {
      errors.rejectValue(pathPrefix + "name", "required", "required");
    }
  }
}
