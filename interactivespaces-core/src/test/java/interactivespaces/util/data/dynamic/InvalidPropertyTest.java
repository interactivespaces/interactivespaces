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

import java.util.Collections;
import java.util.HashMap;

/**
 * Tests for unsupported signatures.
 *
 * @author Oleksandr Kelepko
 */
public class InvalidPropertyTest {
  final HashMap<String, Object> map = new HashMap<String, Object>();

  final PossibleProperties q = InterfaceMap.createInstance(PossibleProperties.class, map);


  @Test(expected = UnsupportedOperationException.class)
  public void setterReturnTypeIsNotVoid_throwsException() {
    q.setterIsInvalid("hello");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void setterWithTooManyParameters_throwsException() {
    q.setTooManyParameters("hello", "world");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void setterWithNoParameters_throwsException() {
    q.setNoParameters();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void doesNotStartWithIsGetSet_throwsException() {
    q.size();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void noPropertyName_throwsException() {
    q.get();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void voidReturnType_throwsException() {
    q.getVoid();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void nativeVoidReturnType_throwsException() {
    q.getNativeVoid();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void tooManyParameters_throwsException() {
    q.getTooManyParameters("hello");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void startsWithIsButDoesNotReturnBoolean_throwsException() {
    q.isNotAJavaBeanProperty();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void mapWithWildCards_throwsException() {
    map.put("mapWithWildcards", Collections.emptyMap());
    q.getMapWithWildcards();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void mapWithNonStringKeys_throwsException() {
    map.put("mapWithNonStringKeys", Collections.emptyMap());
    q.getMapWithNonStringKeys();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void rawMap_throwsException() {
    map.put("rawMap", Collections.emptyMap());
    q.getRawMap();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void listWithWildCards_throwsException() {
    map.put("listWithWildcards", Collections.emptyList());
    q.getListWithWildcards();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void rawList_throwsException() {
    map.put("rawList", Collections.emptyList());
    q.getRawList();
  }
}
