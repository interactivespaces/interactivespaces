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

package interactivespaces.domain.support;

import interactivespaces.domain.support.DomainValidationResult.DomainValidationResultType;
import interactivespaces.resource.Version;

/**
 * A validator for versions.
 *
 * @author Keith M. Hughes
 */
public class VersionValidator implements Validator {

  @Override
  public DomainValidationResult validate(String candidate) {
    candidate = candidate.trim();

    if (candidate.isEmpty()) {
      return new DomainValidationResult(DomainValidationResultType.ERRORS, "A version is required.");
    }

    if (!Version.isLegalSyntax(candidate)) {
      return new DomainValidationResult(DomainValidationResultType.ERRORS,
          Version.VERSION_FORMAT_DESCRIPTION);
    }

    return new DomainValidationResult(DomainValidationResultType.OK, null);
  }
}
