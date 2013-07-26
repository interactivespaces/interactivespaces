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

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * A validator for {@link SpaceController} instances.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerValidator {

  @SuppressWarnings("unchecked")
  public boolean supports(Class<?> clazz) {
    return SpaceController.class.isAssignableFrom(clazz);
  }

  public void validate(Object obj, Errors errors) {
    SpaceController controller = (SpaceController) obj;

    String name = controller.getName();
    if (!StringUtils.hasLength(name)) {
      errors.rejectValue("name", "required", "required");
    }
    String hostId = controller.getHostId();
    if (!StringUtils.hasLength(hostId)) {
      errors.rejectValue("hostId", "required", "required");
    }
  }
}
