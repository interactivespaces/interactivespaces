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

package interactivespaces.container.data;

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.support.DomainValidationResult;
import interactivespaces.domain.support.DomainValidationResult.DomainValidationResultType;
import interactivespaces.domain.support.SpaceControllerHostIdValidator;
import interactivespaces.domain.support.SpaceControllerNameValidator;
import interactivespaces.logging.ExtendedLog;

/**
 * A validator for space controller information for both a master and a space controller.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerInformationValidator {

  /**
   * The validator for names.
   */
  private SpaceControllerNameValidator nameValidator = new SpaceControllerNameValidator();

  /**
   * The validator for host IDs.
   */
  private SpaceControllerHostIdValidator hostIdValidator = new SpaceControllerHostIdValidator();

  /**
   * Check the controller information for errors.
   *
   * @param spaceControllerInfo
   *          the controller information to check
   * @param log
   *          the logger for errors
   *
   * @return a string builder that will contain content if there are errors and empty otherwise
   */
  public StringBuilder checkControllerInfoForErrors(SpaceController spaceControllerInfo, ExtendedLog log) {
    StringBuilder errorBuilder = new StringBuilder();

    String name = spaceControllerInfo.getName();
    DomainValidationResult nameValidate = nameValidator.validate(name);
    if (nameValidate.getResultType() == DomainValidationResultType.ERRORS) {
      String error = String.format("Space controller has illegal name %s\n%s", name, nameValidate.getDescription());
      log.error(error);

      errorBuilder.append(error);
    }

    String hostId = spaceControllerInfo.getHostId();
    DomainValidationResult hostIdValidate = hostIdValidator.validate(hostId);
    if (hostIdValidate.getResultType() == DomainValidationResultType.ERRORS) {
      String error =
          String.format("Space controller has illegal hostID %s\n%s", hostId, hostIdValidate.getDescription());
      log.error(error);

      if (errorBuilder.length() > 0) {
        errorBuilder.append("\n");
      }
      errorBuilder.append(error);
    }

    return errorBuilder;
  }
}
