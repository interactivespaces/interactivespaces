/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.ros.internal.xmlrpc.webserver;

import org.apache.xmlrpc.common.XmlRpcHttpRequestConfigImpl;

/**
 * Data from the XMLRPC request.
 * 
 * <p>
 * This code is derived from the web server which came with the Apache XMLRPC
 * server.
 * 
 * @author Apache
 * @author Keith M. Hughes
 */
public class XmlRpcServerClientRequestData extends XmlRpcHttpRequestConfigImpl {

	/**
	 * The connection for the specific XMLRPC request.
	 */
	private final XmlRpcServerClientConnection connection;

	/**
	 * {@code true} if the connection should be kept open between requests.
	 */
	private boolean keepAlive;
	
	/**
	 * The HTTP method for the call.
	 */
	private String method;
	
	/**
	 * The HTTP version for the call.
	 */
	private String httpVersion;
	
	/**
	 * The length of content.
	 */
	private long contentLength = -1;
	
	/**
	 * {@code true} if the call was successful.
	 */
	private boolean success;

	/**
	 * Creates a new instance.
	 * 
	 * @param pConnection
	 *            The connection, which is serving the request.
	 */
	public XmlRpcServerClientRequestData(XmlRpcServerClientConnection connection) {
		this.connection = connection;
	}

	/**
	 * Returns the connection, which is serving the request.
	 * 
	 * @return The request connection.
	 */
	public XmlRpcServerClientConnection getConnection() {
		return connection;
	}

	/**
	 * Returns, whether HTTP keepAlive is enabled for this connection.
	 * 
	 * @return True, if keepAlive is enabled, false otherwise.
	 */
	public boolean isKeepAlive() {
		return keepAlive;
	}

	/**
	 * Set whether HTTP keepAlive is enabled for this connection.
	 * 
	 * @param keepAlive
	 *            {@code true} if keep alive is enabled
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	/**
	 * Returns the requests HTTP version.
	 * 
	 * @return HTTP version, for example {@code 1.0}.
	 */
	public String getHttpVersion() {
		return httpVersion;
	}

	/**
	 * Sets the requests HTTP version.
	 * 
	 * @param httpVersion
	 *            HTTP version, for example {@code 1.1}
	 */
	public void setHttpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
	}

	/**
	 * Returns the requests content length.
	 * 
	 * @return Content length, or {@code -1} if unknown.
	 */
	public long getContentLength() {
		return contentLength;
	}

	/**
	 * Sets the requests content length.
	 * 
	 * @param contentLength
	 *            content length, or {@code -1} if unknown
	 */
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * Is a byte array for buffering the output required?
	 * 
	 * @return {@code true} if the byte array is required, false otherwise.
	 */
	public boolean isByteArrayRequired() {
		return isKeepAlive() || !isEnabledForExtensions()
				|| !isContentLengthOptional();
	}

	/**
	 * Get the request method.
	 * 
	 * @return The request method, should be {@code POST}.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Set the request HTTP method.
	 * 
	 * @param method
	 *            the request method, should be {@code POST}
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Get whether the request was executed successful.
	 * 
	 * @return {@code true} for success, {@code false} if an error occurred.
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Set whether the request was executed successfully or not.
	 * 
	 * @param success
	 *            {@code true} for success, {@code false} if an error occurred
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}
}