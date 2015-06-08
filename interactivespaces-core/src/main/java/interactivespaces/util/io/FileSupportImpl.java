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

import static com.google.common.io.Closeables.closeQuietly;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Various useful file routines.
 *
 * <p>
 * This class maintains no state.
 *
 * @author Keith M. Hughes
 */
public class FileSupportImpl implements FileSupport {

  /**
   * An instance everyone can use if they chose.
   */
  public static final FileSupport INSTANCE = new FileSupportImpl();

  /**
   * File path separator for ZIP files.
   */
  public static final String ZIP_PATH_SEPARATOR = "/";

  /**
   * Default buffer size for copy operations.
   */
  private static final int COPY_DEFAULT_BUFFER_SIZE = 4096;

  /**
   * File prefix to use for creating unique temporary files.
   */
  private static final String TEMP_FILE_PREFIX = "tmp";

  @Override
  public void zip(File target, File sourceDirectory) {
    zip(target, sourceDirectory, false);
  }

  @Override
  public void zip(File target, File sourceDirectory, boolean overwrite) {
    if (exists(target) && !overwrite) {
      throw new SimpleInteractiveSpacesException("Cannot overwrite existing output file " + target);
    }
    ZipOutputStream zipOutputStream = null;
    try {
      zipOutputStream = createZipOutputStream(target);
      File relPath = new File(".");
      addFileToZipStream(zipOutputStream, sourceDirectory, relPath, null);
      zipOutputStream.close();
      zipOutputStream = null;
    } catch (Exception e) {
      throw new InteractiveSpacesException("Error while zipping directory " + sourceDirectory, e);
    } finally {
      close(zipOutputStream, true);
    }
  }

  @Override
  public ZipOutputStream createZipOutputStream(File outputFile) {
    try {
      return new ZipOutputStream(new FileOutputStream(outputFile));
    } catch (IOException e) {
      throw new SimpleInteractiveSpacesException("Error creating zip output file " + getAbsolutePath(outputFile), e);
    }
  }

  @Override
  public void addFileToZipStream(ZipOutputStream zipOutputStream, File baseFile, File relFile, String prefixPath) {
    FileInputStream fileStream = null;
    try {
      File target = new File(baseFile, getPath(relFile));
      String relPath = getPath(relFile);
      String entryPath = prefixPath == null ? relPath : getPath(new File(prefixPath, relPath));
      if (isFile(target)) {
        zipOutputStream.putNextEntry(new ZipEntry(entryPath));
        fileStream = new FileInputStream(target);
        copyStream(fileStream, zipOutputStream, false);
        fileStream.close();
      } else if (isDirectory(target)) {
        // Zip requires trailing / for directory.
        zipOutputStream.putNextEntry(new ZipEntry(entryPath + ZIP_PATH_SEPARATOR));
        File[] dirFiles = listFiles(target);
        if (dirFiles != null) {
          for (File childPath : dirFiles) {
            File childRelPath = new File(relFile, childPath.getName());
            addFileToZipStream(zipOutputStream, baseFile, childRelPath, prefixPath);
          }
        }
      } else {
        throw new SimpleInteractiveSpacesException("File source not found/recognized: " + getAbsolutePath(target));
      }
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("Error adding file to zip stream " + getAbsolutePath(baseFile), e);
    } finally {
      closeQuietly(fileStream);
    }
  }

  @Override
  public void unzip(File source, File baseLocation) {
    unzip(source, baseLocation, null);
  }

