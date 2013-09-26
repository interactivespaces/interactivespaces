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

package interactivespaces.workbench.project.activity.builder.java;

import interactivespaces.InteractiveSpacesException;

import aQute.lib.osgi.Constants;

import aQute.lib.osgi.Analyzer;
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
public class BndBundleAnalyzer {

  /**
   * Analyze a bundle for its exports.
   *
   * @param bundle
   *          the bundle to analyze
   *
   * @return the set of all exported packages
   */
  public Set<String> analyze(File bundle) {

    Analyzer analyzer = new Analyzer();
    Jar jar = null;
    try {
      jar = new Jar(bundle);
      Manifest manifest = jar.getManifest();
      String exportHeader = manifest.getMainAttributes().getValue(Constants.EXPORT_PACKAGE);
      if (exportHeader != null) {
        Map<String, Map<String, String>> exported = analyzer.parseHeader(exportHeader);

        return exported.keySet();
      } else {
        return null;
      }
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not analyze bundle %s",
          bundle.getAbsolutePath()), e);
    } finally {
      analyzer.close();

      if (jar != null) {
        jar.close();
      }
    }
  }

}
