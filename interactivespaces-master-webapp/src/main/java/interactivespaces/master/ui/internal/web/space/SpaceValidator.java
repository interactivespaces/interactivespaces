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

package interactivespaces.master.ui.internal.web.space;

import interactivespaces.domain.space.Space;
import interactivespaces.master.ui.internal.web.FormObjectValidator;

import com.google.common.collect.Sets;

import org.springframework.validation.Errors;

import java.util.Set;

/**
 * A validator for {@link SpaceForm} instances.
 *
 * @author Keith M. Hughes
 */
public class SpaceValidator extends FormObjectValidator {

  /**
   * Validate a space form.
   *
   * @param space
   *          the space being edited, can be {@code null}
   * @param spaceForm
   *          the space form
   * @param errors
   *          the errors
   */
  public void validate(SpaceForm spaceForm, Errors errors) {
    validate(null, spaceForm, errors);
  }

  /**
   * Validate a space form.
   *
   * @param space
   *          the space being edited, can be {@code null}
   * @param spaceForm
   *          the space form
   * @param errors
   *          the errors
   */
  public void validate(Space space, SpaceForm spaceForm, Errors errors) {
    String name = spaceForm.getSpace().getName();
    if (!hasValue(name)) {
      errors.rejectValue("space.name", "required", "required");
    }

    if (space != null) {
      Set<String> spaceIds = Sets.newHashSet();
      spaceIds.add(space.getId());
    }
  }
}
