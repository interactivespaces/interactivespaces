package interactivespaces.util.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Various useful file routines.
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
   * @throws java.io.IOException
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
   * @throws java.io.IOException
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
   * @throws java.io.IOException
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
   * @throws java.io.IOException
   */
  String inputStreamAsString(InputStream in) throws IOException;

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
   * @throws interactivespaces.InteractiveSpacesException
   *           if there is a file at the location and it is not a directory
   */
  void directoryExists(File dir);
}
