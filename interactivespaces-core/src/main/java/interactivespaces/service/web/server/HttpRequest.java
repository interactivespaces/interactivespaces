/*
 * Copyright (C) 2013 Google Inc.
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

import java.net.URI;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * An HHTP request coming into the server.
 * 
 * @author Keith M. Hughes
 */
public interface HttpRequest {

	/**
	 * Get the URI of the request.
	 * 
	 * @return the URI of the request.
	 */
	URI getUri();

	/**
	 * Get the query parameters from the URI.
	 * 
	 * @return the query parameters
	 */
	Map<String, String> getUriQueryParameters();
	
	/**
	 * Get the logger for this request.
	 * 
	 * @return the logger to use
	 */
	Log getLog();
}
