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
 * An environment for looking up values for evaluating expresions.
 *
 * @author Keith M. Hughes
 */
public interface EvaluationEnvironment {
	/**
	 * Look up the value of a variable.
	 * 
	 * @param variable
	 * 			the name of the variable to lookup
	 * 
	 * @return The value of the variable.
	 * 
	 * @throws EvaluationInteractiveSpacesException
	 */
	String lookupVariableValue(String variable) throws EvaluationInteractiveSpacesException;
}
