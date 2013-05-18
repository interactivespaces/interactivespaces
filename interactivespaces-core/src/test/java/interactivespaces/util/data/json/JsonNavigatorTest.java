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

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;

/**
 * Test the {@link JsonNavigator}.
 *
 * @author Keith M. Hughes
 */
public class JsonNavigatorTest {

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
		
		JsonNavigator nav = new JsonNavigator(root);
		
		assertEquals(valueString1, nav.getString(keyString1));
		assertEquals(valueString2, nav.getString(keyString2));
		assertEquals((Integer)valueInteger, nav.getInteger(keyInteger));
		assertEquals((Double)valueDouble, nav.getDouble(keyDouble));
		assertEquals((Boolean)valueBoolean, nav.getBoolean(keyBoolean));
	}

}
