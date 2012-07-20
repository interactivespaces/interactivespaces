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

package interactivespaces.launcher.bootstrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * The boostrapper for Interactive Spaces.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesFrameworkBootstrap {

	/**
	 * Extensions on config files.
	 */
	private static final String CONFIGURATION_FILES_EXTENSION = ".conf";

	/**
	 * Where the OSGi framework launcher can be found.
	 */
	private static final String OSGI_FRAMEWORK_LAUNCH_FRAMEWORK_FACTORY = "META-INF/services/org.osgi.framework.launch.FrameworkFactory";

	/**
	 * Subdirectory which will contain the bootstrap bundles.
	 */
	public static final String BUNDLE_DIRECTORY_BOOTSTRAP = "bootstrap";

	/**
	 * Where the config files are stored.
	 */
	public static final String CONFIG_DIRECTORY = "config";

	/**
	 * The OSGI framework which has been started.
	 */
	private Framework framework;

	/**
	 * All bundles installed.
	 */
	private Set<Bundle> bundles = new HashSet<Bundle>();

	/**
	 * The initial set of bundles to load.
	 */
	private List<File> initialBundles;

	/**
	 * The final set of bundles to load.
	 */
	private List<File> finalBundles;

	/**
	 * Whether or not the OSGi shell is needed.
	 */
	private boolean needShell = true;

	/**
	 * Boot the framework.
	 */
	public void boot(List<String> args) {
		// TODO(keith): Better command line processing
		needShell = !args.contains("--noshell");

		getBootstrapBundleJars(BUNDLE_DIRECTORY_BOOTSTRAP);

		// If no bundle JAR files are in the directory, then exit.
		if (initialBundles.isEmpty() && finalBundles.isEmpty()) {
			System.out.println("No bundles to install.");
		} else {
			setupShutdownHandler();

			try {
				createAndStartFramework(args);
				List<Bundle> bundleList = new ArrayList<Bundle>();

				// Install bundle JAR files and remember the bundle objects.
				BundleContext ctxt = framework.getBundleContext();

				startBundles(ctxt, bundleList, initialBundles);
				startBundles(ctxt, bundleList, finalBundles);

				// Wait for framework to stop.
				framework.waitForStop(0);
				System.exit(0);

			} catch (Exception ex) {
				System.err.println("Error starting framework: " + ex);
				ex.printStackTrace();
				System.exit(0);
			}
		}
	}

	/**
	 * @param ctxt
	 * @param bundleList
	 * @param jars
	 * @throws BundleException
	 */
	protected void startBundles(BundleContext ctxt, List<Bundle> bundleList,
			List<File> jars) throws BundleException {
		for (File bundleFile : jars) {
			String bundleUri = bundleFile.toURI().toString();
			// System.out.println("Installing " + bundleUri);
			Bundle b = ctxt.installBundle(bundleUri);
			bundleList.add(b);
			bundles.add(b);
		}

		// Start all installed non-fragment bundles.
		for (final Bundle bundle : bundleList) {
			if (!isFragment(bundle)) {
				// TODO(keith): See if way to start up shell from property
				// since we may want it for remote access.
				if (bundle.getLocation().contains("gogo") && !needShell) {
					continue;
				}

				startBundle(bundle);
			}
		}
	}

	/**
	 * Start a particular bundle.
	 * 
	 * @param bundle
	 *            the bundle to start
	 */
	protected void startBundle(Bundle bundle) {
		boolean started;
		try {
			// System.out.println("Starting " + bundle.getLocation());
			bundle.start();
			// System.out.println("Started " + bundle.getLocation());
		} catch (Exception e) {
			started = false;
			System.err.println("Exception " + bundle.getLocation());
			e.printStackTrace();
		}
	}

	/**
	 * Create, configure, and start an OSGi framework instance. OO
	 * 
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * @throws BundleException
	 */
	private void createAndStartFramework(List<String> args) throws IOException,
			Exception, BundleException {
		Map<String, Object> m = new HashMap<String, Object>();

		// m.putAll(System.getProperties());
		m.put(Constants.FRAMEWORK_STORAGE_CLEAN, "onFirstInit");

		String delegations = getClassloaderDelegations();
		if (delegations != null) {
			m.put(Constants.FRAMEWORK_BOOTDELEGATION, delegations);
		}

		m.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "log4j.properties");

		File file = new File("plugins-cache");
		m.put(Constants.FRAMEWORK_STORAGE, file.getCanonicalPath());

		// m.put("org.apache.felix.http.enable", "true");
		// m.put("org.apache.felix.http.jettyEnabled", "true");
		// m.put("org.osgi.service.http.port", "80");

		loadPropertyFiles(CONFIG_DIRECTORY, m);

		StringBuilder argsString = new StringBuilder();
		if (!args.isEmpty()) {
			argsString.append(args.get(0));
			for (int i = 1; i < args.size(); i++) {
				argsString.append(' ').append(args.get(i));
			}
		}

		m.put("interactiveSpacesLaunchArgs", argsString.toString());

		// m.put(Constants.FRAME, value)
		framework = getFrameworkFactory().newFramework(m);
		framework.start();
	}

	/**
	 * 
	 */
	private void setupShutdownHandler() {
		// Want the framework to shut down cleanly no matter how the VM is
		// exitted.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					if (framework != null) {
						framework.stop();
						framework.waitForStop(0);
					}
				} catch (Exception ex) {
					System.err.println("Error stopping framework: " + ex);
				}
			}
		});
	}

	/**
	 * @param args
	 * @return
	 */
	private void getBootstrapBundleJars(String bootstrapFolder) {
		// Look in the specified bundle directory to create a list
		// of all JAR files to install.
		File[] files = new File(bootstrapFolder)
				.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						String filename = name.toLowerCase();
						return filename.endsWith(".jar")
								|| filename.endsWith(".war");
					}
				});
		initialBundles = new ArrayList<File>();
		finalBundles = new ArrayList<File>();
		for (File f : files) {
			if (f.getName().toLowerCase().endsWith("war")) {
				finalBundles.add(f);
			} else {
				initialBundles.add(f);
			}
		}
	}

	/**
	 * Is the bundle a fragment host?
	 * 
	 * @param bundle
	 * @return
	 */
	private boolean isFragment(Bundle bundle) {
		return bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null;
	}

	/**
	 * Simple method to parse META-INF/services file for framework factory.
	 * Currently, it assumes the first non-commented line is the class nodeName
	 * of the framework factory implementation.
	 * 
	 * @return The created <tt>FrameworkFactory</tt> instance.
	 * @throws Exception
	 *             if any errors occur.
	 **/
	private FrameworkFactory getFrameworkFactory() throws Exception {
		// using the ServiceLoader to get a factory.
		ClassLoader classLoader = InteractiveSpacesFrameworkBootstrap.class
				.getClassLoader();
		URL url = classLoader
				.getResource(OSGI_FRAMEWORK_LAUNCH_FRAMEWORK_FACTORY);
		if (url != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					url.openStream()));
			try {
				for (String s = br.readLine(); s != null; s = br.readLine()) {
					s = s.trim();
					// Try to load first non-empty, non-commented line.
					if ((s.length() > 0) && (s.charAt(0) != '#')) {
						return (FrameworkFactory) classLoader.loadClass(s)
								.newInstance();
					}
				}
			} finally {
				if (br != null)
					br.close();
			}
		}

		throw new Exception("Could not find framework factory.");
	}

	private void loadPropertyFiles(String configFolder,
			Map<String, Object> properties) {
		// Look in the specified bundle directory to create a list
		// of all JAR files to install.
		File[] files = new File(configFolder).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(
						CONFIGURATION_FILES_EXTENSION);
			}
		});
		if (files == null || files.length == 0) {
			System.err.format("Couldn't load config files from %s\n",
					configFolder);
			return;
		}

		for (File file : files) {
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(file));
				for (Entry<Object, Object> p : props.entrySet()) {
					properties.put((String) p.getKey(), (String) p.getValue());
				}
			} catch (IOException e) {
				System.err.format("Couldn't load config file %s\n", file);
			}
		}
	}

	private String getClassloaderDelegations() {
		File delegation = new File("lib/system/java/delegations.conf");
		if (delegation.exists()) {

			StringBuilder builder = new StringBuilder();
			String separator = "";

			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(delegation));

				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(separator).append(line);
					separator = ",";
				}

				return builder.toString();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (reader != null)
					try {
						reader.close();
					} catch (IOException e) {
						// Don't care. Closing.
					}
			}
		}

		return null;
	}
}
