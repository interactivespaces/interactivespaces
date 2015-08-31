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

import java.util.regex.Pattern;

/**
 * A validator for space controller host IDs.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerHostIdValidator implements Validator {

  /**
   * The regular expression for a host ID segment.
   */
  public static final String HOST_ID_SEGMENT_VALIDATION_REGEX = "[a-zA-Z_][a-zA-Z_0-9]*";

  /**
   * Pattern for the version.
   */
  public static final Pattern HOST_ID_VALIDATION_PATTERN = Pattern.compile("^(" + HOST_ID_SEGMENT_VALIDATION_REGEX
      + ")(/" + HOST_ID_SEGMENT_VALIDATION_REGEX + ")*$");

  /**
   * Description of a legal version.
   */
  public static final String HOST_ID_FORMAT_DESCRIPTION = "A host must be of the form segment1/segment2/segment3...\n"
      + "where each segment starts with a letter or underscore, and\n" + ""
      + "is followed by letters, digits, -, or underscores.";

  @Override
  public DomainValidationResult validate(String hostId) {
    hostId = hostId.trim();

    if (hostId.isEmpty()) {
      return new DomainValidationResult(DomainValidationResultType.ERRORS, "A host ID is required.");
    }

    if (!HOST_ID_VALIDATION_PATTERN.matcher(hostId).matches()) {
      return new DomainValidationResult(DomainValidationResultType.ERRORS, HOST_ID_FORMAT_DESCRIPTION);
    }

    return new DomainValidationResult(DomainValidationResultType.OK, null);
  }
}
