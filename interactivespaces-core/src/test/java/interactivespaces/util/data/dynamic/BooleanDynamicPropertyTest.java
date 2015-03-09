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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for {@code Boolean} properties.
 *
 * @author Oleksandr Kelepko
 */
public class BooleanDynamicPropertyTest extends BaseDynamicPropertyTest<Boolean> {
  public BooleanDynamicPropertyTest() {
    super(true, false, "boolean", "anotherBoolean");
  }

  @Override
  Boolean getFirst() {
    return q.getBoolean();
  }

  @Override
  Boolean getSecond() {
    return q.getAnotherBoolean();
  }

  @Override
  void setFirst(Boolean o) {
    q.setBoolean(o);
  }

  @Test
  public void getterThatStartsWithIs() {
    map.put("true", first);
    assertEquals(first, q.isTrue());
  }

}
