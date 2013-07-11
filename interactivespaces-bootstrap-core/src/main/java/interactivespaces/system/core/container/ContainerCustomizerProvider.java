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

package interactivespaces.system.core.container;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A container customizer.
 *
 * <p>
 * Can have useful information about things added by the container.
 *
 * @author Keith M. Hughes
 */
public interface ContainerCustomizerProvider {

	/**
	 * Get the command line arguments from the container.
	 *
	 * @return the command line arguments
	 */
	List<String> getCommandLineArguments();

	/**
	 * Get services supplied by the container.
	 *
	 * <p>
	 * These are not using the Service interface because of how classpaths and
	 * IS components are being started.
	 *
	 * @return a map of services with the key being the service name and the
	 *         value being the service
	 */
	Map<String, Object> getServices();

	/**
	 * Is the container supposed to be controllable from a file system standpoint?
	 *
	 * @return {@code true} if the container is file controllable
	 */
	boolean isFileControllable();

	/**
	 * Get the bundles available to the OSGi container as it started up.
	 *
	 * @return
	 */
	Set<File> getStartupBundles();
}
