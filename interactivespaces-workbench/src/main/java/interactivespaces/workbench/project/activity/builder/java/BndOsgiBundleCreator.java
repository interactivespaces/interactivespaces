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

import aQute.lib.osgi.Constants;

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Builder;
import aQute.lib.osgi.Jar;
import aQute.lib.osgi.Verifier;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create an OSGi bundle from a source file using BND
 *
 * @author Keith M. Hughes
 */
public class BndOsgiBundleCreator implements OsgiBundleCreator {

  @Override
  public void createBundle(File source, File output, List<File> classpath) throws Exception {
    Analyzer analyzer = new Analyzer();
    try {
      analyzer.setJar(source);
      analyzer.setPedantic(true);

      if (classpath != null) {
        analyzer.setClasspath((File[]) classpath.toArray());
      }

      Jar sourceJar = analyzer.getJar();

      analyzer.setProperty(Constants.IMPORT_PACKAGE, "*;resolution:=optional");

      if (analyzer.getProperty(Constants.BUNDLE_SYMBOLICNAME) == null) {
        Pattern p = Pattern.compile("(" + Verifier.SYMBOLICNAME.pattern() + ")(-[0-9])?.*\\.jar");
        String base = source.getName();
        Matcher m = p.matcher(base);
        base = "Untitled";
        if (m.matches()) {
          base = m.group(1);
        } else {
          System.err
              .println("Can not calculate name of output bundle, rename jar or use -properties");
        }
        analyzer.setProperty(Constants.BUNDLE_SYMBOLICNAME, base);
      }

      String export = analyzer.calculateExportsFromContents(sourceJar);
      analyzer.setProperty(Constants.EXPORT_PACKAGE, export);

      analyzer.mergeManifest(sourceJar.getManifest());

      //
      // Cleanup the version ..
      //
      String version = analyzer.getProperty(Constants.BUNDLE_VERSION);
      if (version != null) {
        version = Analyzer.cleanupVersion(version);
        analyzer.setProperty(Constants.BUNDLE_VERSION, version);
      }

      if (output == null)
        output = source.getAbsoluteFile().getParentFile();

      String path = "interactivespaces." + source.getName();

      if (output.isDirectory())
        output = new File(output, path);

      analyzer.calcManifest();
      Jar finalJar = analyzer.getJar();

      List<String> errors = analyzer.getErrors();
      outputIssues("Errors", errors);
      outputIssues("Warnings", analyzer.getWarnings());

      if (errors.isEmpty()) {
        finalJar.write(output);
      }
    } finally {
      analyzer.close();
    }
  }

  /**
   * Output issues for the user.
   *
   * @param type
   *          the type of issue
   * @param issues
   *          the issues
   */
  private void outputIssues(String type, List<String> issues) {
    if (!issues.isEmpty()) {
      System.out.format("OSGi Bundle Creator %s:\n", type);
      for (String issue : issues) {
        System.out.println(issue);
      }
    }
  }

}
