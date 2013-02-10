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

package interactivespaces.time;

/**
 * A {@link TimeProvider} where the time is set externally.
 * 
 * <p>
 * Usually used for testing.
 * 
 * @author Keith M. Hughes
 */
public class SettableTimeProvider implements TimeProvider {

	/**
	 * The time to return.
	 */
	private long currentTime;

	@Override
	public void startup() {
		// Nothing to do
	}

	@Override
	public void shutdown() {
		// Nothing to do
	}

	@Override
	public long getCurrentTime() {
		return currentTime;
	}

	/**
	 * Set the current time for the provider.
	 * 
	 * @param currentTime
	 *            the current time in milliseconds
	 */
	public void setCurrentTime(long currentTime) {
		this.currentTime = currentTime;
	}
}
