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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

/**
 * Tests for {@code List<Float>} properties.
 *
 * @author Oleksandr Kelepko
 */
public class FloatListDynamicPropertyTest extends BaseDynamicPropertyTest<List<Float>> {

  public FloatListDynamicPropertyTest() {
    super(asList(Float.NaN, 2.0f, 3.0f), asList(4.0f, 5.0f, 6.0f), "floatList", "anotherFloatList");
  }

  @Override
  List<Float> getFirst() {
    return q.getFloatList();
  }

  @Override
  List<Float> getSecond() {
    return q.getAnotherFloatList();
  }

  @Override
  void setFirst(List<Float> o) {
    q.setFloatList(o);
  }

  @Test
  public void getFloats_fromStrings() {
    List<String> strings = asList("NaN", "Infinity", "-Infinity");
    List<Float> floats = asList(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY);
    map.put(firstKey, strings);
    assertEquals(floats, getFirst());
  }

  @Test
  public void getFloats_fromIntegers() {
    map.put(firstKey, asList(1, 2, 3, 4, 5));
    assertEquals(asList(1.0f, 2.0f, 3.0f, 4.0f, 5.0f), getFirst());
  }

  @Test
  public void getFloats_fromLongs() {
    map.put(firstKey, asList(1L, 2L, 3L, 4L, 5L));
    assertEquals(asList(1.0f, 2.0f, 3.0f, 4.0f, 5.0f), getFirst());
  }

  @Test
  public void getFloats_fromIntegersAndLongs() {
    map.put(firstKey, asList(1L, 2, 3L, 4, 5L));
    assertEquals(asList(1.0f, 2.0f, 3.0f, 4.0f, 5.0f), getFirst());
  }
}
