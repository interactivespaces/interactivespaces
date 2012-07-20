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

package interactivespaces.activity.execution;

/**
 * A method invocation on an activity.
 * 
 * <p>
 * The methods on this class should never be called by the user.
 *
 * @author Keith M. Hughes
 */
public interface ActivityMethodInvocation {
	
	/**
	 * The method has been entered.
	 */
	void methodEnter();
	
	/**
	 * The method has been exited for any reason.
	 */
	void methodExit();
}
