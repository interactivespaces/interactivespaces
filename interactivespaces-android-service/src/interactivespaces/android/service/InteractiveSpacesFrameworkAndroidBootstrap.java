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

import interactivespaces.controller.internal.osgi.OsgiControllerActivator;
import interactivespaces.system.bootstrap.osgi.GeneralInteractiveSpacesSupportActivator;
import interactivespaces.system.core.logging.LoggingProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;

import android.content.Context;
import android.content.res.AssetManager;

/**
 * A bootstrapper for Interactive Spaces on an Android device.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesFrameworkAndroidBootstrap {

	/**
	 * Extensions on config files.
	 */
	private static final String CONFIGURATION_FILES_EXTENSION = ".conf";

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
	private List<String> initialBundles;

	/**
	 * The final set of bundles to load.
	 */
	private List<String> finalBundles;

	/**
	 * Whether or not the OSGi shell is needed.
	 */
	private boolean needShell = true;

	private Map<String, Bundle> jarToBundle = new HashMap<String, Bundle>();

	private GeneralInteractiveSpacesSupportActivator isSystemActivator;

	/**
	 * Logging provider for the container.
	 */
	private AndroidLoggingProvider loggingProvider;

	/**
	 * Boot the framework.
	 */
	public void boot(List<String> args, Context context) {
		AssetManager assetManager = context.getAssets();
		// TODO(keith): Better command line processing
		needShell = !args.contains("--noshell");
		needShell = false;
		try {
			createCoreServices();

			copyInitialBootstrapAssets(assetManager, context.getFilesDir());

			getBootstrapBundleJars(assetManager, BUNDLE_DIRECTORY_BOOTSTRAP);

			// If no bundle JAR files are in the directory, then exit.
			if (initialBundles.isEmpty() && finalBundles.isEmpty()) {
				System.out.println("No bundles to install.");
			}
			// setupShutdownHandler();

			createAndStartFramework(args, context);

			registerCoreServices();

			List<Bundle> bundleList = new ArrayList<Bundle>();

			// Install bundle JAR files and remember the bundle objects.
			BundleContext ctxt = framework.getBundleContext();
			ctxt.addBundleListener(new BundleListener() {

				@Override
				public void bundleChanged(BundleEvent event) {
					bundleChangeEvent(event);
				}

			});

			// Wait for framework to stop.
			// framework.waitForStop(0);
			// System.exit(0);

			isSystemActivator = new GeneralInteractiveSpacesSupportActivator();
			isSystemActivator.start(framework.getBundleContext());

			OsgiControllerActivator isControllerActivator = new OsgiControllerActivator();
			isControllerActivator.start(framework.getBundleContext());

			startBundles(assetManager, ctxt, bundleList, initialBundles, false);
			startBundles(assetManager, ctxt, bundleList, finalBundles, true);

		} catch (Exception ex) {
			System.err.println("Error starting framework: " + ex);
			ex.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Create the core services to the base bundle which are platform dependent.
	 */
	public void createCoreServices() {
		loggingProvider = new AndroidLoggingProvider();
	}

	/**
	 * Register all bootstrap core services with the container.
	 */
	public void registerCoreServices() {
		framework.getBundleContext().registerService(LoggingProvider.class.getName(),
				loggingProvider, null);
	}

	/**
	 * An event happened on one of the bundles in the container.
	 * 
	 * @param event
	 *            the OSGi bundle event
	 */
	private void bundleChangeEvent(BundleEvent event) {
		System.out.format("Bundle %s changed state to %d\n", event.getBundle()
				.getLocation(), event.getBundle().getState());
	}

	/**
	 * @param ctxt
	 * @param bundleList
	 * @param jars
	 * @throws BundleException
	 */
	private void startBundles(AssetManager assetManager, BundleContext ctxt,
			List<Bundle> bundleList, List<String> jars, boolean start)
			throws BundleException {
		for (String bundleFile : jars) {

			// System.out.println("Installing " + bundleUri);
			try {
				Bundle b = ctxt.installBundle(bundleFile,
						assetManager.open(bundleFile));

				bundleList.add(b);
				bundles.add(b);
				jarToBundle.put(
						bundleFile.substring(bundleFile.indexOf('/') + 1), b);
				System.out.format("Added bundle file %s with ID %d\n",
						bundleFile, b.getBundleId());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Start all installed non-fragment bundles.
		for (final Bundle bundle : bundleList) {
			if (!isFragment(bundle)) {
				// TODO(keith): See if way to start up shell from property
				// since we may want it for remote access.
				if ((bundle.getLocation().contains("gogo") && !needShell)
						|| bundle.getLocation().contains("netty")) {
					continue;
				}

				if (start)
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
		boolean started = true;
		try {
			System.out.format("Starting %s\n", bundle.getLocation());
			bundle.start();
			System.out.format("Started %s\n", bundle.getLocation());
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
	private void createAndStartFramework(List<String> args, Context context)
			throws IOException, Exception, BundleException {
		Map<String, String> m = new HashMap<String, String>();

		File filesDir = context.getFilesDir();
		m.put("interactivespaces.rootdir", filesDir.getAbsolutePath());

		// m.putAll(System.getProperties());
		m.put(Constants.FRAMEWORK_STORAGE_CLEAN,
				Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

		// m.put(Constants.FRAMEWORK_BUNDLE_PARENT,
		// Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);BaseSpace
		// System.err.format("Setting %s to %s\n",
		// Constants.FRAMEWORK_BUNDLE_PARENT,
		// m.get(Constants.FRAMEWORK_BUNDLE_PARENT));
		String delegations = getClassloaderDelegations(filesDir);
		if (delegations != null) {
			m.put(Constants.FRAMEWORK_BOOTDELEGATION, delegations);
		}

		String packages = "org.osgi.framework; version=1.5, org.osgi.service.event; org.osgi.service.startlevel; org.osgi.service.log; org.osgi.util.tracker; org.apache.felix.service.command; org.osgi.service.packageadmin; version=1.2.0, javax.xml; javax.xml.xpath; javax.xml.transform.sax ; javax.net; javax.net.ssl; javax.xml.bind; javax.crypto; javax.management; javax.script; javax.xml.datatype; javax.xml.namespace; javax.xml.parsers; javax.crypto.spec; javax.security.auth.callback; javax.naming; javax.management.openmbean; javax.xml.transform; javax.xml.transform.stream; javax.xml.transform.dom; org.w3c.dom; org.xml.sax; org.xml.sax.helpers; org.ietf.jgss; javax.security.sasl; javax.sql; org.xml.sax.ext; javax.security.auth.x500; javax.swing; javax.swing.border; javax.swing.event; javax.swing.table; javax.swing.text; javax.swing.tree";
		packages += "interactivespaces.system.core.logging; ";
		packages += "; com.google.common.collect; interactivespaces; interactivespaces.activity; ";
		packages += "interactivespaces.configuration; ";
		packages += "interactivespaces.controller; ";
		packages += "interactivespaces.controller.activity.installation; ";
		packages += "interactivespaces.domain.basic; ";
		packages += "interactivespaces.domain.basic.pojo; ";
		packages += "interactivespaces.evaluation; ";
		packages += "interactivespaces.master.server.remote.client; ";
		packages += "interactivespaces.master.server.remote.client.ros; ";
		packages += "interactivespaces.system; ";
		packages += "interactivespaces.time; ";
		packages += "org.apache.commons.logging; version=1.1.1, ";
		packages += "org.apache.commons.logging.impl; version=1.1.1, ";
		packages += "interactivespaces.activity.execution; ";
		packages += "interactivespaces.activity.impl; ";
		packages += "interactivespaces.activity.impl.binary; ";
		packages += "interactivespaces.activity.impl.ros; ";
		packages += "interactivespaces.activity.impl.web; ";
		packages += "interactivespaces.activity.binary; ";
		packages += "interactivespaces.activity.component; ";
		packages += "interactivespaces.activity.component.ros; ";
		packages += "interactivespaces.activity.component.web; ";
		packages += "interactivespaces.event; ";
		packages += "interactivespaces.event.trigger; ";
		packages += "interactivespaces.util; ";
		packages += "interactivespaces.util.concurrency; ";
		packages += "interactivespaces.util.data; ";
		packages += "interactivespaces.util.data.persist; ";
		packages += "interactivespaces.util.io; ";
		packages += "interactivespaces.util.process.restart; ";
		packages += "interactivespaces.util.ros; ";
		packages += "interactivespaces.util.uuid; ";
		packages += "interactivespaces.util.web; ";
		packages += "interactivespaces.service.web.server; ";
		packages += "org.ros.osgi.common; ";
		packages += "org.ros.node; ";
		packages += "org.ros.node.topic; ";
		packages += "org.ros.message; ";
		packages += "org.ros.message.interactivespaces_msgs; version=0.0.0";
		m.put(Constants.FRAMEWORK_SYSTEMPACKAGES, packages);

		m.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "log4j.properties");

		File file = new File(filesDir, "plugins-cache");
		file.mkdirs();
		m.put(Constants.FRAMEWORK_STORAGE, file.getCanonicalPath());

		m.put("felix.service.urlhandlers", "false");
		
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
		framework = new Felix(m);
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
	 * @throws IOException
	 */
	private void getBootstrapBundleJars(AssetManager assetManager,
			String bootstrapFolderPath) throws IOException {
		// Look in the specified bundle directory to create a list
		// of all JAR files to install.
		initialBundles = new ArrayList<String>();
		finalBundles = new ArrayList<String>();

		List<String> bootstrapFiles = new ArrayList<String>();
		for (String filename : assetManager.list(bootstrapFolderPath)) {
			if (filename.endsWith(".jar")) {
				if (filename.startsWith("interactivespaces")) {
					finalBundles.add(bootstrapFolderPath + "/" + filename);
				} else {
					initialBundles.add(bootstrapFolderPath + "/" + filename);
				}
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
	 * Load all config files from the configuration folder.
	 * 
	 * @param configFolder
	 *            the configuration
	 * @param properties
	 *            where the properties from the configurations will be placed
	 */
	private void loadPropertyFiles(String configFolder,
			Map<String, String> properties) {
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

	private String getClassloaderDelegations(File filesDir) {
		File delegation = new File(filesDir, "lib/system/java/delegations.conf");
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

	/**
	 * Copy all asserts needed for initial boot.
	 * 
	 * @param assetManager
	 *            the android asset manager for the application
	 * @param filesDir
	 *            the folder where items can be copied to
	 */
	private void copyInitialBootstrapAssets(AssetManager assetManager,
			File filesDir) {
		try {
			File confDir = new File(filesDir, "config");
			confDir.mkdirs();
			File isConfigDir = new File(confDir, "interactivespaces");
			isConfigDir.mkdirs();
			File bootstrapDir = new File(filesDir, "bootstrap");
			bootstrapDir.mkdirs();
			File systemLibDir = new File(filesDir, "lib/system/java");
			systemLibDir.mkdirs();
			File logsDir = new File(filesDir, "logs");
			logsDir.mkdirs();
			File activitiesStagingDir = new File(filesDir, "controller/activities/staging");
			activitiesStagingDir.mkdirs();
			File activitiesInstalledDir = new File(filesDir, "controller/activities/installed");
			activitiesInstalledDir.mkdirs();

			copyAssetFile("config/container.conf", new File(confDir,
					"container.conf"), assetManager);

			String[] configFiles = assetManager
					.list("config/interactivespaces");
			for (String configFile : configFiles) {
				copyAssetFile("config/interactivespaces/" + configFile,
						new File(isConfigDir, configFile), assetManager);
			}

			copyAssetFile("lib/system/java/log4j.properties", new File(
					systemLibDir, "log4j.properties"), assetManager);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Copy an asset file.
	 * 
	 * @param inputStream
	 *            the input stream for the asset
	 * @param outputFile
	 *            where the file should be copied
	 * @param assetManager
	 *            TODO
	 */
	private void copyAssetFile(String input, File outputFile,
			AssetManager assetManager) {

		System.out.format("Copying %s to %s\n", input, outputFile);

		byte buf[] = new byte[1024];
		int len;
		FileOutputStream fos = null;
		InputStream inputStream = null;
		try {
			inputStream = assetManager.open(input);
			fos = new FileOutputStream(outputFile);

			while ((len = inputStream.read(buf)) != -1) {
				fos.write(buf, 0, len);
			}
			fos.flush();
		} catch (IOException e) {

		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// Don't care
				}
			}

			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// Don't care
				}
			}
		}
	}
}
