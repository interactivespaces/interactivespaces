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

package interactivespaces.android.service;

import interactivespaces.system.core.logging.LoggingProvider;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A logging provider for Android.
 * 
 * @author Keith M. Hughes
 */
public class AndroidLoggingProvider implements LoggingProvider {

	/**
	 * Map of logging levels to priorities.
	 */
	private static Map<String, Integer> levelPriorities = new HashMap<String, Integer>();

	private static final int LOG_PRIORITY_OFF = 0;
	private static final int LOG_PRIORITY_FATAL = 1;
	private static final int LOG_PRIORITY_ERROR = 2;
	private static final int LOG_PRIORITY_WARN = 3;
	private static final int LOG_PRIORITY_INFO = 4;
	private static final int LOG_PRIORITY_DEBUG = 5;
	private static final int LOG_PRIORITY_TRACE = 6;

	static {
		levelPriorities.put(LoggingProvider.LOG_LEVEL_OFF, LOG_PRIORITY_OFF);
		levelPriorities
				.put(LoggingProvider.LOG_LEVEL_FATAL, LOG_PRIORITY_FATAL);
		levelPriorities.put(LOG_LEVEL_ERROR, LOG_PRIORITY_ERROR);
		levelPriorities.put(LOG_LEVEL_WARN, LOG_PRIORITY_WARN);
		levelPriorities.put(LOG_LEVEL_INFO, LOG_PRIORITY_INFO);
		levelPriorities.put(LOG_LEVEL_DEBUG, LOG_PRIORITY_DEBUG);
		levelPriorities.put(LOG_LEVEL_TRACE, LOG_PRIORITY_TRACE);
	}

	/**
	 * The containerwide logger.
	 */
	private Log log = new AndroidLog("interactivespaces");

	@Override
	public Log getLog() {
		return log;
	}

	@Override
	public Log getLog(String name, String level) {
		Integer priority = levelPriorities.get(level.toLowerCase());
		if (priority != null) {
			return new AndroidLog("interactivespaces-" + name, priority);
		} else {
			String message = String.format("Illegal log priority level %s",
					level);
			log.error(message);

			throw new RuntimeException(message);
		}
	}

	@Override
	public boolean modifyLogLevel(Log log, String level) {
		if (AndroidLog.class.isAssignableFrom(log.getClass())) {

			Integer l = levelPriorities.get(level.toLowerCase());
			if (l != null) {
				((AndroidLog) log).setLevel(l);

				return true;
			} else {
				log.error(String.format("Unknown log level %s", level));
			}
		} else {
			log.error("Attempt to modify an unmodifiable logger");
		}

		return false;
	}

	private static class AndroidLog implements Log {

		/**
		 * Name of the log.
		 */
		private String name;
		
		/**
		 * Priority of the log.
		 */
		private int levelPriority;

		public AndroidLog(String name) {
			this(name, LOG_PRIORITY_INFO);
		}

		public AndroidLog(String name, int levelPriority) {
			this.name = name;
			this.levelPriority = levelPriority;
		}

		@Override
		public void debug(Object arg0) {
			if (isDebugEnabled()) {
				android.util.Log.d(name, arg0.toString());
			}
		}

		@Override
		public void debug(Object arg0, Throwable arg1) {
			if (isDebugEnabled()) {
				android.util.Log.d(name, arg0.toString(), arg1);
			}
		}

		@Override
		public void error(Object arg0) {
			if (isErrorEnabled()) {
				android.util.Log.e(name, arg0.toString());
			}
		}

		@Override
		public void error(Object arg0, Throwable arg1) {
			if (isErrorEnabled()) {
				android.util.Log.e(name, arg0.toString(), arg1);
			}
		}

		@Override
		public void fatal(Object arg0) {
			if (isFatalEnabled()) {
				android.util.Log.wtf(name, arg0.toString());
			}
		}

		@Override
		public void fatal(Object arg0, Throwable arg1) {
			if (isFatalEnabled()) {
				android.util.Log
						.wtf(name, arg0.toString(), arg1);
			}
		}

		@Override
		public void info(Object arg0) {
			if (isInfoEnabled()) {
				android.util.Log.i(name, arg0.toString());
			}
		}

		@Override
		public void info(Object arg0, Throwable arg1) {
			if (isInfoEnabled()) {
				android.util.Log.i(name, arg0.toString(), arg1);
			}
		}

		@Override
		public boolean isDebugEnabled() {
			return levelPriority <= LOG_PRIORITY_DEBUG
					&& LOG_PRIORITY_OFF != levelPriority;
		}

		@Override
		public boolean isErrorEnabled() {
			return levelPriority <= LOG_PRIORITY_ERROR
					&& LOG_PRIORITY_OFF != levelPriority;
		}

		@Override
		public boolean isFatalEnabled() {
			return levelPriority <= LOG_PRIORITY_FATAL
					&& LOG_PRIORITY_OFF != levelPriority;
		}

		@Override
		public boolean isInfoEnabled() {
			return levelPriority <= LOG_PRIORITY_INFO
					&& LOG_PRIORITY_OFF != levelPriority;
		}

		@Override
		public boolean isTraceEnabled() {
			return levelPriority <= LOG_PRIORITY_TRACE
					&& LOG_PRIORITY_OFF != levelPriority;
		}

		@Override
		public boolean isWarnEnabled() {
			return levelPriority <= LOG_PRIORITY_WARN
					&& LOG_PRIORITY_OFF != levelPriority;
		}

		@Override
		public void trace(Object arg0) {
			if (isTraceEnabled()) {
				android.util.Log.d(name, arg0.toString());
			}
		}

		@Override
		public void trace(Object arg0, Throwable arg1) {
			if (isTraceEnabled()) {
				android.util.Log.d(name, arg0.toString(), arg1);
			}
		}

		@Override
		public void warn(Object arg0) {
			if (isWarnEnabled()) {
				android.util.Log.w(name, arg0.toString());
			}
		}

		@Override
		public void warn(Object arg0, Throwable arg1) {
			if (isWarnEnabled()) {
				android.util.Log.w(name, arg0.toString(), arg1);
			}
		}

		public void setLevel(int priority) {
			this.levelPriority = priority;
		}
	}
}
