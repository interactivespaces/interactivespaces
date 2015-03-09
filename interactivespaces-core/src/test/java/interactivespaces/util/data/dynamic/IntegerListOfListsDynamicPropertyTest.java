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

import java.util.List;

/**
 * Tests for {@code List&lt;List&lt;Integer&gt;&gt;} properties.
 *
 * @author Oleksandr Kelepko
 */
public class IntegerListOfListsDynamicPropertyTest extends BaseDynamicPropertyTest<List<List<Integer>>> {
  public IntegerListOfListsDynamicPropertyTest() {
    super(createFirst(), createSecond(), "intLists", "anotherIntLists");
  }

  private static List<List<Integer>> createSecond() {
    return asList(asList(4, 5, 6), asList(0));
  }

  private static List<List<Integer>> createFirst() {
    return asList(asList(1, 2, 3), asList(6, 5, 4));
  }

  @Override
  List<List<Integer>> getFirst() {
    return q.getIntLists();
  }

  @Override
  List<List<Integer>> getSecond() {
    return q.getAnotherIntLists();
  }

  @Override
  void setFirst(List<List<Integer>> o) {
    q.setIntLists(o);
  }

  @Test
  public void dummy() {
  }
}
