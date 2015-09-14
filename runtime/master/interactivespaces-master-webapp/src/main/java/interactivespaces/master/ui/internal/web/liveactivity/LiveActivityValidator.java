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

package interactivespaces.master.ui.internal.web.liveactivity;

import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.ui.internal.web.FormObjectValidator;
import interactivespaces.master.ui.internal.web.liveactivity.LiveActivityAction.LiveActivityForm;

import org.springframework.validation.Errors;

/**
 * A validator for {@link LiveActivity} instances.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityValidator extends FormObjectValidator {

  /**
   * Validate a live activity.
   *
   * @param liveActivity
   *          the live activity
   * @param errors
   *          the errors
   */
  public void validate(LiveActivity liveActivity, Errors errors) {
    validateLiveActivityPortion(liveActivity, "", errors);
  }

  /**
   * Validate a live activity form.
   *
   * @param liveActivityForm
   *          the live activity form
   * @param errors
   *          the errors
   */
  public void validate(LiveActivityForm liveActivityForm, Errors errors) {
    validateLiveActivityPortion(liveActivityForm.getLiveActivity(), "liveActivity.", errors);
    if (!hasValue(liveActivityForm.getActivityId())) {
      errors.rejectValue("activityId", "required", "required");
    }
    if (!hasValue(liveActivityForm.getControllerId())) {
      errors.rejectValue("controllerId", "required", "required");
    }
  }

  /**
   * Validate a live activity portion of a Spring submission.
   *
   * @param liveActivity
   *          the live activity
   * @param pathPrefix
   *          form path to get to the live activity
   * @param errors
   *          the errors
   */
  private void validateLiveActivityPortion(LiveActivity liveActivity, String pathPrefix, Errors errors) {
    String name = liveActivity.getName();
    if (!hasValue(name)) {
      errors.rejectValue(pathPrefix + "name", "required", "required");
    }
  }
}
