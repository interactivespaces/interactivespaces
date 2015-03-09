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

import org.junit.Test;

/**
 * Tests for {@code String} properties.
 *
 * @author Oleksandr Kelepko
 */
public class StringDynamicPropertyTest extends BaseDynamicPropertyTest<String> {
  public StringDynamicPropertyTest() {
    super("hello", "IS", "string", "anotherString");
  }

  @Override
  String getFirst() {
    return q.getString();
  }

  @Override
  String getSecond() {
    return (String) q.getAnotherString();
  }

  @Override
  void setFirst(String o) {
    q.setString(o);
  }

  @Test
  public void dummy() {
  }
}
