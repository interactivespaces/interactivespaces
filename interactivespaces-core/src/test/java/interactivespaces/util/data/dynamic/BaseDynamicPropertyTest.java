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
import static org.junit.Assert.assertNull;

import interactivespaces.util.data.json.StandardJsonMapper;

import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Abstract superclass that test basic operations for different types. Contains tests that are common for all types.
 * Subclasses can add specific tests.
 * <p>
 * State of a dynamic object can be queried and modified in two ways - through the backing map and via setters and
 * getters of the dynamic object. This class tests the basic sequences of setting and getting values both ways.
 *
 * @param <T>
 *     type of a JavaBean property that is tested
 *
 * @author Oleksandr Kelepko
 */
public abstract class BaseDynamicPropertyTest<T> {
  /**
   * Map that contains state of a dynamic object. ConcurrentSkipListMap is used because it does not allow null keys or
   * values, and it will throw an exception (ClassCastException) if DynamicObject implementation tries to put there
   * something other than String.
   */
  final Map<String, Object> map = new ConcurrentSkipListMap<String, Object>();

  /**
   * The dynamic object under test.
   */
  final PossibleProperties q = InterfaceMap.createInstance(PossibleProperties.class, map);

  /**
   * A value that will be set in the dynamic object.
   */
  final T first;

  /**
   * Another value that will be set in the dynamic object.
   */
  final T second;

  /**
   * Key in the backing map that corresponds to the JavaBean property name for the first value.
   */
  final String firstKey;

  /**
   * Key in the backing map that corresponds to the JavaBean property name for the second value.
   */
  final String secondKey;

  /**
   * Constructor.
   */
  protected BaseDynamicPropertyTest(T first, T second, String firstKey, String secondKey) {
    this.first = first;
    this.second = second;
    this.firstKey = firstKey;
    this.secondKey = secondKey;
  }

  /**
   * Invokes a getter on the dynamic object.
   */
  abstract T getFirst();

  /**
   * Invokes another getter on the dynamic object.
   */
  abstract T getSecond();

  /**
   * Invokes a setter on the dynamic object.
   */
  abstract void setFirst(T o);

  @Test(expected = ClassCastException.class)
  public void whenInvalidValueIsSetGetterThrowsException() {
    map.put(firstKey, new Object());
    getFirst();
  }

  @Test
  public void whenNoValueIsSetGetterReturnsNull() {
    assertNull(getFirst());
  }

  @Test
  public void serialize_noExceptions() {
    setFirst(first);
    map.put(secondKey, second);
    new StandardJsonMapper().toString(map);
  }

  @Test
  public void whatYouSetInSetterIsReflectedByGetter_notNull() {
    setFirst(first);
    assertEquals(first, getFirst());
    assertNull(getSecond());
  }

  @Test
  public void whatYouSetInSetterIsReflectedInBackingMap_notNull() {
    setFirst(first);
    assertEquals(first, map.get(firstKey));
  }

  @Test
  public void whatYouSetInSetterIsReflectedByGetter_null() {
    map.put(firstKey, first);
    setFirst(null);
    assertNull(getFirst());
  }

  @Test
  public void whatYouSetInSetterIsReflectedInBackingMap_null() {
    map.put(firstKey, first);
    setFirst(null);
    assertNull(map.get(firstKey));
  }

  @Test
  public void whatYouPutIntoMapIsReflectedByGetter_notNull() {
    map.put(firstKey, first);
    map.put(secondKey, second);
    assertEquals(first, getFirst());
    assertEquals(second, getSecond());
  }

  @Test
  public void whatYouPutIntoMapIsReflectedByGetter_null() {
    setFirst(first);
    map.remove(firstKey);
    assertNull(getFirst());
    assertNull(getSecond());
  }

  @Test
  public void whatYouSetBothWaysIsReflectedByGetter() {
    setFirst(first);
    map.put(secondKey, second);
    assertEquals(first, getFirst());
    assertEquals(second, getSecond());
  }
}