  @Override
  public void unzip(File source, File baseLocation, Map<File, File> extractMap) {
    java.util.zip.ZipFile zipFile = null;
    try {
      zipFile = new java.util.zip.ZipFile(source);

      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();

        if (entry.isDirectory()) {
          File newDir = newFile(baseLocation, entry.getName());
          if (!exists(newDir) && !mkdirs(newDir)) {
            throw new SimpleInteractiveSpacesException("Could not create directory: " + newDir);
          }
        } else {
          File file = newFile(baseLocation, entry.getName());
          File parentFile = getParentFile(file);
          if (!exists(parentFile) && !mkdirs(parentFile)) {
            throw new SimpleInteractiveSpacesException("Could not create parent directory: " + parentFile);
          }

          copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(file)));
          if (extractMap != null) {
            extractMap.put(file, source);
          }
        }
      }

      zipFile.close();
    } catch (IOException ioe) {
      throw new SimpleInteractiveSpacesException(String.format("Error while unzipping file %s",
          getAbsolutePath(source)), ioe);
    } finally {
      // ZipFile does not implement Closeable, so can't use utility function.
      if (zipFile != null) {
        try {
          zipFile.close();
        } catch (IOException e) {
          // Don't care.
        }
      }
    }
  }

  @Override
  public final void cleanDuplicateDirectory(File srcDir, File destDir) {
    directoryExists(destDir);
    deleteDirectoryContents(destDir);
    copyDirectory(srcDir, destDir, true);
  }

  @Override
  public void copyDirectory(File sourceDir, File destDir, boolean overwrite) {
    copyDirectory(sourceDir, destDir, overwrite, null);
  }

  @Override
  public void copyDirectory(File sourceDir, File destDir, boolean overwrite, Map<File, File> fileMap) {
    copyDirectory(sourceDir, null, destDir, overwrite, fileMap);
  }

  @Override
  public void
      copyDirectory(File sourceDir, FileFilter filter, File destDir, boolean overwrite, Map<File, File> fileMap) {
    directoryExists(destDir);

    File[] sourceFiles = listFiles(sourceDir);
    if (sourceFiles == null) {
      throw new SimpleInteractiveSpacesException("Missing source directory " + getAbsolutePath(sourceDir));
    }

    for (File src : sourceFiles) {
      if (filter != null && !filter.accept(src)) {
        continue;
      }

      try {
        File dst = new File(destDir, src.getName());
        if (isDirectory(src)) {
          copyDirectory(src, filter, dst, overwrite, fileMap);
        } else {
          if (!exists(dst) || overwrite) {
            if (fileMap != null) {
              fileMap.put(dst, src);
            }
            copyFile(src, dst);
          }
        }
      } catch (Exception e) {
        throw new InteractiveSpacesException("While copying file " + getAbsolutePath(src), e);
      }
    }
  }

  @Override
  public void copyFile(File source, File destination) {
    try {
      createNewFile(destination);
    } catch (IOException e) {
      throw new InteractiveSpacesException(
          String.format("Could not create new file %s", getAbsolutePath(destination)), e);
    }

    FileChannel in = null;
    FileChannel out = null;

    try {
      in = new FileInputStream(source).getChannel();
      out = new FileOutputStream(destination).getChannel();
      out.transferFrom(in, 0, in.size());
    } catch (IOException e) {
      throw new InteractiveSpacesException(String.format("Could not copy file %s to %s", getAbsoluteFile(source),
          getAbsolutePath(destination)), e);
    } finally {
      try {
        if (out != null) {
          try {
            out.close();
          } catch (IOException e) {
            throw new InteractiveSpacesException(
                String.format("Could not close file %s", getAbsolutePath(destination)), e);
          }
        }
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            // Don't care.
          }
        }
      }
    }
  }

  @Override
  public void copyInputStream(InputStream in, File file) throws IOException {
    copyInputStream(in, new FileOutputStream(file));
  }

  @Override
  public void copyInputStream(InputStream in, OutputStream out) throws IOException {
    copyStream(in, out, true);
  }

  @Override
  public void copyStream(InputStream in, OutputStream out, boolean closeOnCompletion) throws IOException {
    try {
      byte[] buffer = new byte[COPY_DEFAULT_BUFFER_SIZE];
      int len;

      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }

      out.flush();
    } finally {
      if (closeOnCompletion) {
        try {
          in.close();
        } catch (IOException e) {
          // Don't care
        }
        try {
          out.close();
        } catch (IOException e) {
          // Don't care
        }
      }
    }
  }

  @Override
  public void copyFileToStream(File in, OutputStream out, boolean closeOnCompletion) throws IOException {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(in);

      copyStream(inputStream, out, closeOnCompletion);
    } finally {
      Closeables.closeQuietly(inputStream);
    }
  }

  @Override
  public String inputStreamAsString(InputStream in) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    copyInputStream(in, bos);

    return bos.toString();
  }

  @Override
  public String readAvailableToString(InputStream in) throws IOException {
    if (in == null) {
      return null;
    }
    byte[] buffer = new byte[COPY_DEFAULT_BUFFER_SIZE];
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    while (in.available() > 0) {
      int length = in.read(buffer);
      bos.write(buffer, 0, length);
    }
    return bos.toString();
  }

  @Override
  public final void delete(File file) {
    if (exists(file)) {
      if (isDirectory(file)) {
        deleteDirectoryContents(file);
      }

      file.delete();
    }
  }

  @Override
  public void deleteDirectoryContents(File file) {
    File[] files = listFiles(file);
    if (files != null) {
      for (File contained : files) {
        delete(contained);
      }
    }
  }

  @Override
  public String readFile(File file) {
    final StringBuilder builder = new StringBuilder();

    FileLineReader reader = new FileLineReader();
    reader.process(file, new LineReaderHandler() {
      @Override
      public void processLine(String line) {
        builder.append(line).append('\n');
      }
    });

    return builder.toString();
  }

  @Override
  public void writeFile(File file, String contents) {
    FileWriter writer = null;

    try {
      writer = new FileWriter(file);
      writer.append(contents);
      writer.flush();
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Unable to write contents out to file %s", file), e);
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          // Don't care
        }
      }
    }
  }

  @Override
  public void directoryExists(File dir, String message) {
    if (exists(dir)) {
      if (!isDirectory(dir)) {
        String emessage =
            message != null ? String.format("%s: %s is not a directory", message, getAbsolutePath(dir)) : String
                .format("%s is not a directory", getAbsolutePath(dir));

        throw new SimpleInteractiveSpacesException(emessage);
      }
    } else {
      if (!mkdirs(dir)) {
        String emessage =
            message != null ? String.format("%s: Could not create directory %s", message, getAbsolutePath(dir))
                : String.format("Could not create directory %s", getAbsolutePath(dir));

        throw new SimpleInteractiveSpacesException(emessage);
      }
    }
  }

  @Override
  public boolean createNewFile(File file) throws IOException {
    return file.createNewFile();
  }

  @Override
  public void directoryExists(File dir) {
    directoryExists(dir, null);
  }

  @Override
  public boolean exists(File file) {
    return file.exists();
  }

  @Override
  public File newFile(String path) {
    // TODO(keith): Handle Windows
    return new File(path);
  }

  @Override
  public File newFile(URI uri) {
    return new File(uri);
  }

  @Override
  public File newFile(File parent, String subpath) {
    // TODO(keith): Handle Windows
    return new File(parent, subpath);
  }

  @Override
  public File resolveFile(File parent, String subpath) {
    File subFile = newFile(subpath);
    if (subFile.isAbsolute()) {
      return subFile;
    } else {
      return newFile(parent, subpath);
    }
  }

  @Override
  public File getAbsoluteFile(File file) {
    return file.getAbsoluteFile();
  }

  @Override
  public String getAbsolutePath(File file) {
    return file.getAbsolutePath();
  }

  @Override
  public String getName(File file) {
    return file.getName();
  }

  @Override
  public String getParent(File file) {
    return file.getParent();
  }

  @Override
  public File getParentFile(File file) {
    return file.getParentFile();
  }

  @Override
  public String getPath(File file) {
    return file.getPath();
  }

  @Override
  public boolean isDirectory(File dir) {
    return dir.isDirectory();
  }

  @Override
  public boolean isFile(File file) {
    return file.isFile();
  }

  @Override
  public File[] listFiles(File dir) {
    return dir.listFiles();
  }

  @Override
  public File[] listFiles(File dir, FileFilter fileFilter) {
    return dir.listFiles(fileFilter);
  }

  @Override
  public File[] listFiles(File dir, FilenameFilter fileFilter) {
    return dir.listFiles(fileFilter);
  }

  @Override
  public boolean mkdir(File dir) {
    return dir.mkdir();
  }

  @Override
  public boolean mkdirs(File dir) {
    return dir.mkdirs();
  }

  @Override
  public boolean rename(File from, File to) {
    return from.renameTo(to);
  }

  @Override
  public File createTempFile(File baseDir) {
    return createTempFile(baseDir, TEMP_FILE_PREFIX, "");
  }

  @Override
  public File createTempFile(File baseDir, String prefix, String suffix) {
    try {
      return File.createTempFile(prefix, suffix, baseDir);
    } catch (IOException e) {
      throw new SimpleInteractiveSpacesException("Error creating temp file in " + baseDir.getAbsolutePath(), e);
    }
  }

  @Override
  public File createTempFile(String prefix, String suffix) {
    try {
      return File.createTempFile(prefix, suffix);
    } catch (IOException e) {
      throw new SimpleInteractiveSpacesException("Error creating temp file", e);
    }
  }

  @Override
  public File createTempDirectory(File baseDir) {
    return createTempDirectory(baseDir, TEMP_FILE_PREFIX);
  }

  @Override
  public File createTempDirectory(File baseDir, String prefix) {
    File dir = null;
    try {
      dir = Files.createTempDirectory(baseDir.toPath(), prefix).toFile();
    } catch (IOException e) {
      throw InteractiveSpacesException.newFormattedException(e, "Error creating temp directory in %s",
          baseDir.getAbsolutePath(), e);
    }
    return dir;
  }

  @Override
  public void close(Closeable closeable, boolean throwException) throws InteractiveSpacesException {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        if (throwException) {
          throw new InteractiveSpacesException("Exception while closing closeable", e);
        }
      }
    }
  }

  @Override
  public boolean isParent(File candidateParent, File file) {
    try {
      // We need to make sure that /foo/bar is not considered a parent for /foo/bart/spam, hence taking on
      // the separator
      return file.getCanonicalPath().startsWith(candidateParent.getCanonicalPath() + File.separator);
    } catch (IOException e) {
      throw InteractiveSpacesException.newFormattedException(e, "Error checking that %s is a parent of %s",
          candidateParent.getPath(), file.getPath());
    }
  }

  @Override
  public boolean setExecutable(File file, boolean executable) {
    return file.setExecutable(executable);
  }

  @Override
  public List<File> collectFiles(File baseDir, FileFilter filter, boolean recurse) {
    List<File> files = Lists.newArrayList();

    collectFiles(baseDir, filter, recurse, files);

    return files;
  }

  /**
   * Collect files from the current directory.
   *
   * @param currentDir
   *          the current directory
   * @param filter
   *          the file filter
   * @param recurse
   *          {@code true} if should recurse
   * @param files
   *          the collection of files being added to
   */
  private void collectFiles(File currentDir, FileFilter filter, boolean recurse, List<File> files) {
    File[] contents = listFiles(currentDir);
    if (contents != null) {
      for (File file : contents) {
        if (filter.accept(file)) {
          files.add(file);
        }

        if (recurse && file.isDirectory()) {
          collectFiles(file, filter, true, files);
        }
      }
    }
  }

  @Override
  public FileInputStream newFileInputStream(File file) {
    try {
      return new FileInputStream(file);
    } catch (FileNotFoundException e) {
      throw SimpleInteractiveSpacesException.newFormattedException(e,
          "Could not create a new file input stream for file %s", file.getAbsolutePath());
    }
  }

  @Override
  public FileOutputStream newFileOutputStream(File file) {
    try {
      return new FileOutputStream(file);
    } catch (FileNotFoundException e) {
      throw SimpleInteractiveSpacesException.newFormattedException(e,
          "Could not create a new file output stream for file %s", file.getAbsolutePath());
    }
  }
}
