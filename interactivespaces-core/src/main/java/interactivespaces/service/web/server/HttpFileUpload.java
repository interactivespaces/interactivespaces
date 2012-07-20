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

package interactivespaces.service.web.server;

import interactivespaces.InteractiveSpacesException;

import java.io.File;
import java.util.Map;

/**
 * An HTTP file upload.
 * 
 * @author Keith M. Hughes
 */
public interface HttpFileUpload {

	/**
	 * Is there actually a file in the upload?
	 * 
	 * @return {@code true} if a file was loaded.
	 */
	boolean hasFile();

	/**
	 * Move the file to the given destination.
	 * 
	 * @param destination
	 *            where to write the new file
	 * 
	 * @return {@code true} if there was a file to move an the move was
	 *         successful, {@code false} if there was no file to move
	 * 
	 * @throws InteractiveSpacesException
	 *             something ba happened during the move
	 */
	boolean moveTo(File destination);

	/**
	 * Get the file name the file had.
	 * 
	 * @return the file name, or {@code null} if there was no file
	 */
	String getFilename();

	/**
	 * Get the parameters which were part of the file upload.
	 * 
	 * <p>
	 * These are any text parameters in the HTTP form.
	 * 
	 * @return the map
	 */
	Map<String, String> getParameters();
}