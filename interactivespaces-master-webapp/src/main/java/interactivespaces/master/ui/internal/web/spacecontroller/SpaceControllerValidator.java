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

package interactivespaces.master.ui.internal.web.spacecontroller;

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.master.ui.internal.web.FormObjectValidator;

import org.springframework.validation.Errors;

/**
 * A validator for {@link SpaceController} instances.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerValidator extends FormObjectValidator {

  /**
   * Validate a space controller.
   *
   * @param spaceController
   *          the space controller
   * @param errors
   *          the errors
   */
  public void validate(SpaceController spaceController, Errors errors) {
    String name = spaceController.getName();
    if (!hasValue(name)) {
      errors.rejectValue("name", "required", "required");
    }
    String hostId = spaceController.getHostId();
    if (!hasValue(hostId)) {
      errors.rejectValue("hostId", "required", "required");
    }
  }

  /**
   * Validate a space controller.
   *
   * @param spaceController
   *          the space controller
   * @param errors
   *          the errors
   */
  public void validate(SimpleSpaceController spaceController, Errors errors) {
    // This little annoyance is because Spring wants to have direct class
    // equality when finding which validation method to call.
    validate((SpaceController) spaceController, errors);
  }
}
