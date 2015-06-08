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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

/**
 * Various useful file routines.
 *
 * TODO(peringknife): All the explicit IOExceptions in this class/interface should be converted to be unchecked IS
 * exception of some sort.
 *
 * @author Trevor Pering
 */
public interface FileSupport {

  /**
   * Internal helper function to recursively copy contents into a zip file.
   *
   * @param zipOutputStream
   *          output stream in which to copy the contents
   * @param basePath
   *          base path for the content copy
   * @param relPath
   *          relative path (to {@code basePath}), that will be included in the zip file
   * @param pathPrefix
   *          path prefix for added sections
   */
  void addFileToZipStream(ZipOutputStream zipOutputStream, File basePath, File relPath, String pathPrefix);

  /**
   * Create a new zip output stream.
   *
   * @param outputFile
   *          the target output file
   *
   * @return the zip output stream
   */
  ZipOutputStream createZipOutputStream(File outputFile);

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
   * Place the contents of a zip file into a base directory and keep a record of unzipped files.
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
   * Place the contents of a directory into a zip file.
   *
   * @param target
   *          the output zip file
   * @param sourceDirectory
   *          the source content directory
   */
  void zip(File target, File sourceDirectory);

  /**
   * Place the contents of a directory into a zip file.
   *
   * @param target
   *          the output zip file
   * @param sourceDirectory
   *          the source content directory
   * @param overwrite
   *          {@code true} if should overwrite an existing target
   */
  void zip(File target, File sourceDirectory, boolean overwrite);

  /**
   * Copy the source directory to the destination directory.
   *
   * <p>
   * The copy includes all subdirectories, their subdirectories, etc.
   *
   * <p>
   * The destination directory is cleaned out before the source directory is copied.
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
   *          {@code true} if should overwrite files if already in the destination folder
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
   * Copy the contents of the source directory to the destination directory.
   *
   * <p>
   * This includes all subdirectories, their subdirectories, etc.
   *
   * <p>
   * Files must pass the file filter to be copied.
   *
   * @param sourceDir
   *          the source directory
   * @param filter
   *          the file filter that determines which files get copied, can be {@code null} which passes everything
   * @param destDir
   *          the destination directory (which will be created if necessary)
   * @param overwrite
   *          {@code true} if should overwrite files if already in the destination folder
   * @param copyMap
   *          if not {@code null}, add copied files to map, with dest (key) and source (value)
   */
  void copyDirectory(File sourceDir, FileFilter filter, File destDir, boolean overwrite, Map<File, File> copyMap);

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
   *          {@code true} if the streams should be closed when the copy finishes.
   *
   * @throws IOException
   *           io problem with operation
   */
  void copyStream(InputStream in, OutputStream out, boolean closeOnCompletion) throws IOException;

  /**
   * Copy the contents of a file to an output stream.
   *
   * <p>
   * The output stream will not be closed.
   *
   * @param in
   *          the input stream
   * @param out
   *          the output stream
   * @param closeOnCompletion
   *          {@code true} if the streams should be closed on completion of the copy
   *
   * @throws IOException
   *           io problem with operation
   */
  void copyFileToStream(File in, OutputStream out, boolean closeOnCompletion) throws IOException;

  /**
   * Read the contents of an input stream and return a string containing the contents.
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
   * Read all the available data from an input stream and return it as a string.
   *
   * @param in
   *          the input stream to read
   *
   * @return a string containing the contents, or {@code null} if input is {@code null}
   *
   * @throws IOException
   *           io problem with operation
   */
  String readAvailableToString(InputStream in) throws IOException;

  /**
   * Atomically creates a new, empty file named by this abstract pathname if and only if a file with this name does not
   * yet exist. The check for the existence of the file and the creation of the file if it does not exist are a single
   * operation that is atomic with respect to all other filesystem activities that might affect the file.
   *
   * @param file
   *          the file to be created
   *
   * @return {@code true} if the named file does not exist and was successfully created; {@code false} if the named file
   *         already exists
   *
   * @throws IOException
   *           If an I/O error occurred
   */
  boolean createNewFile(File file) throws IOException;

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

  /**
   * Returns an array of abstract pathnames denoting the files in the directory denoted by this abstract pathname.
   *
   * @param dir
   *          the directory whose contents are to be listed
   *
   * @return the the set of files found in the directory
   */
  File[] listFiles(File dir);

