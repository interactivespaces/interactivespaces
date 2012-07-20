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

package interactivespaces.event;

/**
 * A subscriber for {@olink Event}.
 * 
 * @author Keith M. Hughes
 */
public interface EventSubscriber {

	/**
	 * Add an event listener for events of a given type.
	 * 
	 * @param type
	 *            the type of event
	 * @param listener
	 *            the listener for the event
	 */
	void addEventListener(String type, EventListener listener);

	/**
	 * Remove an event listener for events of a given type.
	 * 
	 * <p>
	 * Does nothing if the lister was never added for the given type
	 * 
	 * @param type
	 *            the type of event
	 * @param listener
	 *            the listener for the event
	 */
	void removeEventListener(String type, EventListener listener);
}
