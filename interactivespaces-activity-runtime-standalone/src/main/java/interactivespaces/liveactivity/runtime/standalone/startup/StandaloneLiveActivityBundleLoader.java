/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime.standalone.startup;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.liveactivity.runtime.activity.wrapper.internal.interactivespaces.LiveActivityBundleLoader;
import interactivespaces.resource.Version;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * A very simple implementation of a bundle loader for live activities.
 *
 * @author Trevor Pering
 */
public class StandaloneLiveActivityBundleLoader implements LiveActivityBundleLoader {

  /**
   * Construct a bundle loader.
   *
   */
  public StandaloneLiveActivityBundleLoader() {
  }

  @Override
  public synchronized Class<?> getBundleClass(File bundleFile, String bundleName, Version bundleVersion,
      String className) {
    try {
      if (!bundleFile.exists()) {
        throw new FileNotFoundException(bundleFile.getAbsolutePath());
      }
      String bundleUri = bundleFile.toURI().toString();
      return loadClass(bundleUri, className);
    } catch (Throwable e) {
      throw new InteractiveSpacesException(String.format(
          "Could not load class %s from bundle %s", className, bundleFile.getAbsolutePath()), e);
    }
  }

  /**
   * Load a class from a bundle URI.
   *
   * @param bundleUri
   *          the uri that contains the class
   * @param loadClass
   *          class to load
   *
   * @return the loaded class
   *
   * @throws MalformedURLException
   *           if the provided uri is bad
   * @throws ClassNotFoundException
   *           if the intended class could not be found
   */
  private Class loadClass(String bundleUri, String loadClass) throws MalformedURLException, ClassNotFoundException {
    URL url = new URL("jar", "", bundleUri + "!/");
    URL[] urls = new URL[] {url};
    ClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());
    return classLoader.loadClass(loadClass);
  }

}
