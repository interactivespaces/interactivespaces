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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for complex {@code Map} properties.
 *
 * @author Oleksandr Kelepko
 */
public class InnerObjectMapDynamicPropertyTest extends BaseDynamicPropertyTest<Map<String, JavaBeanProperties>> {
  static JavaBeanProperties w1 = InterfaceMap.createInstance(JavaBeanProperties.class);

  static JavaBeanProperties w2 = InterfaceMap.createInstance(JavaBeanProperties.class);

  static JavaBeanProperties w3 = InterfaceMap.createInstance(JavaBeanProperties.class);

  static {
    InterfaceMapTest.setSomeValues(w1);

    InterfaceMapTest.setSomeOtherValues(w2);

    InterfaceMapTest.setSomeValues(w3);
    InterfaceMapTest.setSomeOtherValues(w3);
  }

  public InnerObjectMapDynamicPropertyTest() {
    super(createFirst(), createSecond(), "mapOfInner", "anotherMapOfInner");
  }

  private static Map<String, JavaBeanProperties> createFirst() {
    Map<String, JavaBeanProperties> result = new HashMap<String, JavaBeanProperties>();
    result.put("one", w1);
    // Test to ensure null is a valid value.
    result.put("oneMore", null);
    result.put("two", w2);
    return result;
  }

  private static Map<String, JavaBeanProperties> createSecond() {
    Map<String, JavaBeanProperties> result = new HashMap<String, JavaBeanProperties>();
    result.put("first", w1);
    result.put("second", w2);
    result.put("third", w3);
    return result;
  }

  @Override
  Map<String, JavaBeanProperties> getFirst() {
    return q.getMapOfInner();
  }

  @Override
  Map<String, JavaBeanProperties> getSecond() {
    return q.getAnotherMapOfInner();
  }

  @Override
  void setFirst(Map<String, JavaBeanProperties> o) {
    q.setMapOfInner(o);
  }

  @Override
  public void whatYouSetBothWaysIsReflectedByGetter() {
    Map<String, Map> secondMap = getSecondMap();

    setFirst(first);
    map.put(secondKey, secondMap);
    assertEquals(first, getFirst());
    assertEquals(second, getSecond());
  }

  @Override
  public void whatYouPutIntoMapIsReflectedByGetter_notNull() {
    map.put(firstKey, getFirstMap());
    map.put(secondKey, getSecondMap());
    assertEquals(first, getFirst());
    assertEquals(second, getSecond());
  }

  @Test
  public void dummy() {
  }

  private static Map<String, Map> getFirstMap() {
    Map<String, Map> secondMap = new HashMap<String, Map>();
    secondMap.put("one", InterfaceMap.getBackingMap(w1));
    secondMap.put("oneMore", null);
    secondMap.put("two", InterfaceMap.getBackingMap(w2));
    return secondMap;
  }

  private static Map<String, Map> getSecondMap() {
    Map<String, Map> secondMap = new HashMap<String, Map>();
    secondMap.put("first", InterfaceMap.getBackingMap(w1));
    secondMap.put("second", InterfaceMap.getBackingMap(w2));
    secondMap.put("third", InterfaceMap.getBackingMap(w3));
    return secondMap;
  }
}
