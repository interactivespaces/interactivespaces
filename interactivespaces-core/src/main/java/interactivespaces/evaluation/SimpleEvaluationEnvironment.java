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

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A standalone {@link EvaluationEnvironment}.
 * 
 * @author Keith M. Hughes
 * @since Jul 25, 2012
 */
public class SimpleEvaluationEnvironment implements EvaluationEnvironment {

	/**
	 * The map of values for the environment.
	 */
	private Map<String, String> values = Maps.newHashMap();

	@Override
	public String lookupVariableValue(String variable)
			throws EvaluationInteractiveSpacesException {
		return values.get(variable);
	}

	/**
	 * Set the value of a variable.
	 * 
	 * @param variable
	 *            the name of the variable
	 * @param value
	 *            the value of the variable
	 */
	public void set(String variable, String value) {
		values.put(variable, value);
	}
}
