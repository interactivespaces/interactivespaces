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
 * Tests for {@code Double} properties.
 *
 * @author Oleksandr Kelepko
 */
public class DoubleDynamicPropertyTest extends BaseDynamicPropertyTest<Double> {

  public DoubleDynamicPropertyTest() {
    super(Double.NEGATIVE_INFINITY, Double.NaN, "double", "anotherDouble");
  }

  @Override
  Double getFirst() {
    return q.getDouble();
  }

  @Override
  Double getSecond() {
    return q.getAnotherDouble();
  }

  @Override
  void setFirst(Double o) {
    q.setDouble(o);
  }

  @Test
  public void getDouble_fromInteger() {
    Integer intValue = 12345;
    Integer anotherIntValue = 42;
    map.put("double", intValue);
    map.put("anotherDouble", anotherIntValue);
    assertEquals(Double.valueOf(intValue.doubleValue()), q.getDouble());
    assertEquals(Double.valueOf(anotherIntValue.doubleValue()), q.getAnotherDouble());
  }

  @Test
  public void getDouble_fromLong() {
    Long longValue = 0xCAFEBABEL;
    Long anotherLongValue = 0xFEDCBAL;
    map.put("double", longValue);
    map.put("anotherDouble", anotherLongValue);
    assertEquals(Double.valueOf(longValue.doubleValue()), q.getDouble());
    assertEquals(Double.valueOf(anotherLongValue.doubleValue()), q.getAnotherDouble());
  }
}
