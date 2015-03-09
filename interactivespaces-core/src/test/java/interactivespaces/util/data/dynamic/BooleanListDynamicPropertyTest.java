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
 * Tests for {@code List<Boolean>} properties.
 *
 * @author Oleksandr Kelepko
 */
public class BooleanListDynamicPropertyTest extends BaseDynamicPropertyTest<List<Boolean>> {
  public BooleanListDynamicPropertyTest() {
    super(asList(true, false), asList(true, true, true), "booleanList", "anotherBooleanList");
  }

  @Override
  List<Boolean> getFirst() {
    return q.getBooleanList();
  }

  @Override
  List<Boolean> getSecond() {
    return q.getAnotherBooleanList();
  }

  @Override
  void setFirst(List<Boolean> o) {
    q.setBooleanList(o);
  }

  @Test
  public void dummy() {
  }
}
