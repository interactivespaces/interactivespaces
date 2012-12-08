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

package interactivespaces.workbench.osgi;

import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.ui.WorkbenchUi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import com.google.common.collect.Lists;

/**
 * OSGi activator for the Interactive Spaces Workbench.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesWorkbenchActivator implements BundleActivator {

	/**
	 * The workbench UI, if any.
	 */
	private WorkbenchUi ui;

	/**
	 * The workbench properties
	 */
	private Properties workbenchProperties;

	/**
	 * The context for the workbench's OSGi bundle.
	 */
	private BundleContext bundleContext;

	/**
	 * The args handed in.
	 */
	private String[] argList;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		this.bundleContext = bundleContext;

		try {
			String args = bundleContext
					.getProperty("interactiveSpacesLaunchArgs");

			args = args.trim();
			if (!args.isEmpty()) {
				argList = args.split("\\s");
			}

			prepare();
			run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void stop(BundleContext arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * prepare to start the workbench.
	 */
	public void prepare() {
		workbenchProperties = new Properties();

		File[] configFiles = new File("config").listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".conf");
			}
		});

		if (configFiles != null) {
			for (File configFile : configFiles) {
				try {
					workbenchProperties.load(new FileInputStream(configFile));
				} catch (Exception e) {
					System.out.format("Could not open config file %s\n", configFile);
				}
			}
		}

	}

	/**
	 * Start the workbench running.
	 */
	public void run() {
		InteractiveSpacesWorkbench workbench = new InteractiveSpacesWorkbench(
				workbenchProperties);

		if (argList != null) {
			try {
				workbench.doCommands(Lists.newArrayList(argList));
			} finally {
				try {
					bundleContext.getBundle(0).stop();
				} catch (BundleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			ui = new WorkbenchUi(workbench);
		}
	}
}
