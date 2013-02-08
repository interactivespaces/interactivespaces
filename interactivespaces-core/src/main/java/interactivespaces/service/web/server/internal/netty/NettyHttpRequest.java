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

import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

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
	
	private Multimap<String, String> headers;
	
	public NettyHttpRequest(
			org.jboss.netty.handler.codec.http.HttpRequest request, Log log) {
		this.request = request;
		this.log = log;
		headers = null;
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
	
	/**
	 * Return the map of key:value pairs of strings making up the header for
	 * this request.
	 * 
	 * @return the map of all header key:value pairs
	 */
	@Override
	public Multimap<String, String> getHeaders() {
	  if (headers == null) {
	    buildHeaders();
	  }
	  return headers;
	}
	
	/**
	 * Return the value of the header string associated with the given key,
	 * or null if no such key is present. 
	 * 
	 */
	@Override
	public Set<String> getHeader(String key) {
	  if (headers == null) {
	    buildHeaders();
	  }
	  if (headers.containsKey(key)) {
	    return Sets.newHashSet(headers.get(key));
	  }
	  
	  return null;
	}
	
	private void buildHeaders() {
      headers = HashMultimap.create();
      
      for (Entry<String, String> header : request.getHeaders()) {
        headers.put(header.getKey(), header.getValue());
      } 
	}

  /* 
   */
  @Override
  public HttpCookie getCookie(String key) {
    Collection<HttpCookie> cookies = getCookies();
    for (HttpCookie cookie : cookies) {
      if (key.equals(cookie.getName())) {
        return cookie;
      }
    }
    
    return null;
  }

  /*
   */
  @Override
  public Set<HttpCookie> getCookies() {
    
    Collection<String> cookieHeader = getHeader("Cookie");
    if (cookieHeader == null) {
      return Sets.newHashSet();
    }
    Collection<HttpCookie> cookies = Sets.newHashSet();
    for (String cookie : cookieHeader) {
      cookies.addAll(Collections2.transform(
          new CookieDecoder().decode(cookie),
          new Function<Cookie, HttpCookie>() {
            @Override
            public HttpCookie apply(final Cookie cookie) {
              return convertFromNettyCookie(cookie);
            }
          }));
    }
    return Sets.newHashSet(cookies);
  }
  
  public static HttpCookie convertFromNettyCookie(Cookie cookie) {
    HttpCookie httpCookie = new HttpCookie(cookie.getName(), cookie.getValue());
    httpCookie.setComment(cookie.getComment());
    httpCookie.setDomain(cookie.getDomain());
    httpCookie.setMaxAge(cookie.getMaxAge());
    httpCookie.setPath(cookie.getPath());
    httpCookie.setPortlist(createPortString(cookie.getPorts()));
    httpCookie.setVersion(cookie.getVersion());
    httpCookie.setSecure(cookie.isSecure());
    httpCookie.setDiscard(cookie.isDiscard());
    httpCookie.setHttpOnly(cookie.isHttpOnly());
    
    return httpCookie;
  }
  
  private static String createPortString(Set<Integer> ports) {
    StringBuilder portString = new StringBuilder(); 
    Iterator<Integer> iter = ports.iterator();
    while (iter.hasNext()) {
      portString.append(String.valueOf(iter.next()));
      if (iter.hasNext()) {
        portString.append(",");
      }
    }
    
    return portString.toString();
  }
}
