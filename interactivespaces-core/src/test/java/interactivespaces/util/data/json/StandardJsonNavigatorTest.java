/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.util.data.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test the {@link JsonNavigator}.
 *
 * @author Keith M. Hughes
 */
public class StandardJsonNavigatorTest {

  /**
   * Make sure primitives are properly handled.
   */
  @Test
  public void testPrimitives() {
    Map<String, Object> root = Maps.newHashMap();

    String keyString1 = "string1";
    String valueString1 = "bar";
    root.put(keyString1, valueString1);

    String keyString2 = "string1";
    String valueString2 = "bar";
    root.put(keyString2, valueString2);

    String keyInteger = "integer";
    int valueInteger = 1;
    root.put(keyInteger, valueInteger);

    String keyDouble = "double";
    double valueDouble = 1.23456;
    root.put(keyDouble, valueDouble);

    String keyBoolean = "boolean";
    boolean valueBoolean = true;
    root.put(keyBoolean, valueBoolean);

    Set<String> properties =
        Sets.newHashSet(keyString1, keyString1, keyInteger, keyDouble, keyBoolean);

    JsonNavigator nav = new StandardJsonNavigator(root);

    assertEquals(properties, nav.getProperties());
    assertEquals(valueString1, nav.getString(keyString1));
    assertEquals(valueString2, nav.getString(keyString2));
    assertEquals((Integer) valueInteger, nav.getInteger(keyInteger));
    assertEquals((Double) valueDouble, nav.getDouble(keyDouble));
    assertEquals(valueBoolean, nav.getBoolean(keyBoolean));

    assertEquals(Double.valueOf(valueInteger), nav.getDouble(keyInteger));
  }

  /**
   * Test making a series of maps and see if can properly navigate through them.
   */
  @Test
  public void testDescent() {
    Map<String, Object> root = Maps.newHashMap();

    Map<String, Object> obj1 = Maps.newHashMap();
    String obj1Key = "foo";
    root.put(obj1Key, obj1);

    String obj1ValueKey1 = "bar";
    String obj1Value1 = "bletch";

    obj1.put(obj1ValueKey1, obj1Value1);

    String obj1ValueKey2 = "bar2";
    String obj1Value2 = "bletch2";

    obj1.put(obj1ValueKey2, obj1Value2);

    Map<String, Object> obj2 = Maps.newHashMap();
    String obj2Key = "banana";
    obj1.put(obj2Key, obj2);

    String obj2ValueKey = "orange";
    String obj2Value = "apple";

    obj2.put(obj2ValueKey, obj2Value);

    List<Object> array = Lists.newArrayList();
    array.add(17);
    array.add("glork");

    String arrayKey = "array";
    root.put(arrayKey, array);

    JsonNavigator nav = new StandardJsonNavigator(root);

    assertEquals(array, nav.getItem(arrayKey));

    nav.down(obj1Key);

    assertEquals(obj1Value1, nav.getItem(obj1ValueKey1));
    assertEquals(obj2, nav.getItem(obj2Key));

    nav.down(obj2Key);

    assertEquals(obj2Value, nav.getItem(obj2ValueKey));

    nav.up();

    assertEquals(obj1Value2, nav.getItem(obj1ValueKey2));

    nav.up();

    // Should be at root.
    try {
      nav.up();

      fail();
    } catch (JsonInteractiveSpacesException e) {
      // Expected
    }
  }

