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

package interactivespaces.workbench.project.java;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import com.google.common.collect.Sets;

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Constants;
import aQute.lib.osgi.Jar;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

/**
 * Analyze the contents of an existing bundle using BND.
 *
 * @author Keith M. Hughes
 */
public class BndOsgiContainerBundleAnalyzer implements ContainerBundleAnalyzer {

  /**
   * The package exports from the analyzed bundle.
   */
  private Set<String> packageExports;

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public ContainerBundleAnalyzer analyze(File bundle) {
    packageExports = null;
    Analyzer analyzer = new Analyzer();
    Jar jar = null;
    try {
      jar = new Jar(bundle);
      Manifest manifest = jar.getManifest();
      String exportHeader = manifest.getMainAttributes().getValue(Constants.EXPORT_PACKAGE);
      if (exportHeader != null) {
        Map<String, Map<String, String>> exported = analyzer.parseHeader(exportHeader);

        packageExports = exported.keySet();
      } else {
        packageExports = Sets.newHashSet();
      }
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not analyze bundle %s", bundle.getAbsolutePath()), e);
    } finally {
      fileSupport.close(analyzer, false);
      fileSupport.close(jar, false);
    }

    return this;
  }

  @Override
  public Set<String> getPackageExports() {
    return packageExports;
  }
}
