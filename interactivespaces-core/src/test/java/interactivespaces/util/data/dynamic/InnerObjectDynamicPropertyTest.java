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

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Tests for complex dynamic object properties.
 *
 * @author Oleksandr Kelepko
 */
public class InnerObjectDynamicPropertyTest extends BaseDynamicPropertyTest<JavaBeanProperties> {
  private static Map<String, Object> firstMap;

  private static Map<String, Object> secondMap;

  public InnerObjectDynamicPropertyTest() {
    super(createFirst(), createSecond(), "inner", "anotherInner");
  }

  private static JavaBeanProperties createFirst() {
    firstMap = new ConcurrentSkipListMap<String, Object>();
    JavaBeanProperties w = InterfaceMap.createInstance(JavaBeanProperties.class, firstMap);
    InterfaceMapTest.setSomeValues(w);
    return w;
  }

  private static JavaBeanProperties createSecond() {
    secondMap = new ConcurrentSkipListMap<String, Object>();
    JavaBeanProperties w = InterfaceMap.createInstance(JavaBeanProperties.class, secondMap);
    InterfaceMapTest.setSomeOtherValues(w);
    return w;
  }

  @Override
  JavaBeanProperties getFirst() {
    return q.getInner();
  }

  @Override
  JavaBeanProperties getSecond() {
    return q.getAnotherInner();
  }

  @Override
  void setFirst(JavaBeanProperties o) {
    q.setInner(o);
  }

  @Override
  public void whatYouSetBothWaysIsReflectedByGetter() {
    setFirst(first);
    map.put(secondKey, secondMap);
    assertEquals(first, getFirst());
    assertEquals(second, getSecond());
  }

  @Override
  public void whatYouPutIntoMapIsReflectedByGetter_notNull() {
    map.put(firstKey, firstMap);
    map.put(secondKey, secondMap);
    assertEquals(first, getFirst());
    assertEquals(second, getSecond());
  }

  @Override
  public void whatYouSetInSetterIsReflectedInBackingMap_notNull() {
    setFirst(first);
    JavaBeanProperties instance = InterfaceMap.createInstance(JavaBeanProperties.class, (Map) map.get(firstKey));
    assertEquals(first, instance);
    assertEquals(InterfaceMap.getBackingMap(q).get(firstKey), firstMap);
  }

  @Test
  public void dummy() {
  }
}
