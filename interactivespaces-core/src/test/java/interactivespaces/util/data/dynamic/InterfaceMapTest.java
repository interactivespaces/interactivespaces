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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import interactivespaces.util.data.json.JsonMapper;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Dynamic object tests
 *
 * @author Oleksandr Kelepko
 */
public class InterfaceMapTest {

  /**
   * This interface is meant to contain (directly and through inheritance) all kinds of properties a dynamic object
   * supports.
   */
  interface Q extends W {
    List<W> getListOfW();

    // non-interface element type, expected to throw an exception
    List<JsonMapper> getListOfNonInterfaceType();

    Map<String, Object> getMap();

    W getInner();

    void setInner(W inner);

    W getAnotherInner();

    // no property name, expected to throw an exception
    Integer get();

    void setPrimitiveBoolean(boolean d);

    // boolean getters are supported, return false by default
    boolean getPrimitiveBoolean();

    boolean isPrimitiveBoolean();

    // primitive getters throw an exception if there's no value
    void setPrimitiveInt(int d);

    int getPrimitiveInt();

    void setPrimitiveLong(long d);

    long getPrimitiveLong();

    void setPrimitiveDouble(double d);

    double getPrimitiveDouble();

    // not a JavaBean property, expected to throw an exception
    Integer size();

    // not a JavaBean property, expected to throw an exception
    String isNotAJavaBeanProperty();

    // not a JavaBean property, expected to throw an exception
    String setterIsInvalid(String param);

    void setTooManyParameters(String param, String param2);

    void setNoParameters();

    String getTooManyParameters(String param);
  }

  /**
   * This interface here is to test that we support generics.
   */
  interface GenericProperty<T> {
    T getValue();

    void setValue(T value);
  }

  /**
   * This interface contains 'primitive' properties.
   */
  interface W extends GenericProperty<Integer> {

    Integer getAnotherInt();

    Long getLong();

    void setLong(Long l);

    Long getAnotherLong();

    Double getDouble();

    void setDouble(Double d);

    Double getAnotherDouble();

    Number getNumber();

    void setNumber(Number d);

    Number getAnotherNumber();

    Boolean getBoolean();

    void setBoolean(Boolean b);

    Boolean getAnotherBoolean();

    Boolean isTrue();

    String getString();

    void setString(String s);

    CharSequence getAnotherString();

    List<Integer> getIntList();

    void setIntList(List<Integer> list);

    List<Integer> getAnotherIntList();

    List<Long> getLongList();

    void setLongList(List<Long> list);

    List<Long> getAnotherLongList();

    List<Double> getDoubleList();

    void setDoubleList(List<Double> list);

    List<Double> getAnotherDoubleList();

    List<Number> getNumberList();

    void setNumberList(List<Number> list);

    List<Number> getAnotherNumberList();

    List<Boolean> getBooleanList();

    void setBooleanList(List<Boolean> list);

    List<Boolean> getAnotherBooleanList();

    List<String> getStringList();

    void setStringList(List<String> list);

    List<String> getAnotherStringList();
  }

  final HashMap<String, Object> map = new HashMap<String, Object>();
  final Q q = InterfaceMap.createInstance(Q.class, map);

  @Test(expected = IllegalStateException.class)
  public void setBadList_getterThrowsException() {
    try {
      List badList = asList(1, 2, 3);
      q.setBooleanList(badList);
    } catch (Exception e) {
      fail("cannot set bad list");
    }
    q.getBooleanList();
  }

  @Test
  public void isDynamicObject_true() {
    assertTrue(InterfaceMap.isDynamicObject(q));
  }

  @Test
  public void isDynamicObject_null_returnsFalse() {
    assertFalse(InterfaceMap.isDynamicObject(null));
  }

  @Test
  public void isDynamicObject_notProxy_returnsFalse() {
    assertFalse(InterfaceMap.isDynamicObject(new Object()));
  }

