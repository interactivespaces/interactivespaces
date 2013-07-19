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

/**
 * A validation result in the domain objects.
 *
 * @author Keith M. Hughes
 */
public class DomainValidationResult {

  /**
   * The result type of the validation.
   */
  private DomainValidationResultType resultType;

  /**
   * Description of the result.
   */
  private String description;

  public DomainValidationResult(DomainValidationResultType resultType, String description) {
    this.resultType = resultType;
    this.description = description;
  }

  /**
   * get the type of the result.
   *
   * @return the type of the result
   */
  public DomainValidationResultType getResultType() {
    return resultType;
  }

  /**
   * Get the description of the validation result.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  public enum DomainValidationResultType {

    /**
     * Everything validated properly.
     */
    OK,

    /**
     * There were warnings, but no errors.
     */
    WARNINGS,

    /**
     * There are errors.
     */
    ERRORS
  }
}
