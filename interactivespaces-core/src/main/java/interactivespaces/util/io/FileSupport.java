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

package interactivespaces.util.io;

import interactivespaces.SimpleInteractiveSpacesException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Various useful file routines.
 *
 * TODO(peringknife): All the explicit IOExceptions in this class/interface
 * should be converted to be unchecked IS exception of some sort.
 *
 * @author Trevor Pering
 */
public interface FileSupport {

  /**
   * Place the contents of a directory into a zip file.
   *
   * @param target
   *          the output zip file
   * @param basePath
   *          the source content directory
   */
  void zip(File target, File basePath);

  /**
   * Place the contents of a zip file into a base directory.
   *
   * @param source
   *          the source zip file
   * @param baseLocation
   *          where the contents will be written
   */
  void unzip(File source, File baseLocation);

  /**
   * Place the contents of a zip file into a base directory and keep a record of unziped files.
   *
   * @param source
   *          the source zip file
   * @param baseLocation
   *          where the contents will be written
   * @param extractMap
   *          if not {@code null}, add extracted files to map, with dest (key) and source (value)
   */
  void unzip(File source, File baseLocation, Map<File, File> extractMap);

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
  void cleanDuplicateDirectory(File srcDir, File destDir);

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
  void copyDirectory(File sourceDir, File destDir, boolean overwrite);

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
   *          {@code true} if should overwrite files if already in the destination folder
   * @param copyMap
   *          if not {@code null}, add copied files to map, with dest (key) and source (value)
   */
  void copyDirectory(File sourceDir, File destDir, boolean overwrite, Map<File, File> copyMap);

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
  void copyFile(File source, File destination);

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
   * @throws IOException
   *           io problem with operation
   */
  void copyInputStream(InputStream in, File file) throws IOException;

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
   * @throws IOException
   *           io problem with operation
   */
  void copyInputStream(InputStream in, OutputStream out) throws IOException;

  /**
   * Copy an input stream to an output stream.
   *
   * @param in
   *          the input stream
   * @param out
   *          the output stream
   * @param closeOnCompletion
   *          {@code true} if the streams should be closed when the copy
   *          finishes.
   *
   * @throws IOException
   *           io problem with operation
   */
  void copyStream(InputStream in, OutputStream out, boolean closeOnCompletion) throws IOException;

  /**
   * Read the contents of an input stream and return a string containing the
   * contents.
   *
   * @param in
   *          the input stream to read
   *
   * @return a string containing the contents
   *
   * @throws IOException
   *           io problem with operation
   */
  String inputStreamAsString(InputStream in) throws IOException;

  /**
   * Read all the availble data from an input stream and return it as a string.
   *
   * @param in
   *          the input stream to read
   *
   * @return a string containing the contents, or {@code null} if input is
   *         {@code null}
   *
   * @throws IOException
   *           io problem with operation
   */
  String readAvailableToString(InputStream in) throws IOException;

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
  void delete(File file);

  /**
   * Delete the contents of a directory.
   *
   * <p>
   * Will recursively delete subdirectories.
   *
   * @param file
   *          the directory to be deleted
   */
  void deleteDirectoryContents(File file);

  /**
   * Get a file as a string.
   *
   * @param file
   *          the file to be read
   *
   * @return the contents of the file
   */
  String readFile(File file);

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
  void writeFile(File file, String contents);

  /**
   * Make sure a directory exists. If not, it will be created.
   *
   * @param dir
   *          the directory that should exist
   *
   * @throws SimpleInteractiveSpacesException
   *           if the result is not the existence of a valid directory
   */
  void directoryExists(File dir) throws SimpleInteractiveSpacesException;

  /**
   * Make sure a directory exists. If not, it will be created.
   *
   * @param dir
   *          the directory that should exist
   * @param message
   *          the message to use if the directory cannot be made
   *
   * @throws SimpleInteractiveSpacesException
   *           if the result is not the existence of a valid directory
   */
  void directoryExists(File dir, String message) throws SimpleInteractiveSpacesException;
}
