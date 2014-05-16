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

package interactivespaces.resource.analysis;

import interactivespaces.resource.NamedVersionedResourceCollection;
import interactivespaces.resource.NamedVersionedResourceWithData;
import interactivespaces.resource.Version;

import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * A resource analyzer for OSGi resources.
 *
 * @author Keith M. Hughes
 */
public class OsgiResourceAnalyzer implements ResourceAnalyzer {

  /**
   * OSGi manifest header for getting the OSGi bundle symbolic name.
   */
  private static final String OSGI_HEADER_SYMBOLIC_NAME = "Bundle-SymbolicName";

  /**
   * OSGi manifest header for getting the OSGi bundle version.
   */
  private static final String OSGI_HEADER_VERSION = "Bundle-Version";

  /**
   * Logger for the analyzer.
   */
  private final Log log;

  /**
   * Construct an analyzer.
   *
   * @param log
   *          the log to use
   */
  public OsgiResourceAnalyzer(Log log) {
    this.log = log;
  }

  @Override
  public NamedVersionedResourceCollection<NamedVersionedResourceWithData<String>> getResourceCollection(File baseDir) {
    NamedVersionedResourceCollection<NamedVersionedResourceWithData<String>> resources =
        NamedVersionedResourceCollection.newNamedVersionedResourceCollection();

    File[] files = baseDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".jar");
      }
    });

    if (files != null) {
      for (File file : files) {
        JarFile jarFile = null;
        try {
          jarFile = new JarFile(file);
          Manifest manifest = jarFile.getManifest();
          Attributes attributes = manifest.getMainAttributes();
          String name = attributes.getValue(OSGI_HEADER_SYMBOLIC_NAME);
          String version = attributes.getValue(OSGI_HEADER_VERSION);
          if (name != null && version != null) {
          NamedVersionedResourceWithData<String> resource =
              new NamedVersionedResourceWithData<String>(name, Version.parseVersion(version), file.getAbsolutePath());

          resources.addResource(resource.getName(), resource.getVersion(), resource);
          } else {
            log.warn(String.format("Resource %s is not a proper OSGi bundle (missing symbolic name and/or version) and is being ignored.", file.getAbsolutePath()));
          }
        } catch (IOException e) {
          log.error(String.format("Could not open resource file jar manifest for %s", file.getAbsolutePath()), e);
        } finally {
          // For some reason Closeables does not work with JarFile despite it
          // claiming it is Closeable in the Javadoc.
          if (jarFile != null) {
            try {
              jarFile.close();
            } catch (IOException e) {
              // Don't care.
            }
          }
        }
      }
    }

    return resources;
  }
}