  /**
   * Get an array of abstract pathnames denoting the files in the directory denoted by this abstract pathname which pass
   * the file filter.
   *
   * @param dir
   *          the directory whose contents are to be listed
   * @param fileFilter
   *          the file filter
   *
   * @return the the set of files found in the directory that pass the filter
   */
  File[] listFiles(File dir, FileFilter fileFilter);

  /**
   * Get an array of abstract pathnames denoting the files in the directory denoted by this abstract pathname which pass
   * the file name filter.
   *
   * @param dir
   *          the directory whose contents are to be listed
   * @param fileFilter
   *          the file name filter
   *
   * @return the the set of files found in the directory that pass the filter
   */
  File[] listFiles(File dir, FilenameFilter fileFilter);

  /**
   * Tests whether the file denoted by this abstract pathname is a directory.
   *
   * @param dir
   *          the directory that is to be tested
   *
   * @return {@code true} if and only if the file denoted by this abstract pathname exists and is a directory;
   *         {@code false} otherwise
   */
  boolean isDirectory(File dir);

  /**
   * Tests whether the file denoted by this abstract pathname is a normal file. A file is normal if it is not a
   * directory and, in addition, satisfies other system-dependent criteria. Any non-directory file created by a Java
   * application is guaranteed to be a normal file.
   *
   * @param file
   *          the file that is to be tested
   *
   * @return {@code true} if and only if the file denoted by this abstract pathname exists and is a normal file;
   *         {@code false} otherwise
   */
  boolean isFile(File file);

  /**
   * Tests whether the file or directory denoted by this abstract pathname exists.
   *
   * @param file
   *          the file/directory that is to be tested
   *
   * @return {@code true} if and only if the file or directory denoted by this abstract pathname exists; {@code false}
   *         otherwise
   */
  boolean exists(File file);

  /**
   * Create a new file for a specified path.
   *
   * <p>
   * This method takes canonical pathnames and corrects them for the underlying operation system.
   *
   * @param path
   *          the path
   *
   * @return a file for the path
   */
  File newFile(String path);

  /**
   * Create a new file for a specified path.
   *
   * @param uri
   *          the {@code file:} based URI for the file
   *
   * @return a file for the uRI
   */
  File newFile(URI uri);

  /**
   * Create a new file for a specified path relative to the parent file.
   *
   * <p>
   * This method takes canonical pathnames and corrects them for the underlying operation system.
   *
   * @param parent
   *          the parent file
   * @param subpath
   *          the path
   *
   * @return a file for the subpath
   */
  File newFile(File parent, String subpath);

  /**
   * Create a new file for a specified path relative to the parent file. if the subpath is absolute, the returned file
   * will be just the subpath.
   *
   * <p>
   * This method takes canonical pathnames and corrects them for the underlying operation system.
   *
   * @param baseDir
   *          the parent file if the subpath is relative
   * @param subpath
   *          the path
   *
   * @return a file for the subpath
   */
  File resolveFile(File baseDir, String subpath);

  /**
   * Returns the absolute form of this abstract pathname.
   *
   * @param file
   *          the file/directory whose path is to be examined
   *
   * @return The absolute abstract pathname denoting the same file or directory as this abstract pathname
   */
  File getAbsoluteFile(File file);

  /**
   * Returns the absolute pathname string of this abstract pathname.
   *
   * @param file
   *          the file/directory whose path is to be examined
   *
   * @return The absolute pathname string denoting the same file or directory as this abstract pathname
   */
  String getAbsolutePath(File file);

  /**
   * Returns the name of the file or directory denoted by this abstract pathname. This is just the last name in the
   * pathname's name sequence. If the pathname's name sequence is empty, then the empty string is returned.
   *
   * @param file
   *          the file/directory whose path is to be determined
   *
   * @return The name of the file or directory denoted by this abstract pathname, or the empty string if this pathname's
   *         name sequence is empty
   */
  String getName(File file);

  /**
   * Converts this abstract pathname into a pathname string. The resulting string uses the default name-separator
   * character to separate the names in the name sequence.
   *
   * @param file
   *          the file/directory whose path is to be determined
   *
   * @return The abstract pathname of the parent directory named by this abstract pathname, or {@code null} if this
   *         pathname does not name a parent
   */
  String getPath(File file);

  /**
   * Returns the pathname string of this abstract pathname's parent, or {@code null} if this pathname does not name a
   * parent directory.
   *
   * @param file
   *          the file/directory whose path is to be examined
   *
   * @return The pathname string of the parent directory named by this abstract pathname, or {@code null} if this
   *         pathname does not name a parent
   */
  String getParent(File file);