  /**
   * Make sure primitives are properly handled.
   */
  @Test
  public void testArrayPrimitives() {
    Map<String, Object> root = Maps.newHashMap();

    String keyString = "test";
    List<Object> array = Lists.newArrayList();
    root.put(keyString, array);

    String objectValue1 = "bar";
    String arrayValueString = objectValue1;
    array.add(arrayValueString);

    int arrayValueInteger = 1234;
    array.add(arrayValueInteger);

    double arrayValueDouble = 1.23456;
    array.add(arrayValueDouble);

    boolean arrayValueBoolean = true;
    array.add(arrayValueBoolean);

    Map<String, Object> arrayValueMap = Maps.newHashMap();
    array.add(arrayValueMap);
    String objectKey1 = "foo";
    arrayValueMap.put(objectKey1, objectValue1);
    String objectKey2 = "bletch";
    int objectValue2 = 123;
    arrayValueMap.put(objectKey2, objectValue2);

    JsonNavigator nav = new StandardJsonNavigator(root);
    nav.down(keyString);

    assertEquals(arrayValueString, nav.getString(0));
    assertEquals((Integer) arrayValueInteger, nav.getInteger(1));
    assertEquals((Double) arrayValueDouble, nav.getDouble(2));
    assertEquals(arrayValueBoolean, nav.getBoolean(3));
    assertEquals(arrayValueMap, nav.getItem(4));

    assertEquals(array.size(), nav.getSize());

    nav.down(4);
    assertEquals(objectValue1, nav.getString(objectKey1));
    assertEquals((Integer) objectValue2, nav.getInteger(objectKey2));

    nav.up();

    assertEquals(arrayValueString, nav.getString(0));
    assertEquals((Integer) arrayValueInteger, nav.getInteger(1));
    assertEquals((Double) arrayValueDouble, nav.getDouble(2));
    assertEquals(arrayValueBoolean, nav.getBoolean(3));
    assertEquals(arrayValueMap, nav.getItem(4));

    assertEquals(array.size(), nav.getSize());
  }

  /**
   * Test using the path API.
   */
  @Test
  public void testPaths() {
    Map<String, Object> root = Maps.newHashMap();

    Map<String, Object> obj1 = Maps.newHashMap();
    String obj1Key = "foo";
    root.put(obj1Key, obj1);

    String obj1ValueKey1 = "bar";
    String obj1Value1 = "bletch";

    obj1.put(obj1ValueKey1, obj1Value1);

    String obj1ValueKey2 = "bar2";
    String obj1Value2 = "bletch2";

    obj1.put(obj1ValueKey2, obj1Value2);

    Map<String, Object> obj2 = Maps.newHashMap();
    String obj2Key = "banana";
    obj1.put(obj2Key, obj2);

    String obj2ValueKey = "orange";
    String obj2Value = "apple";

    obj2.put(obj2ValueKey, obj2Value);

    Map<String, Object> arrayElement1 = Maps.newHashMap();
    String arrayElement1ValueKey = "orangina";
    String arrayElement1Value = "apple sauce";
    arrayElement1.put(arrayElement1ValueKey, arrayElement1Value);

    List<Object> array = Lists.newArrayList();
    String arrayElement0 = "glork";
    array.add(arrayElement0);
    array.add(arrayElement1);

    String arrayKey = "array";
    root.put(arrayKey, array);

    JsonNavigator nav = new StandardJsonNavigator(root);

    // Simple path into a series of nested objects
    assertEquals(obj2Value,
        nav.traversePath(String.format("%s.%s.%s", obj1Key, obj2Key, obj2ValueKey)));

    // Path that passes through an array to a primitive in the array
    assertEquals(arrayElement0, nav.traversePath(String.format("%s.[0]", arrayKey)));

    // Path that passes through an array into an object in the array
    assertEquals(arrayElement1Value,
        nav.traversePath(String.format("%s.[1].%s", arrayKey, arrayElement1ValueKey)));

    // Test a relative path
    nav.down(obj1Key);

    assertEquals(obj2Value, nav.traversePath(String.format("%s.%s", obj2Key, obj2ValueKey)));

    // One level down, try an absolute path
    assertEquals(obj2Value,
        nav.traversePath(String.format("$.%s.%s.%s", obj1Key, obj2Key, obj2ValueKey)));

    // This will attempt to go through the primitive object
    try {
      nav.traversePath(String.format("%s.%s.foo", obj2Key, obj2ValueKey));
      fail();
    } catch (JsonInteractiveSpacesException e) {
      // Expected
    }
  }
}
