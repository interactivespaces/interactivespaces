/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for the {@link SimpleExpressionEvaluator}.
 * 
 * @author Keith M. Hughes
 */
public class SimpleExpressionEvaluatorTest {

	private static final String NAME_FIRST = "Joan";
	private static final String TODAY_DATE = "1/2/2005";

	private EvaluationEnvironment environment;

	private SimpleExpressionEvaluator evaluator;

	@Before
	public void setUp() {
		environment = mock(EvaluationEnvironment.class);
		when(environment.lookupVariableValue(eq("today"))).thenReturn(
				TODAY_DATE);
		when(environment.lookupVariableValue(eq("nameFirst"))).thenReturn(
				NAME_FIRST);

		evaluator = new SimpleExpressionEvaluator();
		evaluator.setEvaluationEnvironment(environment);
	}

	@Test
	public void testSingleExistsSimple() {
		assertEquals(evaluator.evaluateStringExpression("${today}"), TODAY_DATE);
	}

	@Test
	public void leExistsEmbedded() {
		assertEquals(evaluator.evaluateStringExpression("Foo ${today} bar"),
				"Foo " + TODAY_DATE + " bar");
	}

	@Test
	public void testSingleExistsStart() {
		assertEquals(evaluator.evaluateStringExpression("${today} bar"),
				TODAY_DATE + " bar");
	}

	@Test
	public void testSingleExistsEndsd() {
		assertEquals(evaluator.evaluateStringExpression("Foo ${today}"), "Foo "
				+ TODAY_DATE);
	}

	@Test
	public void testDouble() {
		assertEquals(
				evaluator.evaluateStringExpression("${nameFirst} Foo ${today}"),
				NAME_FIRST + " Foo " + TODAY_DATE);
	}

	@Test
	public void testMissingSingle() {
		assertEquals(evaluator.evaluateStringExpression("${nameLast}"),
				"${nameLast}");
	}

	@Test
	public void testSingleError() {
		try {
			evaluator.evaluateStringExpression("${nameFirst");
			fail();
		} catch (EvaluationInteractiveSpacesException e) {
			
		}
	}

	@Test
	public void testDoubleOneMissing() {
		assertEquals(
				evaluator.evaluateStringExpression("${nameLast} Foo ${today}"),
				"${nameLast} Foo " + TODAY_DATE);
	}

	@Test
	public void testDoubleError() {
		assertEquals(
				evaluator.evaluateStringExpression("${nameFirst Foo ${today}"),
				"${nameFirst Foo ${today}");
	}
}
