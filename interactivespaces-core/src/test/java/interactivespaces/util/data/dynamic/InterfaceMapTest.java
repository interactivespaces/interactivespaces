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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dynamic object tests
 *
 * @author Oleksandr Kelepko
 */
public class InterfaceMapTest {

  final HashMap<String, Object> map = new HashMap<String, Object>();
  final PossibleProperties q = InterfaceMap.createInstance(PossibleProperties.class, map);

  @Test(expected = ClassCastException.class)
  public void setBadList_getterThrowsException() {
    try {
      List badList = asList(1, 2, 3);
      q.setBooleanList(badList);
    } catch (Exception e) {
      fail("cannot set bad list");
    }
    List<Boolean> booleanList = q.getBooleanList();
    for (Boolean aBoolean : booleanList) {
      // Should throw ClassCastException due to incompatible types.
    }
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
    assertEquals(InterfaceMap.getClass(q), PossibleProperties.class);
  }

  @Test(expected = RuntimeException.class)
  public void getClass_notDynamicObject_throwsException() {
    InterfaceMap.getClass(new Object());
  }

  @Test(expected = NullPointerException.class)
  public void getPrimitiveInt_noValue_throwsException() {
    q.getPrimitiveInt();
  }

  @Test(expected = NullPointerException.class)
  public void getPrimitiveLong_noValue_throwsException() {
    q.getPrimitiveLong();
  }

  @Test(expected = NullPointerException.class)
  public void getPrimitiveDouble_noValue_throwsException() {
    q.getPrimitiveDouble();
  }

  @Test(expected = UnsupportedOperationException.class)
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
    JavaBeanProperties w1 = InterfaceMap.createInstance(JavaBeanProperties.class);
    setSomeValues(w1);
    JavaBeanProperties w2 = InterfaceMap.createInstance(JavaBeanProperties.class);
    setSomeOtherValues(w2);
    List<Map<String, Object>> list = asList(InterfaceMap.getBackingMap(w1), InterfaceMap.getBackingMap(w2));
    map.put("listOfW", list);

    List<JavaBeanProperties> actual = q.getListOfW();

    assertEquals(asList(w1, w2), actual);
  }

  @Test
  public void getListOfW_objects_ok() {
    JavaBeanProperties w1 = InterfaceMap.createInstance(JavaBeanProperties.class);
    setSomeValues(w1);
    JavaBeanProperties w2 = InterfaceMap.createInstance(JavaBeanProperties.class);
    setSomeOtherValues(w2);
    List<JavaBeanProperties> list = asList(w1, w2);
    map.put("listOfW", list);

    List<JavaBeanProperties> actual = q.getListOfW();

    assertEquals(asList(w1, w2), actual);
  }

  @Test
  public void getListOfW_mixed_ok() {
    JavaBeanProperties w1 = InterfaceMap.createInstance(JavaBeanProperties.class);
    setSomeValues(w1);
    JavaBeanProperties w2 = InterfaceMap.createInstance(JavaBeanProperties.class);
    setSomeOtherValues(w2);
    List<Object> list = asList(w1, InterfaceMap.getBackingMap(w2));
    map.put("listOfW", list);

    List<JavaBeanProperties> actual = q.getListOfW();

    assertEquals(asList(w1, w2), actual);
  }

  @Test(expected = ClassCastException.class)
  public void getListOfW_mixed_bad() {
    JavaBeanProperties w1 = InterfaceMap.createInstance(JavaBeanProperties.class);
    setSomeValues(w1);
    List<Object> list = asList(w1, "hello");
    map.put("listOfW", list);

    List<JavaBeanProperties> listOfW = q.getListOfW();
    for (JavaBeanProperties javaBeanProperties : listOfW) {
      // Should throw ClassCastException due to incompatible types.
    }
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
    PossibleProperties other = InterfaceMap.createInstance(PossibleProperties.class, new HashMap<String, Object>(map));

    assertEquals(q, other);
    assertEquals(other, q);
  }

  @Test
  public void equals_notEqual_returnsFalse() {
    setSomeValues(q);
    PossibleProperties other = InterfaceMap.createInstance(PossibleProperties.class, new HashMap<String, Object>(map));
    setSomeOtherValues(other);

    assertFalse(q.equals(other));
    assertFalse(other.equals(q));
  }

  @Test
  public void equals_sameStateDifferentTypes_returnsFalse() {
    JavaBeanProperties other = InterfaceMap.createInstance(JavaBeanProperties.class, new HashMap<String, Object>(map));
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
    assertFalse(q.isPrimitiveBoolean());
    q.setPrimitiveBoolean(false);
    assertFalse(q.isPrimitiveBoolean());
    q.setPrimitiveBoolean(true);
    assertTrue(q.isPrimitiveBoolean());

    assertFalse(q.getAnotherPrimitiveBoolean());
    map.put("anotherPrimitiveBoolean", true);
    assertTrue(q.getAnotherPrimitiveBoolean());
  }

  static void setSomeOtherValues(JavaBeanProperties w) {
    w.setStringList(asList("", "some string", "2b||!2b"));
    w.setBooleanList(asList(false, true));
    w.setLongList(asList(0xcafebabeL, Long.MAX_VALUE));
    w.setDouble(Math.PI);
    w.setStringList(asList("one", "two", "three"));
  }

  static void setSomeValues(JavaBeanProperties w) {
    w.setString("some string");
    w.setBoolean(false);
    w.setLong(0xcafebabeL);
    w.setDouble(Math.PI);
    w.setStringList(asList("one", "two", "three"));
  }
}
