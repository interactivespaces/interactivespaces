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

package interactivespaces.master.server.services;

import interactivespaces.InteractiveSpacesException;

/**
 * A {@link InteractiveSpacesException} for entities which are not found.
 * 
 * @author Keith M. Hughes
 */
public class EntityNotFoundInteractiveSpacesException extends
		InteractiveSpacesException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8428724375774147347L;

	public EntityNotFoundInteractiveSpacesException(String message) {
		super(message);
	}

	public EntityNotFoundInteractiveSpacesException(String message,
			Throwable cause) {
		super(message, cause);
	}
}
