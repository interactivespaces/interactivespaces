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

package interactivespaces.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * An OSGI launcher for Interactive Spaces.
 * 
 * @author Keith M. Hughes
 */
public class InteractiveSpacesLauncher {

	private static final String EXTRA_SYSTEM_JARS_CONF = "extrasystemjars.conf";
	private static final String SPACES_LIB_JAVA_SYSTEM = "lib/system/java";
	private ClassLoader classLoader;
	private File pidFile;

	public static void main(String[] args) throws Exception {
		InteractiveSpacesLauncher launcher = new InteractiveSpacesLauncher();
		launcher.launch(args);
	}

	/**
	 * Launch Interactive Spaces
	 */
	public void launch(String[] args) {
		if (writePid()) {
			createClassLoader();
			boostrap(args);
		}
	}

	/**
	 * Create the classloader to use to start the system.
	 */
	private void createClassLoader() {
		File systemDirectory = new File(SPACES_LIB_JAVA_SYSTEM);

		List<URL> urls = collectSystemLibClasspath(systemDirectory);
		addExtraClasspath(systemDirectory, urls);

		classLoader = new URLClassLoader(urls.toArray(new URL[0]));
	}

	/**
	 * Get the classpath to be used for starting the system.
	 * 
	 * @return
	 */
	private List<URL> collectSystemLibClasspath(File systemDirectory) {
		List<URL> urls = new ArrayList<URL>();
		File[] files = systemDirectory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getName().equals(EXTRA_SYSTEM_JARS_CONF)) {
					continue;
				}

				try {
					URL url = file.toURI().toURL();
					urls.add(url);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}

		if (urls.isEmpty()) {
			System.err.format("No bootstrap files found in %s",
					systemDirectory.getAbsolutePath());
		}

		return urls;
	}

	/**
	 * If the {@link #SYSTEMJARS_CONF} exists in the system directory, extend
	 * the classpath.
	 * 
	 * @param systemDirectory
	 *            the system directory
	 * @param urls
	 *            the collection of URLs for the classpath.
	 */
	private void addExtraClasspath(File systemDirectory, List<URL> urls) {
		File systemJarsFile = new File(systemDirectory, EXTRA_SYSTEM_JARS_CONF);
		if (systemJarsFile.exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(systemJarsFile));

				String line = null;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (!line.isEmpty()) {
						File file = new File(line);
						if (file.exists()) {
							try {
								urls.add(file.toURI().toURL());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Bootstrap the framework.
	 */
	protected void boostrap(String[] args) {
		try {
			Class<?> bootstrapClass = classLoader
					.loadClass("interactivespaces.launcher.bootstrap.InteractiveSpacesFrameworkBootstrap");

			Object bootstrapInstance = bootstrapClass.newInstance();

			List<String> argList = new ArrayList<String>();
			for (String arg : args) {
				argList.add(arg);
			}

			Method boostrapMethod = bootstrapClass
					.getMethod("boot", List.class);
			boostrapMethod.invoke(bootstrapInstance, argList);
		} catch (Exception e) {
			System.err.println("Could not create bootstrapper");
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Try and write the pid file.
	 * 
	 * @return {@code true} if a pid file didn't previously exist and one
	 *         couldn't be written.
	 */
	private boolean writePid() {
		File runDirectory = new File("run");
		if (!runDirectory.exists()) {
			if (!runDirectory.mkdir()) {
				System.err.format("Could not create run directory %s\n",
						runDirectory);
				return false;
			}
		}

		pidFile = new File(runDirectory, "interactivespaces.pid");
		if (!pidFile.exists()) {
			pidFile.deleteOnExit();
			BufferedWriter out = null;
			try {
				out = new BufferedWriter(new FileWriter(pidFile));
				out.append(Integer.toString(getPid()));
			} catch (Exception e) {
				System.err.format("Error while writing pid file %s\n", pidFile);
				e.printStackTrace();

				return false;
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// Don't care
					}
				}
			}

			return true;
		} else {
			System.err
					.format("InteractiveSpaces component already running. If it isn't running, delete %s\n",
							pidFile.getAbsolutePath());
			return false;
		}
	}

	/**
	 * @return PID of node process if available, throws
	 *         {@link UnsupportedOperationException} otherwise.
	 */
	private int getPid() {
		// Java has no standard way of getting PID. MF.getName()
		// returns '1234@localhost'.
		try {
			String mxName = ManagementFactory.getRuntimeMXBean().getName();
			int idx = mxName.indexOf('@');
			if (idx > 0) {
				try {
					return Integer.parseInt(mxName.substring(0, idx));
				} catch (NumberFormatException e) {
					return 0;
				}
			}
		} catch (NoClassDefFoundError unused) {
			// Android does not support ManagementFactory. Try to get the PID on
			// Android.
			try {
				return (Integer) Class.forName("android.os.Process")
						.getMethod("myPid").invoke(null);
			} catch (Exception unused1) {
				// Ignore this exception and fall through to the
				// UnsupportedOperationException.
			}
		}
		throw new UnsupportedOperationException();
	}

}
