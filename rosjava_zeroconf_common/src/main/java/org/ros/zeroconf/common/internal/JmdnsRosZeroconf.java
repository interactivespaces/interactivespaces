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

package org.ros.zeroconf.common.internal;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.ros.exception.RosRuntimeException;
import org.ros.osgi.common.RosEnvironment;
import org.ros.zeroconf.common.RosZeroconf;
import org.ros.zeroconf.common.RosZeroconfListener;
import org.ros.zeroconf.common.ZeroconfRosMasterInfo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * An implementation of the Zeroconf master.
 * 
 * @author Keith M. Hughes
 */
public class JmdnsRosZeroconf implements RosZeroconf {

	/**
	 * The Zeroconf type for ROS masters.
	 */
	public static final String ZEROCONF_TYPE_ROS_MASTER = "_ros-master._tcp.local.";

	/**
	 * The JmDNS instance being used for service discovery.
	 */
	private JmDNS jmdns;

	/**
	 * The JmDNS service listener.
	 */
	private ServiceListener listener;

	/**
	 * The listeners for ROS zeroconf events.
	 */
	private List<RosZeroconfListener> rosListeners = new CopyOnWriteArrayList<RosZeroconfListener>();

	/**
	 * The ROS environment this is running under.
	 */
	private RosEnvironment rosEnvironment;

	@Override
	public void startup() {
		try {
			jmdns = JmDNS.create();
			jmdns.addServiceListener(ZEROCONF_TYPE_ROS_MASTER,
					listener = new ServiceListener() {
						public void serviceResolved(ServiceEvent ev) {
							signalNewMaster(getMasterInfo(ev.getInfo()));
						}

						public void serviceRemoved(ServiceEvent ev) {
							signalRemoveMaster(getMasterInfo(ev.getInfo()));
						}

						public void serviceAdded(ServiceEvent event) {
							// Required to force serviceResolved to be called
							// again (after the first search)
							jmdns.requestServiceInfo(event.getType(),
									event.getName(), 1);
						}
					});
			rosEnvironment.getLog().info("Zeroconf services started up");
		} catch (IOException e) {
			throw new RosRuntimeException(
					"Unable to start up ROS Zeroconf services", e);
		}
	}

	@Override
	public void shutdown() {
		try {
			jmdns.unregisterAllServices();
			jmdns.close();

			jmdns = null;
		} catch (IOException e) {
			throw new RosRuntimeException(
					"Unable to shut ROS Zeroconf services down", e);
		}
	}

	@Override
	public void addListener(RosZeroconfListener listener) {
		rosListeners.add(listener);
	}

	@Override
	public void removeListener(RosZeroconfListener listener) {
		rosListeners.remove(listener);
	}

	@Override
	public void registerMaster(ZeroconfRosMasterInfo masterInfo) {
		try {
			jmdns.registerService(getServiceInfo(masterInfo));
		} catch (Exception e) {
			throw new RosRuntimeException(
					"Unable to register new ROS master with Zeroconf", e);
		}
	}

	@Override
	public void unregisterMaster(ZeroconfRosMasterInfo masterInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ZeroconfRosMasterInfo> getKnownMasters() {
		List<ZeroconfRosMasterInfo> masters = Lists.newArrayList();

		for (ServiceInfo serviceInfo : jmdns.list(ZEROCONF_TYPE_ROS_MASTER)) {
			masters.add(getMasterInfo(serviceInfo));
		}

		return masters;
	}

	@Override
	public List<ZeroconfRosMasterInfo> getKnownMasters(String type) {
		List<ZeroconfRosMasterInfo> masters = Lists.newArrayList();

		for (ServiceInfo serviceInfo : jmdns.list(ZEROCONF_TYPE_ROS_MASTER)) {
			if (type.equals(serviceInfo.getSubtype()))
			masters.add(getMasterInfo(serviceInfo));
		}

		return masters;
	}

	/**
	 * Signal that a new master has been sighted.
	 * 
	 * @param masterInfo
	 *            information about the new master
	 */
	private void signalNewMaster(ZeroconfRosMasterInfo masterInfo) {
		if (rosEnvironment != null) {
			rosEnvironment.getLog().info(String.format("Zeroconf registering ROS Master %s", masterInfo));
		}
		
		for (RosZeroconfListener listener : rosListeners) {
			listener.onNewRosMaster(masterInfo);
		}
	}

	/**
	 * Signal that a master has been removed.
	 * 
	 * @param masterInfo
	 *            information about the new master
	 */
	private void signalRemoveMaster(ZeroconfRosMasterInfo masterInfo) {
		if (rosEnvironment != null) {
			rosEnvironment.getLog().info(String.format("Zeroconf unregistering ROS Master %s", masterInfo));
		}
		
		for (RosZeroconfListener listener : rosListeners) {
			listener.onRemoveRosMaster(masterInfo);
		}
	}

	/**
	 * Convert a ROS zeroconf master object into a JmDNS service info object.
	 * 
	 * @param masterInfo
	 *            the ROS master information
	 * 
	 * @return a properly configured JmDNS service info descriptor for the
	 *         master.
	 */
	private ServiceInfo getServiceInfo(ZeroconfRosMasterInfo masterInfo) {
		Map<String, String> txtInfo = Maps.newHashMap();
		txtInfo.put("protocols", masterInfo.getProtocols());
		return ServiceInfo.create(ZEROCONF_TYPE_ROS_MASTER,
				masterInfo.getName(), masterInfo.getType(),
				masterInfo.getPort(), masterInfo.getPriority(), masterInfo.getWeight(), txtInfo);
	}

	/**
	 * Convert a JmDNS service info object into a ROS zeroconf master object.
	 * 
	 * @param info
	 *            the JmDNS service information
	 * 
	 * @return the ROS zeroconf master object
	 */
	private ZeroconfRosMasterInfo getMasterInfo(ServiceInfo info) {
		String hostName = info.getHostAddresses()[0];

		ZeroconfRosMasterInfo masterInfo = new ZeroconfRosMasterInfo(
				info.getName(), info.getSubtype(),
				info.getPropertyString("protocols"), hostName, info.getPort(),
				info.getPriority(), info.getWeight());

		return masterInfo;
	}

	/**
	 * Set the Ros Environment being run in.
	 * 
	 * @param rosEnvironment
	 */
	public void setRosEnvironment(RosEnvironment rosEnvironment) {
		this.rosEnvironment = rosEnvironment;
	}

	/**
	 * Remove the ROS Environment that was being used.
	 * 
	 * @param rosEnvironment
	 */
	public void unsetRosEnvironment(RosEnvironment rosEnvironment) {
		this.rosEnvironment = null;
	}
}
