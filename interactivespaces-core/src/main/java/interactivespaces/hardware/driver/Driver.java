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

package interactivespaces.hardware.driver;

import interactivespaces.configuration.Configuration;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.resource.ManagedResource;

import org.apache.commons.logging.Log;

/**
 * A driver for a hardware device.
 * 
 * @author Keith M. Hughes
 */
public interface Driver extends ManagedResource {

	/**
	 * Prepare the driver for startup.
	 * 
	 * @param spacesEnvironment
	 *            the space environment the driver will run under
	 * @param configuration
	 *            the configuration the driver should run under
	 * @param log
	 *            the log which should be used for the driver
	 */
	void prepare(InteractiveSpacesEnvironment spacesEnvironment,
			Configuration configuration, Log log);
}
