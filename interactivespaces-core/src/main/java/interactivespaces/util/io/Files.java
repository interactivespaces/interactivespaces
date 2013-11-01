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

package interactivespaces.util.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Various useful file routines.
 *
 * @author Keith M. Hughes
 * @deprecated Replaced with FileSupport interface.
 */
public final class Files {

  /**
   * Static instance of the object variant of this class. This is an
   * intermediate transition until this entire class can be removed.
   */
  private static final FileSupportImpl FILE_SUPPORT_INSTANCE = new FileSupportImpl();

  /**
   * Private constructor because it's a static utility class.
   */
  private Files() {
  }

  /**
   * Place the contents of a directory into a zip file.
   *
   * @param target
   *          the output zip file
   * @param basePath
   *          the source content directory
   */
  public static void zip(File target, File basePath) {
    FILE_SUPPORT_INSTANCE.zip(target, basePath);
  }

  /**
   * Place the contents of a zip file into a base directory.
   *
   * @param source
   *          the source zip file
   * @param baseLocation
   *          where the contents will be written
   */
  public static void unzip(File source, File baseLocation) {
    FILE_SUPPORT_INSTANCE.unzip(source, baseLocation);
  }

  /**
   * Copy the source directory to the destination directory.
   *
   * <p>
   * The copy includes all subdirectories, their subdirectories, etc.
   *
   * <p>
   * The destination directory is cleaned out before the source directory is
   * copied.
   *
   * @param srcDir
   *          the source directory
   * @param destDir
   *          the destination directory
   */
  public static void cleanDuplicateDirectory(File srcDir, File destDir) {
    FILE_SUPPORT_INSTANCE.cleanDuplicateDirectory(srcDir, destDir);
  }

  /**
   * Copy the contents of the source directory to the destination directory.
   *
   * <p>
   * This includes all subdirectories, their subdirectories, etc.
   *
   * @param sourceDir
   *          the source directory
   * @param destDir
   *          the destination directory (which will be created if necessary)
   * @param overwrite
   *          {@code true} if should overwrite files if already in the
   *          destination folder
   */
  public static void copyDirectory(File sourceDir, File destDir, boolean overwrite) {
    FILE_SUPPORT_INSTANCE.copyDirectory(sourceDir, destDir, overwrite);
  }

  /**
   * Copy an input stream to an output file.
   *
   * <p>
   * Input stream will be closed upon completion.
   *
   * @param source
   *          the source file to copy
   * @param destination
   *          the destination file to copy to
   */
  public static void copyFile(File source, File destination) {
    FILE_SUPPORT_INSTANCE.copyFile(source, destination);
  }

  /**
   * Copy an input stream to an output file.
   *
   * <p>
   * Input stream will be closed upon completion.
   *
   * @param in
   *          the input strem being copied
   * @param file
   *          the file where the input stream's contents will be copied
   *
   * @throws IOException io problem with operation
   */
  public static void copyInputStream(InputStream in, File file) throws IOException {
    FILE_SUPPORT_INSTANCE.copyInputStream(in, file);
  }

  /**
   * Copy an input stream to an output stream.
   *
   * <p>
   * Both streams will be closed upon completion.
   *
   * @param in
   *          the input stream
   * @param out
   *          the output stream
   *
   * @throws IOException io problem with operation
   */
  public static void copyInputStream(InputStream in, OutputStream out) throws IOException {
    FILE_SUPPORT_INSTANCE.copyInputStream(in, out);
  }

  /**
   * Copy an input stream to an output stream.
   *
   * @param in
   *          the input stream
   * @param out
   *          the output stream
   * @param closeOnCompletion
   *          {@code true} if the streams should be closed when the copy finishes.
   *
   * @throws IOException io problem with operation
   */
  public static void copyStream(InputStream in, OutputStream out, boolean closeOnCompletion) throws IOException {
    FILE_SUPPORT_INSTANCE.copyStream(in, out, closeOnCompletion);
  }

  /**
   * Read the contents of an input stream and return a string containing the
   * contents.
   *
   * @param in
   *          the input stream to read
   *
   * @return a string containing the contents
   *
   * @throws IOException io problem with operation
   */
  public static String inputStreamAsString(InputStream in) throws IOException {
    return FILE_SUPPORT_INSTANCE.inputStreamAsString(in);
  }

  /**
   * Delete a file.
   *
   * <p>
   * If a directory, will recursively delete the directory and its contents.
   *
   * <p>
   * If the file doesn't exist, nothing happens.
   *
   * @param file
   *          the file to be deleted
   */
  public static void delete(File file) {
    FILE_SUPPORT_INSTANCE.delete(file);
  }

  /**
   * Delete the contents of a directory.
   *
   * <p>
   * Will recursively delete subdirectories.
   *
   * @param file
   *          the directory to be deleted
   */
  public static void deleteDirectoryContents(File file) {
    FILE_SUPPORT_INSTANCE.deleteDirectoryContents(file);
  }

  /**
   * Get a file as a string.
   *
   * @param file
   *          the file to be read
   *
   * @return the contents of the file
   */
  public static String readFile(File file) {
    return FILE_SUPPORT_INSTANCE.readFile(file);
  }

  /**
   * Write a string into a file.
   *
   * <p>
   * The supplied string will be the only contents of the file.
   *
   * @param file
   *          the file to be written
   * @param contents
   *          the contents to be written into the file
   */
  public static void writeFile(File file, String contents) {
    FILE_SUPPORT_INSTANCE.writeFile(file, contents);
  }

  /**
   * Make sure a directory exists. If not, it will be created.
   *
   * @param dir
   *          the directory that should exist
   */
  public static void directoryExists(File dir) {
    FILE_SUPPORT_INSTANCE.directoryExists(dir);
  }
}
