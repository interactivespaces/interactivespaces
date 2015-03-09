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
 * Tests for {@code List<Double>} properties.
 *
 * @author Oleksandr Kelepko
 */
public class DoubleListDynamicPropertyTest extends BaseDynamicPropertyTest<List<Double>> {

  public DoubleListDynamicPropertyTest() {
    super(asList(Double.NaN, 2.0, 3.0), asList(4.0, 5.0, 6.0), "doubleList", "anotherDoubleList");
  }

  @Override
  List<Double> getFirst() {
    return q.getDoubleList();
  }

  @Override
  List<Double> getSecond() {
    return q.getAnotherDoubleList();
  }

  @Override
  void setFirst(List<Double> o) {
    q.setDoubleList(o);
  }

  @Test
  public void getDoubles_fromStrings() {
    List<String> strings = asList("NaN", "Infinity", "-Infinity");
    List<Double> doubles = asList(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    map.put(firstKey, strings);
    assertEquals(doubles, getFirst());
  }

  @Test
  public void getDoubles_fromIntegers() {
    map.put(firstKey, asList(1, 2, 3, 4, 5));
    assertEquals(asList(1.0, 2.0, 3.0, 4.0, 5.0), getFirst());
  }

  @Test
  public void getDoubles_fromLongs() {
    map.put(firstKey, asList(1L, 2L, 3L, 4L, 5L));
    assertEquals(asList(1.0, 2.0, 3.0, 4.0, 5.0), getFirst());
  }

  @Test
  public void getDoubles_fromIntegersAndLongs() {
    map.put(firstKey, asList(1L, 2, 3L, 4, 5L));
    assertEquals(asList(1.0, 2.0, 3.0, 4.0, 5.0), getFirst());
  }
}
