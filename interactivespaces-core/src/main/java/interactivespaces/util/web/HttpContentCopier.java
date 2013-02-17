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

package interactivespaces.util.web;

import interactivespaces.util.resource.ManagedResource;

import java.io.File;

/**
 * Copy content from an HTTP connection to a file.
 * 
 * <p>
 * It is safe to have multiple threads copying content.
 * 
 * @author Keith M. Hughes
 */
public interface HttpContentCopier extends ManagedResource {

	/**
	 * Copy the contents from the source URI to the destination file.
	 * 
	 * <p>
	 * This method blocks until the transfer is complete or it fails.
	 * 
	 * @param sourceUri
	 *            the URI to copy the content from
	 * @param destination
	 *            where to copy the content to
	 */
	void copy(String sourceUri, File destination);
}
