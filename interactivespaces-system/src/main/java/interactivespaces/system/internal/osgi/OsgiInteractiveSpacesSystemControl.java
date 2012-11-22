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

import interactivespaces.system.InteractiveSpacesSystemControl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * An OSGi version of {@link InteractiveSpacesSystemControl}.
 *
 * @author Keith M. Hughes
 */
public class OsgiInteractiveSpacesSystemControl implements InteractiveSpacesSystemControl {

	/**
	 * The bundle the control is part of.
	 */
	private BundleContext bundleContext;
	
	public OsgiInteractiveSpacesSystemControl(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	@Override
	public void shutdown() {
		try {
			bundleContext.getBundle(0).stop();
		} catch (BundleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
