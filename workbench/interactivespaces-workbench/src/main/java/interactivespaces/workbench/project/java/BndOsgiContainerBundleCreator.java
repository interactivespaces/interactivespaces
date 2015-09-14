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
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Constants;
import aQute.lib.osgi.Jar;
import aQute.lib.osgi.Verifier;
import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

/**
 * Create an OSGi bundle from a source file using BND.
 *
 * @author Keith M. Hughes
 */
public class BndOsgiContainerBundleCreator implements ContainerBundleCreator {

  /**
   * The default fat jar name prefix.
   */
  private static final String DEFAULT_FAT_JAR_FILENAME_PREFIX = "interactivespaces";

  /**
   * The default fat jar name suffix.
   */
  private static final String DEFAULT_FAT_JAR_FILENAME_SUFFIX = "-bundle.jar";

  /**
   * The prefix of the name for the merge jar before handed to BND.
   */
  private static final String MERGE_JAR_TEMP_DIRECTORY_PREFIX = "interactivespaces-bundle";

  /**
   * The prefix for the filename for the ultimate OSGi bundle if no name is specified.
   */
  private static final String GENERIC_BUNDLE_FILENAME_PREFIX = "interactivespaces.";

  /**
   * Regular expression prefix for a guess at a symbolic name for a jar from its filename.
   */
  private static final String PATTERN_PREFIX_SYMBOLIC_NAME_FROM_JAR = "(";

  /**
   * Regular expression suffix for a guess at a symbolic name for a jar from its filename.
   */
  private static final String PATTERN_SUFFIX_SYMBOLIC_NAME_FROM_JAR = ")(-[0-9])?.*\\.jar";

  /**
   * BND declaration for all import packages should be considered optional.
   */
  private static final String ALL_IMPORT_PACKAGES_OPTIONAL = "*;resolution:=optional";

  /**
   * Size of IO buffers in bytes.
   */
  public static final int IO_BUFFER_SIZE = 1024;

  /**
   * A folder for temporary files.
   */
  private File baseTempDir;

  /**
   * The logger to use.
   */
  private Log log;

  /**
   * The file support to use for file operations.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new creator.
   *
   * @param baseTempDir
   *          directory to be used for scratch files
   * @param log
   *          the logger to use
   */
  public BndOsgiContainerBundleCreator(File baseTempDir, Log log) {
    this.baseTempDir = baseTempDir;
    this.log = log;
  }

  @Override
  public void createBundle(List<File> sources, File output, File headers, List<File> classpath) {
    if (sources.size() > 1 && output == null) {
      throw new SimpleInteractiveSpacesException("An output name must be specified if merging multiple jars");
    }

    File sourceJarFile = getSourceJar(sources);

    Analyzer analyzer = new Analyzer();
    try {
      analyzer.setJar(sourceJarFile);
      analyzer.setPedantic(true);

      if (classpath != null) {
        analyzer.setClasspath((File[]) classpath.toArray());
      }

      Jar sourceJar = analyzer.getJar();

      if (headers != null) {
        analyzer.addProperties(headers);
      } else {
        analyzer.setProperty(Constants.IMPORT_PACKAGE, ALL_IMPORT_PACKAGES_OPTIONAL);

        Pattern p =
            Pattern.compile(PATTERN_PREFIX_SYMBOLIC_NAME_FROM_JAR + Verifier.SYMBOLICNAME.pattern()
                + PATTERN_SUFFIX_SYMBOLIC_NAME_FROM_JAR);
        String base = sourceJarFile.getName();
        Matcher m = p.matcher(base);
        base = null;
        if (m.matches()) {
          base = m.group(1);
        } else {
          throw new SimpleInteractiveSpacesException("Can not calculate bundle name from the source jar, "
              + "rename jar to be of form name-version.jar or use a headers file");
        }
        analyzer.setProperty(Constants.BUNDLE_SYMBOLICNAME, base);

        String export = analyzer.calculateExportsFromContents(sourceJar);
        analyzer.setProperty(Constants.EXPORT_PACKAGE, export);
      }

      analyzer.mergeManifest(sourceJar.getManifest());

      // Cleanup the version ..
      String version = analyzer.getProperty(Constants.BUNDLE_VERSION);
      if (version != null) {
        version = Analyzer.cleanupVersion(version);
        analyzer.setProperty(Constants.BUNDLE_VERSION, version);
      }

      if (output == null) {
        output = sourceJarFile.getAbsoluteFile().getParentFile();
      }

      String bundleFileName = GENERIC_BUNDLE_FILENAME_PREFIX + sourceJarFile.getName();

      if (output.isDirectory()) {
        output = fileSupport.newFile(output, bundleFileName);
      }

      analyzer.calcManifest();
      Jar finalJar = analyzer.getJar();

      List<String> errors = analyzer.getErrors();
      outputErrors(errors, log);
      outputWarnings(analyzer.getWarnings(), log);

      if (errors.isEmpty()) {
        finalJar.write(output);

        log.info(String.format("Created OSGi bundle %s", output.getAbsolutePath()));
      }
    } catch (Throwable e) {
      throw new InteractiveSpacesException("Error while creating OSGi bundle", e);
    } finally {
      fileSupport.close(analyzer, false);

      if (sources.size() > 1) {
        fileSupport.delete(sourceJarFile);
      }
    }
  }

