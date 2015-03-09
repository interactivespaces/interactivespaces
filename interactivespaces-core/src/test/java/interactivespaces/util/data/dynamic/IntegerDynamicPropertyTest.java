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

package interactivespaces.util.data.dynamic;

import org.junit.Test;

/**
 * Tests for {@code Integer} properties.
 *
 * @author Oleksandr Kelepko
 */
public class IntegerDynamicPropertyTest extends BaseDynamicPropertyTest<Integer> {
  public IntegerDynamicPropertyTest() {
    super(Integer.MIN_VALUE, Integer.MAX_VALUE, "value", "anotherInt");
  }

  @Override
  Integer getFirst() {
    return q.getValue();
  }

  @Override
  Integer getSecond() {
    return q.getAnotherInt();
  }

  @Override
  void setFirst(Integer o) {
    q.setValue(o);
  }

  @Test
  public void dummy() {
  }
}
