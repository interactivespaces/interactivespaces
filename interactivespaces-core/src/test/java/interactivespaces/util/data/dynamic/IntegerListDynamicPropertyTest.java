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
 * Tests for {@code List<Integer>} properties.
 *
 * @author Oleksandr Kelepko
 */
public class IntegerListDynamicPropertyTest extends BaseDynamicPropertyTest<List<Integer>> {
  public IntegerListDynamicPropertyTest() {
    super(asList(1, 2, 3), asList(4, 5, 6), "intList", "anotherIntList");
  }

  @Override
  List<Integer> getFirst() {
    return q.getIntList();
  }

  @Override
  List<Integer> getSecond() {
    return q.getAnotherIntList();
  }

  @Override
  void setFirst(List<Integer> o) {
    q.setIntList(o);
  }

  @Test
  public void dummy() {
  }
}
