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

package interactivespaces.system.internal.osgi;

import interactivespaces.configuration.Configuration;
import interactivespaces.service.ServiceRegistry;
import interactivespaces.service.SimpleServiceRegistry;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.time.TimeProvider;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

/**
 * A {@link InteractiveSpacesEnvironment} which lives in a ROS container.
 * 
 * @author Keith M. Hughes
 */
public class RosOsgiInteractiveSpacesEnvironment implements
		InteractiveSpacesEnvironment {

	/**
	 * The map of logging levels to their log4j level.
	 */
	public static final Map<String, Level> log4jLevels;

	static {
		Map<String, Level> levels = Maps.newHashMap();
		levels.put(LOG_LEVEL_ERROR, Level.ERROR);
		levels.put(LOG_LEVEL_FATAL, Level.FATAL);
		levels.put(LOG_LEVEL_DEBUG, Level.DEBUG);
		levels.put(LOG_LEVEL_INFO, Level.INFO);
		levels.put(LOG_LEVEL_OFF, Level.OFF);
		levels.put(LOG_LEVEL_TRACE, Level.TRACE);
		levels.put(LOG_LEVEL_WARN, Level.WARN);

		log4jLevels = Collections.unmodifiableMap(levels);
	}

	/**
	 * The system configuration.
	 */
	private Configuration systemConfiguration;

	/**
	 * The executor service to use for thread pools.
	 */
	private ScheduledExecutorService executorService;

	/**
	 * The file system for Interactive Spaces.
	 */
	private InteractiveSpacesFilesystem filesystem;

	/**
	 * Base log for Interactive Spaces.
	 */
	private Log log;

	/**
	 * Network type for the container.
	 * 
	 * <p>
	 * This allows distinguishing between different Interactive Spaces networks,
	 * e.g. localdev, prod, fredbot.
	 */
	private String networkType;
	
	/**
	 * The time provider for everyone to use.
	 */
	private TimeProvider timeProvider;

	/**
	 * Values stored in the environment.
	 */
	private ConcurrentMap<String, Object> values = Maps.newConcurrentMap();
	
	/**
	 * The service registry.
	 */
	private ServiceRegistry serviceRegistry = new SimpleServiceRegistry();

	@Override
	public Configuration getSystemConfiguration() {
		return systemConfiguration;
	}

	@Override
	public String getNetworkType() {
		return networkType;
	}

	@Override
	public InteractiveSpacesFilesystem getFilesystem() {
		return filesystem;
	}

	@Override
	public ScheduledExecutorService getExecutorService() {
		return executorService;
	}

	@Override
	public Log getLog() {
		// Just hand along the ROS Environment log.
		return log;
	}

	@Override
	public Log getLog(String logName, String level) {
		// TODO(keith): We need a logging bundle which handles all of this
		// stuff.
		// OSGi logging only allows one logger, need something more general.
		// This would allow things like being able to change log levels, etc.
//		Level l = log4jLevels.get(level.toLowerCase());
//		boolean unknownLevel = false;
//		if (l == null) {
//			unknownLevel = true;
//			l = Level.ERROR;
//		}
//
//		Logger logger = Logger.getLogger("interactivespaces." + logName);
//		logger.setLevel(l);
//
//		if (unknownLevel) {
//			logger.error(String.format("Unknown log level %s, set to ERROR",
//					level));
//		}

		//return new Log4JLogger(logger);
		return new Jdk14Logger("interactivespaces." + logName);
	}

	@Override
	public boolean modifyLogLevel(Log log, String level) {
		if (Log4JLogger.class.isAssignableFrom(log.getClass())) {
		
			Level l = log4jLevels.get(level.toLowerCase());
			if (l != null) {
				((Log4JLogger)log).getLogger().setLevel(l);
				
				return true;
			} else {
				log.error(String.format("Unknown log level %s", level));
			}
		} else {
			log.error("Attempt to modify an unmodifiable logger");
		}

		return false;
	}

	@Override
	public TimeProvider getTimeProvider() {
		return timeProvider;
	}

	@Override
	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	@Override
	public <T> T getValue(String valueName) {
		@SuppressWarnings("unchecked")
		T value = (T) values.get(valueName);

		return value;
	}

	@Override
	public void setValue(String valueName, Object value) {
		values.put(valueName, value);
	}

	@Override
	public void removeValue(String valueName) {
		values.remove(valueName);
	}

	/**
	 * @param filesystem
	 *            the filesystem to set
	 */
	public void setFilesystem(InteractiveSpacesFilesystem filesystem) {
		this.filesystem = filesystem;
	}

	/**
	 * The network type for Interactive Spaces.
	 * 
	 * <p>
	 * This allows distinguishing between Interactive Spaces networks, e.g.
	 * localdev, prod, fredbot.
	 * 
	 * @param networkType
	 */
	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	/**
	 * Set the logger to use.
	 * 
	 * @param log
	 */
	public void setLog(Log log) {
		this.log = log;
	}

	/**
	 * @param executorService
	 *            the executorService to set
	 */
	public void setExecutorService(ScheduledExecutorService executorService) {
		this.executorService = executorService;
	}

	/**
	 * @param systemConfiguration
	 *            the systemConfiguration to set
	 */
	public void setSystemConfiguration(Configuration systemConfiguration) {
		this.systemConfiguration = systemConfiguration;
	}

	/**
	 * @param timeProvider the timeProvider to set
	 */
	public void setTimeProvider(TimeProvider timeProvider) {
		this.timeProvider = timeProvider;
	}
}
