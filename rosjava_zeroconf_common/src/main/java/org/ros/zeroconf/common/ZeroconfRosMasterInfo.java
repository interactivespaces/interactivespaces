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

package org.ros.zeroconf.common;

import java.net.URI;
import java.net.URISyntaxException;

import org.ros.exception.RosRuntimeException;

/**
 * information about a ROS master from Zeroconf.
 * 
 * <p>
 * The Java identity of this object is only in its name.
 * 
 * @author Keith M. Hughes
 */
public class ZeroconfRosMasterInfo implements ZeroconfServiceInfo {

	/**
	 * The name used to identify the master.
	 */
	private String name;

	/**
	 * The type of the master, e.g. dev, local, prod, qa.
	 */
	private String type;

	/**
	 * A colon-separated lists of protocols which can be used to contact the
	 * master, e.g. http.
	 */
	private String protocols;

	/**
	 * Hostname that the master is running on.
	 */
	private String hostName;

	/**
	 * The port number for the master.
	 */
	private int port;

	/**
	 * Priority of the server. Lower priority values should be used first.
	 */
	private int priority;

	/**
	 * Weight of the server. All servers of the same priority will be randomly
	 * chosen from, weighted by this value.
	 */
	private int weight;

	public ZeroconfRosMasterInfo(String name, String type, String protocols,
			String hostName, int port, int priority, int weight) {
		this.name = name;
		this.type = type;
		this.protocols = protocols;
		this.hostName = hostName;
		this.port = port;
		this.priority = priority;
		this.weight = weight;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Get the type of the master. Examples are local, prod, etc.
	 * 
	 * @return the type of the master
	 */
	public String getType() {
		return type;
	}

	/**
	 * Get the protocols supported by the master. Examples are http, https.
	 * 
	 * @return A colon-separated list of protocols.
	 */
	public String getProtocols() {
		return protocols;
	}

	@Override
	public String getHostName() {
		return hostName;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	/**
	 * Get the URI for this master.
	 * 
	 * @param protocol
	 *            which supported protocol the URI should use
	 *            
	 * @return the URI for accessing the master.
	 */
	public URI getUri(String protocol) {
		// TODO(keith): Should check if protocol is supported
		String masterUri = protocol + "://" + getHostName() + ":" + getPort() + "/";
		try {
			return new URI(masterUri);
		} catch (URISyntaxException e) {
			throw new RosRuntimeException(String.format("Illegal Master URI %s", masterUri), e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ZeroconfRosMasterInfo other = (ZeroconfRosMasterInfo) obj;
		if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ZeroconfRosMasterInfo [name=" + name + ", type=" + type
				+ ", protocols=" + protocols + ", hostName=" + hostName
				+ ", port=" + port + ", priority=" + priority + ", weight="
				+ weight + "]";
	}
}
