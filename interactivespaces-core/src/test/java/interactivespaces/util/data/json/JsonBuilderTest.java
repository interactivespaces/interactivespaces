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

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Test the {@link JsonBuilder}.
 *
 * @author Keith M. Hughes
 */
public class JsonBuilderTest {

  private JsonBuilder builder;

  @Before
  public void setup() {
    builder = new JsonBuilder();
  }

  /**
   * Make sure primitives are properly handled.
   */
  @Test
  public void testPrimitives() {
    String keyString1 = "string1";
    String valueString1 = "bar";
    builder.put(keyString1, valueString1);

    String keyString2 = "string1";
    String valueString2 = "bar";
    builder.put(keyString2, valueString2);

    String keyInteger = "integer";
    int valueInteger = 1;
    builder.put(keyInteger, valueInteger);

    String keyDouble = "double";
    double valueDouble = 1.23456;
    builder.put(keyDouble, valueDouble);

    String keyBoolean = "boolean";
    boolean valueBoolean = true;
    builder.put(keyBoolean, valueBoolean);

    Map<String, Object> object = builder.build();

    assertEquals(valueString1, object.get(keyString1));
    assertEquals(valueString2, object.get(keyString2));
    assertEquals(valueInteger, object.get(keyInteger));
    assertEquals(valueDouble, object.get(keyDouble));
    assertEquals(valueBoolean, object.get(keyBoolean));
  }

  /**
   * Test putting all elements into a container
   */
  @Test
  public void testPutAll() {
    Map<String, Object> testData = Maps.newHashMap();

    String keyString1 = "string1";
    String valueString1 = "bar";
    testData.put(keyString1, valueString1);

    String keyString2 = "string1";
    String valueString2 = "bar";
    testData.put(keyString2, valueString2);

    String keyInteger = "integer";
    int valueInteger = 1;
    testData.put(keyInteger, valueInteger);

    String keyDouble = "double";
    double valueDouble = 1.23456;
    testData.put(keyDouble, valueDouble);

    String keyBoolean = "boolean";
    boolean valueBoolean = true;
    testData.put(keyBoolean, valueBoolean);

    builder.putAll(testData);

    Map<String, Object> object = builder.build();

    assertEquals(valueString1, object.get(keyString1));
    assertEquals(valueString2, object.get(keyString2));
    assertEquals(valueInteger, object.get(keyInteger));
    assertEquals(valueDouble, object.get(keyDouble));
    assertEquals(valueBoolean, object.get(keyBoolean));
  }

  /**
   * Test nesting objects. Also add in a new value after the last nest to make
   * sure that up() works.
   */
  @Test
  public void testNestedObjects() {
    String key = "key";
    String value0 = "value0";
    builder.put(key, value0);

    String keyObject1 = "obj1";
    builder.newObject(keyObject1);

    String value1 = "value1";
    builder.put(key, value1);

    String keyObject2 = "obj2";
    builder.newObject(keyObject2);

    String value2 = "value2";
    builder.put(key, value2);

    // Go to obj1
    builder.up();

    String nextKey1 = "key7";
    String nextValue1 = "value7";
    builder.put(nextKey1, nextValue1);

    Map<String, Object> root = builder.build();

    assertEquals(value0, root.get(key));

    Map<String, Object> obj1 = (Map<String, Object>) root.get(keyObject1);
    assertEquals(value1, obj1.get(key));
    assertEquals(nextValue1, obj1.get(nextKey1));

    Map<String, Object> obj2 = (Map<String, Object>) obj1.get(keyObject2);
    assertEquals(value2, obj2.get(key));
  }

  /**
   * Test putting in unnamed objects.
   */
  @Test
  public void testObjectUnamedItems() {
    try {
      builder.newObject();

      fail();
    } catch (Exception e) {
      // Expected
    }

    try {
      builder.newArray();

      fail();
    } catch (Exception e) {
      // Expected
    }
  }

  /**
   * Test putting in an array into an object.
   */
  @Test
  public void testObjectAddArray() {
    String key = "foo";
    builder.newArray(key);

    List<Object> expected = Lists.newArrayList((Object) 17, (Object) 7);

    for (Object value : expected) {
      builder.put(value);
    }

    Map<String, Object> root = builder.build();

    List<Object> actual = (List<Object>) root.get(key);
    assertEquals(expected, actual);

  }

  /**
   * Test putting in various types into an array. This will include a primitive,
   * an array, and an object.
   */
  @Test
  public void testArrayAddVariousTypes() {
    String key = "foo";
    builder.newArray(key);
    builder.newArray();

    List<Object> expectedArray = Lists.newArrayList((Object) 17, (Object) 7);

    for (Object value : expectedArray) {
      builder.put(value);
    }

    builder.up();

    String afterValue = "foo";
    builder.put(afterValue);

    String objKey = "glorp";
    String objValue = "goober";
    builder.newObject();
    builder.put(objKey, objValue);

    Map<String, Object> root = builder.build();

    List<Object> actualArray = (List<Object>) ((List<Object>) root.get(key)).get(0);
    assertEquals(expectedArray, actualArray);

    Object actualPrimitiveValue = ((List<Object>) root.get(key)).get(1);
    assertEquals(afterValue, actualPrimitiveValue);

    Map<String, Object> expectedObjectValue = Maps.newHashMap();
    expectedObjectValue.put(objKey, objValue);
    Map<String, Object> actualObjectValue =
        (Map<String, Object>) ((List<Object>) root.get(key)).get(2);
    assertEquals(expectedObjectValue, actualObjectValue);
  }

  /**
   * Test putting in adding named items into an array.
   */
  @Test
  public void testArrayNamedItems() {
    builder.newArray("foo");

    try {
      builder.newObject("foo");

      fail();
    } catch (Exception e) {
      // Expected
    }

    try {
      builder.newArray("bar");

      fail();
    } catch (Exception e) {
      // Expected
    }
  }
}
