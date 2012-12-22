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

package interactivespaces.comm.serial.rxtx;

import gnu.io.CommPortIdentifier;
import interactivespaces.comm.serial.SerialCommunicationEndpoint;
import interactivespaces.comm.serial.SerialCommunicationEndpointService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.Enumeration;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * A factory for serial communication endpoints using the RXTX library.
 * 
 * @author Keith M. Hughes
 */
public class RxtxSerialCommunicationEndpointService implements
		SerialCommunicationEndpointService {
	
	/**
	 * Space environment for this service.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	@Override
	public void startup() {
		// Nothing to do yet.
	}

	@Override
	public void shutdown() {
		// Nothing to do yet.
	}

	@Override
	public List<String> getSerialPorts() {
		List<String> ports = Lists.newArrayList();

		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier
				.getPortIdentifiers();

		CommPortIdentifier portId = null; // will be set if port found

		while (portIdentifiers.hasMoreElements()) {
			CommPortIdentifier pid = portIdentifiers.nextElement();
			if (pid.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				ports.add(pid.getName());
			}
		}

		return ports;
	}

	@Override
	public SerialCommunicationEndpoint newSerialEndpoint(String portName) {
		return new RxtxSerialCommunicationEndpoint(portName);
	}

	@Override
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}
}
