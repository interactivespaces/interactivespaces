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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link SpaceControllerHostIdValidator}.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerHostIdValidatorTest {
  private SpaceControllerHostIdValidator validator;

  @Before
  public void setup() {
    validator = new SpaceControllerHostIdValidator();
  }

  /**
   * Test that a clean startup works.
   */
  @Test
  public void testCleanStartup() {
    assertEquals(DomainValidationResult.DomainValidationResultType.OK, validator.validate("_123435").getResultType());
    assertEquals(DomainValidationResult.DomainValidationResultType.OK, validator.validate("a_35").getResultType());

    assertEquals(DomainValidationResult.DomainValidationResultType.ERRORS, validator.validate("12asd").getResultType());
    assertEquals(DomainValidationResult.DomainValidationResultType.ERRORS, validator.validate("a-sd").getResultType());

    assertEquals(DomainValidationResult.DomainValidationResultType.OK, validator.validate("asd/a12").getResultType());
    assertEquals(DomainValidationResult.DomainValidationResultType.OK, validator.validate("asd/_a12/foo123").getResultType());
    assertEquals(DomainValidationResult.DomainValidationResultType.ERRORS, validator.validate("/aaa/bbb")
        .getResultType());
  }
}
