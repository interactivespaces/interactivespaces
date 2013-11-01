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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * Various useful file routines.
 *
 * @author Keith M. Hughes
 */
public class FileSupportImpl implements FileSupport {

  /**
   * Default buffer size for copy operations.
   */
  private static final int COPY_DEFAULT_BUFFER_SIZE = 4096;

  @Override
  public void zip(File target, File basePath) {
    if (target.exists()) {
      throw new SimpleInteractiveSpacesException(
          "Cowardly refusing to overwrite existing output file " + target);
    }
    ZipOutputStream zipOutputStream = null;
    try {
      zipOutputStream = new ZipOutputStream(new FileOutputStream(target));
      File relPath = new File(".");
      addFileToZipStream(zipOutputStream, basePath, relPath);
      zipOutputStream.close();
    } catch (Exception e) {
      throw new InteractiveSpacesException("Error wile zipping directory " + basePath, e);
    } finally {
      closeQuietly(zipOutputStream);
    }
  }

  /**
   * Internal helper function to recursively copy contents into a zip file.
   * @param zipOutputStream
   *          output stream in which to copy the contents
   * @param basePath
   *          base path for the content copy
   * @param relPath
   *          relative path (to {@code basePath}), that will be included in the zip file
   * @throws java.io.IOException
   */
  private void addFileToZipStream(ZipOutputStream zipOutputStream, File basePath, File relPath) throws IOException {
    FileInputStream fileStream = null;
    try {
      File target = new File(basePath, relPath.getPath());
      if (target.isFile()) {
        zipOutputStream.putNextEntry(new ZipEntry(relPath.getPath()));
        fileStream = new FileInputStream(target);
        copyStream(fileStream, zipOutputStream, false);
        fileStream.close();
      } else if (target.isDirectory()) {
        String dirPath = relPath.getPath() + File.separator; // Zip requires trailing / for directory.
        zipOutputStream.putNextEntry(new ZipEntry(dirPath));
        File[] dirFiles = target.listFiles();
        if (dirFiles != null) {
          for (File childPath : dirFiles) {
            File childRelPath = new File(relPath, childPath.getName());
            addFileToZipStream(zipOutputStream, basePath, childRelPath);
          }
        }
      } else {
        throw new SimpleInteractiveSpacesException("Unsupported file type for " + relPath);
      }
    } finally {
      closeQuietly(fileStream);
    }
  }

  @Override
  public void unzip(File source, File baseLocation) {
    java.util.zip.ZipFile zipFile = null;
    try {
      zipFile = new java.util.zip.ZipFile(source);

      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = (ZipEntry) entries.nextElement();

        if (entry.isDirectory()) {
          File newDir = new File(baseLocation, entry.getName());
          if (!newDir.exists() && !newDir.mkdirs()) {
            throw new SimpleInteractiveSpacesException("Could not create directory: " + newDir);
          }
        } else {
          File file = new File(baseLocation, entry.getName());
          File parentFile = file.getParentFile();
          if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new SimpleInteractiveSpacesException("Could not create directory: " + parentFile);
          }

          copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(
              new FileOutputStream(file)));
        }
      }

      zipFile.close();
    } catch (IOException ioe) {
      throw new SimpleInteractiveSpacesException(String.format("Error while unzipping file %s",
          source.getAbsolutePath()), ioe);
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
    directoryExists(destDir);

    File[] sourceFiles = sourceDir.listFiles();
    if (sourceFiles == null) {
      throw new SimpleInteractiveSpacesException(
          "Missing source directory " + sourceDir.getAbsolutePath());
    }

    for (File src : sourceFiles) {
      try {
        File dst = new File(destDir, src.getName());
        if (src.isDirectory()) {
          copyDirectory(src, dst, overwrite);
        } else {
          if (!dst.exists() || overwrite) {
            copyFile(src, dst);
          }
        }
      } catch (Exception e) {
        throw new InteractiveSpacesException("While copying file " + src.getAbsolutePath(), e);
      }
    }
  }

  @Override
  public void copyFile(File source, File destination) {
    try {
      destination.createNewFile();
    } catch (IOException e) {
      throw new InteractiveSpacesException(String.format("Could not create new file %s",
          destination.getAbsolutePath()), e);
    }

    FileChannel in = null;
    FileChannel out = null;

    try {
      in = new FileInputStream(source).getChannel();
      out = new FileOutputStream(destination).getChannel();
      out.transferFrom(in, 0, in.size());
    } catch (IOException e) {
      throw new InteractiveSpacesException(String.format("Could not copy file %s to %s",
          source.getAbsoluteFile(), destination.getAbsolutePath()), e);
    } finally {
      try {
        if (out != null) {
          try {
            out.close();
          } catch (IOException e) {
            throw new InteractiveSpacesException(String.format("Could not close file %s",
                destination.getAbsolutePath()), e);
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
    if (file.exists()) {
      if (file.isDirectory()) {
        deleteDirectoryContents(file);
      }

      file.delete();
    }
  }

  @Override
  public void deleteDirectoryContents(File file) {
    File[] files = file.listFiles();
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
      throw new InteractiveSpacesException(String.format("Unable to write contents out to file %s",
          file), e);
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
  public void directoryExists(File dir) {
    if (dir.exists()) {
      if (!dir.isDirectory()) {
        throw new SimpleInteractiveSpacesException(String.format("%s is not a directory",
            dir.getAbsolutePath()));
      }
    } else {
      if (!dir.mkdirs()) {
        throw new SimpleInteractiveSpacesException(String.format("Could not create directory %s",
            dir.getAbsolutePath()));
      }
    }
  }
}
