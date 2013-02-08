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

import com.google.common.collect.Multimap;

import java.io.OutputStream;
import java.net.HttpCookie;
import java.util.Collection;
import java.util.Set;

/**
 * A response for an HTTP request.
 * 
 * @author Keith M. Hughes
 */
public interface HttpResponse {

	/**
	 * Get the output stream for the response.
	 * 
	 * @return the output stream for the response
	 */
	OutputStream getOutputStream();

	/**
	 * Add an HTTP content header that will go out with the HTTP response.
	 * 
	 * @param name
	 *            name of the header
	 * @param value
	 *            value of the header
	 */
	void addContentHeader(String name, String value);
	
	/**
	 * Add an HTTP content header that will go out with the HTTP response.
	 * 
	 * @param headers
	 *            the headers to add, the key is the header name, value is the
	 *            header value
	 */
	void addContentHeaders(Multimap<String, String> headers);
	
	
	/**
	 * Add the given cookie to the response.
	 * @param cookies
	 */
	void addCookie(HttpCookie cookie);
	
	/**
	 * Add a set of cookies to the response.
	 * @param cookie
	 */
	void addCookies(Set<HttpCookie> cookie);

	/**
	 * Get the content headers that have been added.
	 * 
	 * @return the key is the header name, the value is the header value
	 */
	Multimap<String, String> getContentHeaders();
}
