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
 * Tests for {@code Long} properties.
 *
 * @author Oleksandr Kelepko
 */
public class LongDynamicPropertyTest extends BaseDynamicPropertyTest<Long> {
  public LongDynamicPropertyTest() {
    super(Long.MIN_VALUE, Long.MAX_VALUE, "long", "anotherLong");
  }

  @Override
  Long getFirst() {
    return q.getLong();
  }

  @Override
  Long getSecond() {
    return q.getAnotherLong();
  }

  @Override
  void setFirst(Long o) {
    q.setLong(o);
  }

  @Test
  public void getLong_fromInteger() {
    Integer intValue = 123;
    Integer anotherIntValue = 321;
    map.put(firstKey, intValue);
    map.put(secondKey, anotherIntValue);
    assertEquals(Long.valueOf(intValue.longValue()), getFirst());
    assertEquals(Long.valueOf(anotherIntValue.longValue()), getSecond());
  }
}