  @Test
  public void isDynamicObject_otherProxy_returnsFalse() {
    assertFalse(InterfaceMap.isDynamicObject(Proxy.newProxyInstance(null, new Class[] {},
        new java.lang.reflect.InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
          }
        })));
  }

  @Test
  public void getClass_dynamicObject_ok() {
    assertEquals(InterfaceMap.getClass(q), Q.class);
  }

  @Test(expected = RuntimeException.class)
  public void getClass_notDynamicObject_throwsException() {
    InterfaceMap.getClass(new Object());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void invalidSetter_throwsException() {
    q.setterIsInvalid("hello");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void invalidSetter2_throwsException() {
    q.setTooManyParameters("hello", "world");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void invalidSetter3_throwsException() {
    q.setNoParameters();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void notAGetter_throwsException() {
    q.size();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void notAGetter2_throwsException() {
    q.get();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void notAGetter3_throwsException() {
    q.getTooManyParameters("hello");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void notABooleanGetter_throwsException() {
    q.isNotAJavaBeanProperty();
  }

  @Test(expected = IllegalStateException.class)
  public void getPrimitiveInt_noValue_throwsException() {
    q.getPrimitiveInt();
  }

  @Test(expected = IllegalStateException.class)
  public void getPrimitiveLong_noValue_throwsException() {
    q.getPrimitiveLong();
  }

  @Test(expected = IllegalStateException.class)
  public void getPrimitiveDouble_noValue_throwsException() {
    q.getPrimitiveDouble();
  }

  @Test(expected = IllegalStateException.class)
  public void getListOfNotInterfaceType_throwsException() {
    map.put("listOfNonInterfaceType", new ArrayList<Object>());
    q.getListOfNonInterfaceType();
  }

  @Test
  public void getListOfW_noValue_ok() {
    q.getListOfW();
  }

  @Test
  public void getListOfW_maps_ok() {
    W w1 = InterfaceMap.createInstance(W.class);
    setSomeValues(w1);
    W w2 = InterfaceMap.createInstance(W.class);
    setSomeOtherValues(w2);
    List<Map<String, Object>> list = asList(InterfaceMap.getBackingMap(w1), InterfaceMap.getBackingMap(w2));
    map.put("listOfW", list);

    List<W> actual = q.getListOfW();

    assertEquals(asList(w1, w2), actual);
  }

  @Test
  public void getListOfW_objects_ok() {
    W w1 = InterfaceMap.createInstance(W.class);
    setSomeValues(w1);
    W w2 = InterfaceMap.createInstance(W.class);
    setSomeOtherValues(w2);
    List<W> list = asList(w1, w2);
    map.put("listOfW", list);

    List<W> actual = q.getListOfW();

    assertEquals(asList(w1, w2), actual);
  }

  @Test
  public void getListOfW_mixed_ok() {
    W w1 = InterfaceMap.createInstance(W.class);
    setSomeValues(w1);
    W w2 = InterfaceMap.createInstance(W.class);
    setSomeOtherValues(w2);
    List<Object> list = asList(w1, InterfaceMap.getBackingMap(w2));
    map.put("listOfW", list);

    List<W> actual = q.getListOfW();

    assertEquals(asList(w1, w2), actual);
  }

  @Test(expected = IllegalStateException.class)
  public void getListOfW_mixed_bad() {
    W w1 = InterfaceMap.createInstance(W.class);
    setSomeValues(w1);
    List<Object> list = asList(w1, "hello");
    map.put("listOfW", list);

    List<W> actual = q.getListOfW();

    // Should have thrown since incompatible types in list.
  }

  @Test
  public void getMap_ok() {
    setSomeValues(q);
    Object copy = map.clone();
    Object copy2 = map.clone();
    map.put("map", copy);

    assertEquals(copy2, q.getMap());
  }

  @Test
  public void equals_null_returnsFalse() {
    assertFalse(q.equals(null));
  }

  @Test
  public void equals_same_returnsTrue() {
    assertEquals(q, q);
    setSomeValues(q);
    setSomeOtherValues(q);
    assertEquals(q, q);
  }

  @Test
  public void equals_equal_returnsTrue() {
    setSomeValues(q);
    setSomeOtherValues(q);
    Q other = InterfaceMap.createInstance(Q.class, new HashMap<String, Object>(map));

    assertEquals(q, other);
    assertEquals(other, q);
  }

  @Test
  public void equals_notEqual_returnsFalse() {
    setSomeValues(q);
    Q other = InterfaceMap.createInstance(Q.class, new HashMap<String, Object>(map));
    setSomeOtherValues(other);

    assertFalse(q.equals(other));
    assertFalse(other.equals(q));
  }

  @Test
  public void equals_sameStateDifferentTypes_returnsFalse() {
    W other = InterfaceMap.createInstance(W.class, new HashMap<String, Object>(map));
    assertFalse(q.equals(other));
    assertFalse(other.equals(q));

    setSomeValues(q);
    setSomeValues(other);

    assertFalse(q.equals(other));
    assertFalse(other.equals(q));
  }

  @Test
  public void hashCode_sameAsBackingMap() {
    assertEquals(q.hashCode(), map.hashCode());
    setSomeValues(q);
    setSomeOtherValues(q);
    assertEquals(q.hashCode(), map.hashCode());
    map.clear();
    assertEquals(q.hashCode(), map.hashCode());
  }

  @Test
  public void toString_sameAsBackingMap() {
    assertEquals(q.toString(), map.toString());
    setSomeValues(q);
    assertEquals(q.toString(), map.toString());
    setSomeOtherValues(q);
    assertEquals(q.toString(), map.toString());
    map.clear();
    assertEquals(q.toString(), map.toString());
  }

  @Test
  public void setPrimitive_ok() {
    double d = Double.MAX_VALUE;
    q.setPrimitiveDouble(d);
    assertEquals(d, map.get("primitiveDouble"));
    assertEquals(d, q.getPrimitiveDouble(), Double.MIN_VALUE);

    int i = Integer.MAX_VALUE;
    q.setPrimitiveInt(i);
    assertEquals(i, map.get("primitiveInt"));
    assertEquals(i, q.getPrimitiveInt());

    long l = Long.MAX_VALUE;
    q.setPrimitiveLong(l);
    assertEquals(l, map.get("primitiveLong"));
    assertEquals(l, q.getPrimitiveLong());

    q.setPrimitiveBoolean(false);
    assertEquals(false, map.get("primitiveBoolean"));
    q.setPrimitiveBoolean(true);
    assertEquals(true, map.get("primitiveBoolean"));
  }

  @Test
  public void getPrimitiveBoolean_ok() {
    // precondition
    assertFalse(map.containsKey("primitiveBoolean"));

    // false is the default value
    assertFalse(q.getPrimitiveBoolean());
    assertFalse(q.isPrimitiveBoolean());

    q.setPrimitiveBoolean(false);
    assertFalse(q.getPrimitiveBoolean());
    assertFalse(q.isPrimitiveBoolean());

    q.setPrimitiveBoolean(true);
    assertTrue(q.getPrimitiveBoolean());
    assertTrue(q.isPrimitiveBoolean());
  }

  /**
   * Abstract superclass that test basic operations for different types. Contains tests that are common for all types.
   * Subclasses can add specific tests.
   * <p>
   * State of a dynamic object can be queried and modified in two ways - through the backing map and via setters and
   * getters of the dynamic object. This class tests the basic sequences of setting and getting values both ways.
   *
   * @param <T>
   *          type of a JavaBean property that is tested
   */
  public abstract static class Types<T> {
    /**
     * Map that contains state of a dynamic object. ConcurrentSkipListMap is used because it does not allow null keys or
     * values, and it will throw an exception (ClassCastException) if DynamicObject implementation tries to put there
     * something other than String.
     */
    final Map<String, Object> map = new ConcurrentSkipListMap<String, Object>();

    /**
     * The dynamic object under test.
     */
    final Q q = InterfaceMap.createInstance(Q.class, map);

    Integer intValue = 12345;
    Integer anotherIntValue = 42;

    Long longValue = 0xCAFEBABEL;
    Long anotherLongValue = 0xFEDCBAL;

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
    protected Types(T first, T second, String firstKey, String secondKey) {
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

    @Test(expected = IllegalStateException.class)
    public void invalidValue_getterThrowsException() {
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
      new JsonMapper().toString(map);
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

  public static class Ints extends Types<Integer> {
    public Ints() {
      super(Integer.MIN_VALUE, Integer.MAX_VALUE, "value", "anotherInt");
    }

    @Override
    public void invalidValue_getterThrowsException() {
      // doesn't work with generic return values
      throw new IllegalStateException("ignore the test");
    }

    @Override
    Integer getFirst() {
      return q.getValue();
    }

    @Override
    Integer getSecond() {
      return q.getAnotherInt();
    }

    @Override
    void setFirst(Integer o) {
      q.setValue(o);
    }
  }

  public static class Longs extends Types<Long> {
    public Longs() {
      super(Long.MIN_VALUE, Long.MAX_VALUE, "long", "anotherLong");
    }

    @Override
    Long getFirst() {
      return q.getLong();
    }

    @Override
    Long getSecond() {
      return q.getAnotherLong();
    }

    @Override
    void setFirst(Long o) {
      q.setLong(o);
    }

    @Test
    public void getLong_fromInteger() {
      map.put("long", intValue);
      map.put("anotherLong", anotherIntValue);
      assertEquals(Long.valueOf(intValue.longValue()), q.getLong());
      assertEquals(Long.valueOf(anotherIntValue.longValue()), q.getAnotherLong());
    }
  }

  public static class Numbers extends Types<Number> {
    public Numbers() {
      super(new BigInteger("" + Long.MAX_VALUE), new AtomicInteger(Integer.MIN_VALUE), "number", "anotherNumber");
    }

    @Override
    Number getFirst() {
      return q.getNumber();
    }

    @Override
    Number getSecond() {
      return q.getAnotherNumber();
    }

    @Override
    void setFirst(Number o) {
      q.setNumber(o);
    }
  }

  public static class Doubles extends Types<Double> {
    public Doubles() {
      super(Double.NEGATIVE_INFINITY, Double.NaN, "double", "anotherDouble");
    }

    @Override
    Double getFirst() {
      return q.getDouble();
    }

    @Override
    Double getSecond() {
      return q.getAnotherDouble();
    }

    @Override
    void setFirst(Double o) {
      q.setDouble(o);
    }

    @Test
    public void getDouble_fromInteger() {
      map.put("double", intValue);
      map.put("anotherDouble", anotherIntValue);
      assertEquals(Double.valueOf(intValue.doubleValue()), q.getDouble());
      assertEquals(Double.valueOf(anotherIntValue.doubleValue()), q.getAnotherDouble());
    }

    @Test
    public void getDouble_fromLong() {
      map.put("double", longValue);
      map.put("anotherDouble", anotherLongValue);
      assertEquals(Double.valueOf(longValue.doubleValue()), q.getDouble());
      assertEquals(Double.valueOf(anotherLongValue.doubleValue()), q.getAnotherDouble());
    }
  }

  public static class Booleans extends Types<Boolean> {
    public Booleans() {
      super(true, false, "boolean", "anotherBoolean");
    }

    @Override
    Boolean getFirst() {
      return q.getBoolean();
    }

    @Override
    Boolean getSecond() {
      return q.getAnotherBoolean();
    }

    @Override
    void setFirst(Boolean o) {
      q.setBoolean(o);
    }

    @Test
    public void getterThatStartsWithIs() {
      map.put("true", first);
      assertEquals(first, q.isTrue());
    }
  }

  public static class Strings extends Types<String> {
    public Strings() {
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
  }

  public static class IntegerLists extends Types<List<Integer>> {
    public IntegerLists() {
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
  }

  public static class LongLists extends Types<List<Long>> {
    public LongLists() {
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

  public static class DoubleLists extends Types<List<Double>> {
    public DoubleLists() {
      super(asList(Double.NaN, 2.0, 3.0), asList(4.0, 5.0, 6.0), "doubleList", "anotherDoubleList");
    }

    @Override
    List<Double> getFirst() {
      return q.getDoubleList();
    }

    @Override
    List<Double> getSecond() {
      return q.getAnotherDoubleList();
    }

    @Override
    void setFirst(List<Double> o) {
      q.setDoubleList(o);
    }

    @Test
    public void getDoubles_fromStrings() {
      List<String> strings = asList("NaN", "Infinity", "-Infinity");
      List<Double> doubles = asList(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
      map.put(firstKey, strings);
      assertEquals(doubles, getFirst());
    }

    @Test
    public void getDoubles_fromIntegers() {
      map.put(firstKey, asList(1, 2, 3, 4, 5));
      assertEquals(asList(1.0, 2.0, 3.0, 4.0, 5.0), getFirst());
    }

    @Test
    public void getDoubles_fromLongs() {
      map.put(firstKey, asList(1L, 2L, 3L, 4L, 5L));
      assertEquals(asList(1.0, 2.0, 3.0, 4.0, 5.0), getFirst());
    }

    @Test
    public void getDoubles_fromIntegersAndLongs() {
      map.put(firstKey, asList(1L, 2, 3L, 4, 5L));
      assertEquals(asList(1.0, 2.0, 3.0, 4.0, 5.0), getFirst());
    }
  }

  public static class NumberLists extends Types<List<Number>> {
    public NumberLists() {
      super(makeList(1.0, 2, 3L), makeList(new AtomicInteger(42), new AtomicLong(123L), new BigInteger("100500")),
          "numberList", "anotherNumberList");
    }

    private static List<Number> makeList(Number... numbers) {
      return asList(numbers);
    }

    @Override
    List<Number> getFirst() {
      return q.getNumberList();
    }

    @Override
    List<Number> getSecond() {
      return q.getAnotherNumberList();
    }

    @Override
    void setFirst(List<Number> o) {
      q.setNumberList(o);
    }
  }

  public static class BooleanLists extends Types<List<Boolean>> {
    public BooleanLists() {
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
  }

  public static class StringLists extends Types<List<String>> {
    public StringLists() {
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
  }

  public static class InnerObjects extends Types<W> {
    private static Map<String, Object> firstMap;
    private static Map<String, Object> secondMap;

    public InnerObjects() {
      super(createFirst(), createSecond(), "inner", "anotherInner");
    }

    private static W createFirst() {
      firstMap = new ConcurrentSkipListMap<String, Object>();
      W w = InterfaceMap.createInstance(W.class, firstMap);
      setSomeValues(w);
      return w;
    }

    private static W createSecond() {
      secondMap = new ConcurrentSkipListMap<String, Object>();
      W w = InterfaceMap.createInstance(W.class, secondMap);
      setSomeOtherValues(w);
      return w;
    }

    @Override
    W getFirst() {
      return q.getInner();
    }

    @Override
    W getSecond() {
      return q.getAnotherInner();
    }

    @Override
    void setFirst(W o) {
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
      W instance = InterfaceMap.createInstance(W.class, (Map) map.get(firstKey));
      assertEquals(first, instance);
      assertEquals(InterfaceMap.getBackingMap(q).get(firstKey), firstMap);
    }
  }

  static void setSomeOtherValues(W w) {
    w.setStringList(asList("", "some string", "2b||!2b"));
    w.setBooleanList(asList(false, true));
    w.setLongList(asList(0xcafebabeL, Long.MAX_VALUE));
    w.setDouble(Math.PI);
    w.setStringList(asList("one", "two", "three"));
  }

  static void setSomeValues(W w) {
    w.setString("some string");
    w.setBoolean(false);
    w.setLong(0xcafebabeL);
    w.setDouble(Math.PI);
    w.setStringList(asList("one", "two", "three"));
  }
}
