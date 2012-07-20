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

package interactivespaces.service.script;

import interactivespaces.activity.execution.ActivityMethodInvocation;
import interactivespaces.activity.execution.BaseActivityExecutionContext;

/**
 * A {@link ActivityExecutionContext} for scripted items.
 * 
 * @author Keith M. Hughes
 */
public class ScriptActivityExecutionContext extends
		BaseActivityExecutionContext {

	/**
	 * The classloader to use during invocation.
	 */
	private ClassLoader newLoader;

	public ScriptActivityExecutionContext(ClassLoader newLoader) {
		this.newLoader = newLoader;
	}
	
	@Override
	protected ActivityMethodInvocation newInvocation() {
		return new ScriptActivityMethodInvocation(newLoader);
	}
}
