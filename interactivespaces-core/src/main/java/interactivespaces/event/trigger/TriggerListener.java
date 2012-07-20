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

package interactivespaces.event.trigger;


/**
 * A listener for trigger events.
 *
 * @author Keith M. Hughes
 */
public interface TriggerListener {

	/**
	 * Have triggered to a new state.
	 * 
	 * @param src
	 *            The source of the event.
	 * @param newState
	 *            The new state.
	 * @param event
	 *            The type of event created by the trigger.
	 */
	void onTrigger(Trigger src, TriggerState newState, TriggerEventType event);

}
