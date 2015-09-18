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

package interactivespaces.domain.support;

import interactivespaces.domain.support.DomainValidationResult.DomainValidationResultType;

/**
 * A validator for space controller names.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerNameValidator implements Validator {

  /**
   * Description for a space controller name being required.
   */
  public static final String DESCRIPTION_SPACE_CONTROLLER_NAME_REQUIRED = "A space controller name is required.";

  @Override
  public DomainValidationResult validate(String hostId) {
    hostId = hostId.trim();
    if (hostId.isEmpty()) {
      return new DomainValidationResult(DomainValidationResultType.ERRORS, DESCRIPTION_SPACE_CONTROLLER_NAME_REQUIRED);
    }

    return new DomainValidationResult(DomainValidationResultType.OK, null);
  }
}