  /**
   * Returns the abstract pathname of this abstract pathname's parent, or null if this pathname does not name a parent
   * directory.
   *
   * @param file
   *          the file/directory whose path is to be examined
   *
   * @return The abstract pathname of the parent directory named by this abstract pathname, or {@code null} if this
   *         pathname does not name a parent
   */
  File getParentFile(File file);

  /**
   * Creates the directory named by this abstract pathname.
   *
   * @param dir
   *          the directory that is going to be created
   *
   * @return {@code true} if and only if the directory was created; {@code false} otherwise
   */
  boolean mkdir(File dir);

  /**
   * Creates the directory named by this abstract pathname, including any necessary but nonexistent parent directories.
   *
   * @param dir
   *          the directory that is going to be created
   *
   * @return {@code true} if and only if the directory was created, along with all necessary parent directories;
   *         {@code false} otherwise
   */
  boolean mkdirs(File dir);

  /**
   * Rename a file/directory.
   *
   * @param from
   *          source file to rename
   * @param to
   *          new target name
   *
   * @return {@code true} if rename was successful
   */
  boolean rename(File from, File to);

  /**
   * Create a unique temporary file in the given directory, using default prefix and suffix.
   *
   * @param baseDir
   *          directory in which to create file
   *
   * @return unique file
   */
  File createTempFile(File baseDir);

  /**
   * Create a unique temporary file in the given directory.
   *
   * @param baseDir
   *          directory in which to create file
   * @param prefix
   *          temp file prefix to use
   * @param suffix
   *          temp file suffix to use
   *
   * @return unique file
   */
  File createTempFile(File baseDir, String prefix, String suffix);

  /**
   * Create a unique temporary file somewhere in the filesystem.
   *
   * @param prefix
   *          temp file prefix to use
   * @param suffix
   *          temp file suffix to use
   *
   * @return unique file
   */
  File createTempFile(String prefix, String suffix);

  /**
   * Create a unique temporary directory in the given directory, using default prefix and suffix.
   *
   * @param baseDir
   *          directory in which to create directory
   *
   * @return unique directory
   */
  File createTempDirectory(File baseDir);

  /**
   * Create a unique temporary directory in the given directory.
   *
   * @param baseDir
   *          directory in which to create directory
   * @param prefix
   *          temp file prefix to use
   *
   * @return unique directory
   */
  File createTempDirectory(File baseDir, String prefix);

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
   * Close the closeable. If asked to throw any exceptions from the close, the method will wrap it in an
   * {@link InteractiveSpacesException}.
   *
   * @param closeable
   *          the item to close, can be {@code null}
   * @param throwException
   *          {@code true} if any exceptions thrown during closing should be rethrown
   *
   * @throws InteractiveSpacesException
   *           an exception happened during close
   */
  void close(Closeable closeable, boolean throwException) throws InteractiveSpacesException;

  /**
   * Is the candidate parent the parent of the file?
   *
   * <p>
   * The parent relationship is a strict parent. {@code /foo/bar} is not considered a parent for {@code /foo/bart/spam}
   * and {@code /foo/bar} is not a parent for {@code /foo/bar}.
   *
   * @param candidateParent
   *          the possible parent
   * @param file
   *          the file that may have the candidate parent as a parent
   *
   * @return {@code true} if the candidate is a parent of the file
   */
  boolean isParent(File candidateParent, File file);

  /**
   * Set the file as executable by the owner.
   *
   * @param file
   *          the file
   * @param executable
   *          {@code true} if the file should be executable
   *
   * @return {@code true} if successful
   */
  boolean setExecutable(File file, boolean executable);

  /**
   * Collect all files that pass the file filer.
   *
   * @param baseDir
   *          the base directory to start in
   * @param filter
   *          the filter that decides which files
   * @param recurse
   *          {@code true} if should recurse through directories
   *
   * @return the collection of files that pass the filter
   */
  List<File> collectFiles(File baseDir, FileFilter filter, boolean recurse);

  /**
   * Create a new file input stream for the requested file.
   *
   * @param file
   *          the file
   *
   * @return the new stream
   */
  FileInputStream newFileInputStream(File file);

  /**
   * Create a new file output stream for the requested file.
   *
   * @param file
   *          the file
   *
   * @return the new stream
   */
  FileOutputStream newFileOutputStream(File file);
}
