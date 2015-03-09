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
 * Tests for {@code Float} properties.
 *
 * @author Oleksandr Kelepko
 */
public class FloatDynamicPropertyTest extends BaseDynamicPropertyTest<Float> {

  public FloatDynamicPropertyTest() {
    super(Float.NEGATIVE_INFINITY, Float.NaN, "float", "anotherFloat");
  }

  @Override
  Float getFirst() {
    return q.getFloat();
  }

  @Override
  Float getSecond() {
    return q.getAnotherFloat();
  }

  @Override
  void setFirst(Float o) {
    q.setFloat(o);
  }

  @Test
  public void getFloat_fromInteger() {
    Integer intValue = 12345;
    Integer anotherIntValue = 42;
    map.put("float", intValue);
    map.put("anotherFloat", anotherIntValue);
    assertEquals(Float.valueOf(intValue.floatValue()), q.getFloat());
    assertEquals(Float.valueOf(anotherIntValue.floatValue()), q.getAnotherFloat());
  }

  @Test
  public void getFloat_fromLong() {
    Long longValue = 0xCAFEBABEL;
    Long anotherLongValue = 0xFEDCBAL;
    map.put("float", longValue);
    map.put("anotherFloat", anotherLongValue);
    assertEquals(Float.valueOf(longValue.floatValue()), q.getFloat());
    assertEquals(Float.valueOf(anotherLongValue.floatValue()), q.getAnotherFloat());
  }
}
