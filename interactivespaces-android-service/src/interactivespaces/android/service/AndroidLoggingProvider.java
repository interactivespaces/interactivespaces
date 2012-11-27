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

import org.apache.commons.logging.Log;

/**
 * A logging provider for Android.
 *
 * @author Keith M. Hughes
 */
public class AndroidLoggingProvider implements LoggingProvider {
	
	/**
	 * The containerwide logger.
	 */
	private Log log = new AndroidLog();
	
	@Override
	public Log getLog() {
		return log;
	}

	@Override
	public Log getLog(String name, String level) {
		// For now just return the container logger
		return log;
	}

	@Override
	public boolean modifyLogLevel(Log log, String level) {
		// Can't modify levels
		return false;
	}

	private static class AndroidLog implements Log {
		
		private String level;

		@Override
		public void debug(Object arg0) {
			android.util.Log.d("interactivespaces", arg0.toString());
		}

		@Override
		public void debug(Object arg0, Throwable arg1) {
			android.util.Log.d("interactivespaces", arg0.toString(), arg1);
		}

		@Override
		public void error(Object arg0) {
			android.util.Log.e("interactivespaces", arg0.toString());
		}

		@Override
		public void error(Object arg0, Throwable arg1) {
			android.util.Log.e("interactivespaces", arg0.toString(), arg1);
		}

		@Override
		public void fatal(Object arg0) {
			android.util.Log.wtf("interactivespaces", arg0.toString());
		}

		@Override
		public void fatal(Object arg0, Throwable arg1) {
			android.util.Log.wtf("interactivespaces", arg0.toString(), arg1);
		}

		@Override
		public void info(Object arg0) {
			android.util.Log.i("interactivespaces", arg0.toString());
		}

		@Override
		public void info(Object arg0, Throwable arg1) {
			android.util.Log.i("interactivespaces", arg0.toString(), arg1);
		}

		@Override
		public boolean isDebugEnabled() {
			return true;
		}

		@Override
		public boolean isErrorEnabled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isFatalEnabled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isInfoEnabled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isTraceEnabled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isWarnEnabled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void trace(Object arg0) {
			android.util.Log.d("interactivespaces", arg0.toString());
		}

		@Override
		public void trace(Object arg0, Throwable arg1) {
			android.util.Log.d("interactivespaces", arg0.toString(), arg1);
		}

		@Override
		public void warn(Object arg0) {
			android.util.Log.w("interactivespaces", arg0.toString());
		}

		@Override
		public void warn(Object arg0, Throwable arg1) {
			android.util.Log.w("interactivespaces", arg0.toString(), arg1);
		}
		
		public void setLevel(String level) {
			this.level = level;
		}
		
	}
}
