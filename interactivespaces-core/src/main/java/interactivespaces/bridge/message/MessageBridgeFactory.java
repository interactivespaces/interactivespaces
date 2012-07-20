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

package interactivespaces.bridge.message;

import org.apache.commons.logging.Log;

/**
 * A factory for {@link MessageBridge} instance.
 * 
 * @author Keith M. Hughes
 */
public interface MessageBridgeFactory {
	/**
	 * Get a message bridge from a specification.
	 * 
	 * @param specification
	 *            the specification for the bridge
	 * @param log
	 * 			  log that the bridge should use for any logging
	 *            
	 * @return A ready to run specification.
	 */
	MessageBridge newMessageBridge(String specification, Log log);
}
