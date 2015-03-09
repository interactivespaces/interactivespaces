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
 * Tests for {@code List<Long>} properties.
 *
 * @author Oleksandr Kelepko
 */
public class LongListDynamicPropertyTest extends BaseDynamicPropertyTest<List<Long>> {
  public LongListDynamicPropertyTest() {
    super(asList(1L, 2L, 3L), asList(4L, 5L, 6L), "longList", "anotherLongList");
  }

  @Override
  List<Long> getFirst() {
    return q.getLongList();
  }

  @Override
  List<Long> getSecond() {
    return q.getAnotherLongList();
  }

  @Override
  void setFirst(List<Long> o) {
    q.setLongList(o);
  }

  @Test
  public void getLongs_fromIntegers() {
    map.put(firstKey, asList(1, 2, 3, 4, 5));
    assertEquals(asList(1L, 2L, 3L, 4L, 5L), getFirst());
  }
}
