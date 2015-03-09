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
 * Tests for {@code List<String>} properties.
 *
 * @author Oleksandr Kelepko
 */
public class StringListDynamicPropertyTest extends BaseDynamicPropertyTest<List<String>> {
  public StringListDynamicPropertyTest() {
    super(asList("", "2", "3"), asList("4", "5", "6"), "stringList", "anotherStringList");
  }

  @Override
  List<String> getFirst() {
    return q.getStringList();
  }

  @Override
  List<String> getSecond() {
    return q.getAnotherStringList();
  }

  @Override
  void setFirst(List<String> o) {
    q.setStringList(o);
  }

  @Test
  public void dummy() {
  }
}
