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

import interactivespaces.service.web.server.HttpResponse;

import java.io.OutputStream;
import java.net.HttpCookie;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultCookie;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * A Netty-based HttpResponse
 * 
 * @author Keith M. Hughes
 */
public class NettyHttpResponse implements HttpResponse {

	/**
	 * The Netty handler context.
	 */
	private ChannelHandlerContext ctx;

	/**
	 * The channel buffer for writing content.
	 */
	private ChannelBuffer channelBuffer;
	
	/**
	 * Content headers to add to the response.
	 */
	private Multimap<String, String> contentHeaders = HashMultimap.create();
	
	private Multimap<String, HttpCookie> cookies = HashMultimap.create();
	
	public NettyHttpResponse(ChannelHandlerContext ctx,
	      Map<String, String> extraHttpContentHeaders) {
	  this.ctx = ctx;
	  channelBuffer = ChannelBuffers.dynamicBuffer();
	  for (String key : extraHttpContentHeaders.keySet()) {
	    contentHeaders.put(key, extraHttpContentHeaders.get(key));
	  }

	}
	
	public NettyHttpResponse(ChannelHandlerContext ctx,
			Multimap<String, String> extraHttpContentHeaders) {
		this.ctx = ctx;
		channelBuffer = ChannelBuffers.dynamicBuffer();
		
		if (extraHttpContentHeaders != null) {
			contentHeaders.putAll(extraHttpContentHeaders);
		}
	}

	@Override
	public OutputStream getOutputStream() {
		return new ChannelBufferOutputStream(channelBuffer);
	}

	@Override
	public void addContentHeader(String name, String value) {
		contentHeaders.put(name,  value);
	}

	@Override
	public void addContentHeaders(Multimap<String, String> headers) {
		contentHeaders.putAll(headers);
	}

	@Override
	public Multimap<String, String> getContentHeaders() {
		return contentHeaders;
	}

	/**
	 * @return the channelBuffer
	 */
	public ChannelBuffer getChannelBuffer() {
		return channelBuffer;
	}


    @Override
    public void addCookie(HttpCookie cookie) {
      cookies.put(cookie.getName(), cookie);      
      reencodeCookies();
    }
    
    private void reencodeCookies() {
      CookieEncoder encoder = new CookieEncoder(false);
      for (HttpCookie value: cookies.values()) {
        encoder.addCookie(createNettyCookie(value));
        contentHeaders.put("Set-Cookie", encoder.encode());
      }
    }
    
    @Override
    public void addCookies(Set<HttpCookie> newCookies) {
      if (newCookies == null) {
        return;
      }
      for (HttpCookie cookie : newCookies) {
        cookies.put(cookie.getName(), cookie);
      }
      reencodeCookies();
    }
    
    public static Cookie createNettyCookie(HttpCookie cookie) {
      Cookie nettyCookie = new DefaultCookie(cookie.getName(), cookie.getValue());
      nettyCookie.setComment(cookie.getComment());
      nettyCookie.setDomain(cookie.getDomain());
      nettyCookie.setMaxAge((int)cookie.getMaxAge());
      nettyCookie.setPath(cookie.getPath());
      nettyCookie.setPorts(createPortList(cookie.getPortlist()));
      nettyCookie.setVersion(cookie.getVersion());
      nettyCookie.setSecure(cookie.getSecure());
      nettyCookie.setDiscard(cookie.getDiscard());

      return nettyCookie;
    }
    
    private static Set<Integer> createPortList(String portString) {
      if (portString == null) {
        return Sets.newHashSet();
      }
      String[] portStrings = portString.split(",");
      Set<Integer> ports = Sets.newHashSet();
      for (String port : portStrings) {
        ports.add(Integer.valueOf(port));
      }
      return ports;
    }
    
}
