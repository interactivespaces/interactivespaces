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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

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
}
