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

package interactivespaces.controller.activity.wrapper.internal.interactivespaces;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.activity.wrapper.BaseActivityWrapper;
import interactivespaces.controller.activity.wrapper.ActivityWrapper;

import java.io.File;

import org.apache.commons.logging.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * A {@link ActivityWrapper} which works with an OSGi container.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesNativeActivityWrapper extends BaseActivityWrapper {

	/**
	 * Configuration property giving the Java class.
	 */
	public static final String CONFIGURATION_APPLICATION_JAVA_CLASS = "space.activity.java.class";

	/**
	 * Executable for the activity.
	 */
	private File executable;

	/**
	 * Name of the class to run from the bundle.
	 */
	private String className;

	/**
	 * The bundle containing the activity.
	 */
	private Bundle bundle;

	/**
	 * The context we use for loading in a bundle.
	 */
	private BundleContext starterBundle;

	/**
	 * When the file of the loaded bundle was last modified.
	 */
	private long lastModified;

	/**
	 * Log to use for reporting errors.
	 */
	private Log log;

	public InteractiveSpacesNativeActivityWrapper(BundleContext starterBundle,
			File executable, Configuration configuration, Log log) {
		this.starterBundle = starterBundle;
		this.executable = executable;
		this.className = configuration
				.getRequiredPropertyString(CONFIGURATION_APPLICATION_JAVA_CLASS);
		this.log = log;
	}

	@Override
	public synchronized void destroy() {
		try {
			bundle.uninstall();
			bundle = null;
		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Could not unload bundle at %s", bundle.getLocation()), e);
		}
	}

	@Override
	public synchronized Activity newInstance() {
		prepare();
		try {
			return (Activity) bundle.loadClass(className).newInstance();
		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Could not create class %s from bundle at %s", className,
					bundle.getLocation()), e);
		}
	}

	/**
	 * Prepare an instance.
	 * 
	 * <p>
	 * This method will do nothing if there is no need to. But if the bundle has
	 * changed, it will destroy the current version and load a new one.
	 */
	private void prepare() {
		try {
			// If there is a bundle loaded and it hasn't been modified,
			// no reason to load.
			if (bundle != null) {
				if (executable.lastModified() > lastModified) {
					destroy();
				} else {
					return;
				}
			}
			String bundleUri = executable.toURI().toString();

			bundle = starterBundle.installBundle(bundleUri);
			lastModified = executable.lastModified();

			bundle.start();
		} catch (BundleException e) {
			throw new InteractiveSpacesException(String.format(
					"Cannot load bundle %s", executable.getAbsolutePath()), e);
		}
	}
}
