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

package interactivespaces.service.web.server.internal.netty;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.web.server.HttpRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * An HTTP request that proxies the Netty HTTP request
 * 
 * @author Keith M. Hughes
 */
public class NettyHttpRequest implements HttpRequest {

	/**
	 * The proxied request.
	 */
	private org.jboss.netty.handler.codec.http.HttpRequest request;

	/**
	 * The logger for this request.
	 */
	private Log log;
	
	public NettyHttpRequest(
			org.jboss.netty.handler.codec.http.HttpRequest request, Log log) {
		this.request = request;
		this.log = log;
	}

	@Override
	public URI getUri() {
		try {
			return new URI(request.getUri());
		} catch (URISyntaxException e) {
			// Should never, ever happen
			throw new InteractiveSpacesException(String.format(
					"Illegal URI syntax %s", request.getUri()), e);
		}
	}

	@Override
	public Map<String, String> getUriQueryParameters() {
		Map<String, String> params = Maps.newHashMap();
		
		String rawQuery = getUri().getRawQuery();
		if (rawQuery != null && !rawQuery.isEmpty()) {
			String[] components = rawQuery.split("\\&");
			for (String component : components) {
				int pos = component.indexOf('=');
				if (pos != -1) {
					String decode = component.substring(pos + 1);
					try {
						decode = URLDecoder.decode(decode, "UTF-8");
					} catch (Exception e) {
						// Don't care
					}
					params.put(component.substring(0, pos).trim(), decode);
				} else {
					params.put(component.trim(), "");
				}
			}
		}
		
		return params;
	}

	@Override
	public Log getLog() {
		return log;
	}
}