  /**
   * Get the source jar for BND analysis.
   *
   * <p>
   * If multiple sources this file should be deleted after the source jar is processed.
   *
   * @param sources
   *          the source jars
   *
   * @return the source jar
   */
  private File getSourceJar(List<File> sources) {
    if (sources.size() > 1) {
      return mergeSources(sources);
    } else {
      return sources.get(0);
    }
  }

  /**
   * Merge the following sources into one large source.
   *
   * @param sources
   *          the source jars
   *
   * @return a source which contains everything from the sources
   */
  private File mergeSources(List<File> sources) {
    File tempExpansionFolder = null;
    File fatJar = null;
    try {
      tempExpansionFolder = fileSupport.createTempDirectory(baseTempDir, MERGE_JAR_TEMP_DIRECTORY_PREFIX);

      for (File source : sources) {
        fileSupport.unzip(source, tempExpansionFolder);
      }

      fatJar = fileSupport.createTempFile(baseTempDir,
          DEFAULT_FAT_JAR_FILENAME_PREFIX, DEFAULT_FAT_JAR_FILENAME_SUFFIX);
      createJarFile(fatJar, tempExpansionFolder);

      return fatJar;
    } catch (Exception e) {
      if (fatJar != null) {
        fileSupport.delete(fatJar);
      }

      throw new InteractiveSpacesException("Exception while merging source jars", e);
    } finally {
      if (tempExpansionFolder != null) {
        fileSupport.delete(tempExpansionFolder);
      }
    }
  }

  /**
   * Output errors for the user.
   *
   * @param issues
   *          the issues
   * @param log
   *          the logger to use
   */
  private void outputErrors(List<String> issues, Log log) {
    for (String issue : issues) {
      log.error("OSGi Bundle Creator: " + issue);
    }
  }

  /**
   * Output warnings for the user.
   *
   * @param issues
   *          the issues
   * @param log
   *          the logger to use
   */
  private void outputWarnings(List<String> issues, Log log) {
    for (String issue : issues) {
      log.warn("OSGi Bundle Creator: " + issue);
    }
  }

  /**
   * Create a Jar file from a source folder.
   *
   * @param jarDestinationFile
   *          the jar file being created
   * @param sourceFolder
   *          the source folder containing the classes
   */
  private void createJarFile(File jarDestinationFile, File sourceFolder) {
    // Create a buffer for reading the files
    byte[] buf = new byte[IO_BUFFER_SIZE];

    JarOutputStream out = null;
    try {
      // Create the jar file
      out = new JarOutputStream(new FileOutputStream(jarDestinationFile));

      writeJarFile(sourceFolder, buf, out, "");

      // Complete the jar file
      out.close();
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Failed creating jar file %s",
          jarDestinationFile.getAbsolutePath()), e);
    } finally {
      fileSupport.close(out, true);
    }
  }

  /**
   * Write out the contents of the folder to the distribution file.
   *
   * @param directory
   *          folder being written to the build
   * @param buf
   *          a buffer for caching info
   * @param jarOutputStream
   *          the stream where the jar is being written
   * @param parentPath
   *          path up to this point
   *
   * @throws IOException
   *           for IO access errors
   */
  private void writeJarFile(File directory, byte[] buf, ZipOutputStream jarOutputStream, String parentPath)
      throws IOException {
    File[] files = directory.listFiles();
    if (files == null || files.length == 0) {
      log.warn("No source files found in " + directory.getAbsolutePath());
      return;
    }
    for (File file : files) {
      if (file.isDirectory()) {
        writeJarFile(file, buf, jarOutputStream, parentPath + file.getName() + "/");
      } else {
        FileInputStream in = null;
        try {
          in = new FileInputStream(file);

          // Add ZIP entry to output stream.
          jarOutputStream.putNextEntry(new JarEntry(parentPath + file.getName()));

          // Transfer bytes from the file to the ZIP file
          int len;
          while ((len = in.read(buf)) > 0) {
            jarOutputStream.write(buf, 0, len);
          }

          // Complete the entry
          jarOutputStream.closeEntry();
        } finally {
          fileSupport.close(in, false);
        }
      }
    }
  }
}
