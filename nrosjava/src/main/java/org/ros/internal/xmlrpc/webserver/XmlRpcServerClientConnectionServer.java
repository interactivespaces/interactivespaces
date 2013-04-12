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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.ServerStreamConnection;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.server.XmlRpcHttpServer;

/**
 * A connection server for the Netty Apache XMLRPC webserver
 * 
 * <p>
 * This code is derived from the web server which came with the Apache XMLRPC
 * server.
 * 
 * @author Apache
 * @author Keith M. Hughes
 */
public class XmlRpcServerClientConnectionServer extends XmlRpcHttpServer {
	protected void writeError(XmlRpcStreamRequestConfig config,
			OutputStream stream, Throwable throwable) throws XmlRpcException {
		XmlRpcServerClientRequestData data = (XmlRpcServerClientRequestData) config;
		try {
			if (data.isByteArrayRequired()) {
				super.writeError(config, stream, throwable);
				data.getConnection().notifyError(data, throwable);
			} else {
				data.getConnection().notifyError(data, throwable);
				super.writeError(config, stream, throwable);
				stream.flush();
			}
		} catch (IOException e) {
			throw new XmlRpcException(e.getMessage(), e);
		}
	}

	@Override
	protected void writeResponse(XmlRpcStreamRequestConfig config,
			OutputStream stream, Object result) throws XmlRpcException {
		XmlRpcServerClientRequestData data = (XmlRpcServerClientRequestData) config;
		try {
			if (data.isByteArrayRequired()) {
				super.writeResponse(config, stream, result);
				//data.getConnection().writeResponse(data, stream);
			} else {
				//data.getConnection().writeResponseHeader(data, -1);
				super.writeResponse(config, stream, result);
				stream.flush();
			}
		} catch (IOException e) {
			throw new XmlRpcException(e.getMessage(), e);
		}
	}

	@Override
	protected void setResponseHeader(ServerStreamConnection connection,
			String headerName, String headerValue) {
		((XmlRpcServerClientConnection) connection).setResponseHeader(headerName, headerValue);
	}
}