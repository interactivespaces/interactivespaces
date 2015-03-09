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

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@code Map&lt;String, List&lt;Integer&gt;&gt;} properties.
 *
 * @author Oleksandr Kelepko
 */
public class IntegerMapDynamicPropertyTest extends BaseDynamicPropertyTest<Map<String, List<Integer>>> {

  public IntegerMapDynamicPropertyTest() {
    super(createFirst(), createSecond(), "mapOfInts", "anotherMapOfInts");
  }

  private static Map<String, List<Integer>> createFirst() {
    Map<String, List<Integer>> result = new HashMap<String, List<Integer>>();
    result.put("one", asList(1, 2, 3));
    result.put("oneMore", asList(2, 3, 4));
    result.put("two", asList(4, 5, 6));
    return result;
  }

  private static Map<String, List<Integer>> createSecond() {
    Map<String, List<Integer>> result = new HashMap<String, List<Integer>>();
    result.put("first", Collections.<Integer>emptyList());
    result.put("second", asList(4, 56));
    result.put("third", asList(7, 89));
    return result;
  }

  @Override
  Map<String, List<Integer>> getFirst() {
    return q.getMapOfInts();
  }

  @Override
  Map<String, List<Integer>> getSecond() {
    return q.getAnotherMapOfInts();
  }

  @Override
  void setFirst(Map<String, List<Integer>> o) {
    q.setMapOfInts(o);
  }

  @Test
  public void dummy() {
  }
}
