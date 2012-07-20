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


/**
 * An evaluator for some expressions.
 * 
 * @author Keith M. Hughes
 */
public interface ExpressionEvaluator {
	
	/**
	 * Set the evaluation environment
	 * @param environment
	 */
	void setEvaluationEnvironment(EvaluationEnvironment environment);
	
	/**
	 * Evaluate a string expression.
	 * 
	 * @param initial
	 *            the initial string expression to evaluate. It will contain
	 *            {@code code ${expr}} at various places.
	 * 
	 * @return The string with all expressions evaluated.
	 * 
	 * @throws EvaluationInteractiveSpacesException An evaluation error of some sort occurred.
	 */
	String evaluateStringExpression(String initial) throws EvaluationInteractiveSpacesException;
	
	/**
	 * Evaluate an expression.
	 * 
	 * @param expression
	 * 			the expression to evaluate in whatever expression language is being supported.
	 * 
	 * @return The value of the expression.
	 * 
	 * @throws EvaluationInteractiveSpacesException An evaluation error of some sort occurred.
	 */
	Object evaluateExpression(String expression) throws EvaluationInteractiveSpacesException;
}
