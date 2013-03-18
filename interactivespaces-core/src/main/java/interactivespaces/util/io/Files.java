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
import java.util.zip.ZipFile;

/**
 * Various useful file routines.
 * 
 * @author Keith M. Hughes
 */
public class Files {

	/**
	 * Place the contents of a zip file into a base directory.
	 * 
	 * @param source
	 *            The source zip file.
	 * @param baseLocation
	 *            Where the contents will be written.
	 */
	static public void unzip(File source, File baseLocation) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(source);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();

				if (entry.isDirectory()) {
					File newDir = new File(baseLocation, entry.getName());
					if (!newDir.exists() && !newDir.mkdirs()) {
						throw new InteractiveSpacesException(
								"Could not create directory: " + newDir);
					}
				} else {
					File file = new File(baseLocation, entry.getName());
					File parentFile = file.getParentFile();
					if (!parentFile.exists() && !parentFile.mkdirs()) {
						throw new InteractiveSpacesException(
								"Could not create directory: " + parentFile);
					}

					copyInputStream(
							zipFile.getInputStream(entry),
							new BufferedOutputStream(new FileOutputStream(file)));
				}
			}

			zipFile.close();
		} catch (IOException ioe) {
			throw new InteractiveSpacesException("Error while unzipping file",
					ioe);
		} finally {
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					// Don't care.
				}
			}
		}
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
	 *            the source directory
	 * @param destDir
	 *            the destination directory
	 */
	public static final void cleanDuplicateDirectory(File srcDir, File destDir) {
		Files.directoryExists(destDir);
		Files.deleteDirectoryContents(destDir);
		Files.copyDirectory(srcDir, destDir, true);
	}

	/**
	 * Copy the contents of the source directory to the destination directory.
	 * 
	 * <p>
	 * This includes all subdirectories, their subdirectories, etc.
	 * 
	 * @param sourceDir
	 *            the source directory
	 * @param destDir
	 *            the destination directory (which will be created if necessary)
	 * @param overwrite
	 *            {@code true} if should overwrite files if already in the
	 *            destination folder
	 */
	public static void copyDirectory(File sourceDir, File destDir,
			boolean overwrite) {
		directoryExists(destDir);

		for (File src : sourceDir.listFiles()) {
			File dst = new File(destDir, src.getName());
			if (src.isDirectory()) {
				copyDirectory(src, dst, overwrite);
			} else {
				if (!dst.exists() || overwrite) {
					copyFile(src, dst);
				}
			}
		}
	}

	/**
	 * Copy an input stream to an output file.
	 * 
	 * <p>
	 * Input stream will be closed upon completion.
	 * 
	 * @param source
	 *            the source file to copy
	 * @param destination
	 *            the destination file to copy to
	 */
	static public void copyFile(File source, File destination) {
		try {
			destination.createNewFile();
		} catch (IOException e) {
			throw new InteractiveSpacesException(String.format(
					"Could not create new file %s",
					destination.getAbsolutePath()), e);
		}

		FileChannel in = null;
		FileChannel out = null;

		try {
			in = new FileInputStream(source).getChannel();
			out = new FileOutputStream(destination).getChannel();
			out.transferFrom(in, 0, in.size());
		} catch (IOException e) {
			throw new InteractiveSpacesException(String.format(
					"Could not copy file %s to %s", source.getAbsoluteFile(),
					destination.getAbsolutePath()), e);
		} finally {
			try {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						throw new InteractiveSpacesException(String.format(
								"Could not close file %s",
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

	/**
	 * Copy an input stream to an output file.
	 * 
	 * <p>
	 * Input stream will be closed upon completion.
	 * 
	 * @param in
	 *            the input strem being copied
	 * @param file
	 *            the file where the input stream's contents will be copied
	 * 
	 * @throws IOException
	 */
	static public void copyInputStream(InputStream in, File file)
			throws IOException {
		copyInputStream(in, new FileOutputStream(file));
	}

	/**
	 * Copy an input stream to an output stream.
	 * 
	 * <p>
	 * Both streams will be closed upon completion.
	 * 
	 * @param in
	 *            the input stream
	 * @param out
	 *            the output stream
	 * 
	 * @throws IOException
	 */
	static public void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		try {
			byte[] buffer = new byte[4096];
			int len;

			while ((len = in.read(buffer)) > 0)
				out.write(buffer, 0, len);

			out.flush();
		} finally {
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

	/**
	 * Read the contents of an input stream and return a string containing the
	 * contents.
	 * 
	 * @param in
	 *            the input stream to read
	 * 
	 * @return a string containing the contents
	 * 
	 * @throws IOException
	 */
	static public String inputStreamAsString(InputStream in) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		copyInputStream(in, bos);

		return bos.toString();
	}

	/**
	 * Delete a file.
	 * 
	 * <p>
	 * If a directory, will recursively delete the directory and its contents.
	 * 
	 * @param file
	 *            The file to be deleted.
	 */
	static public final void delete(File file) {
		if (file.isDirectory()) {
			deleteDirectoryContents(file);
		}

		file.delete();
	}

	/**
	 * Delete the contents of a directory.
	 * 
	 * <p>
	 * Will recursively delete subdirectories.
	 * 
	 * @param file
	 *            the directory to be deleted
	 */
	static public void deleteDirectoryContents(File file) {
		File[] files = file.listFiles();
		if (files != null) {
			for (File contained : files)
				delete(contained);
		}
	}

	/**
	 * Get a file as a string.
	 * 
	 * @param file
	 *            the file to be read
	 * 
	 * @return the contents of the file
	 */
	static public String readFile(File file) {
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

	/**
	 * Write a string into a file.
	 * 
	 * <p>
	 * The supplied string will be the only contents of the file.
	 * 
	 * @param file
	 *            the file to be written
	 * @param contents
	 *            the contents to be written into the file
	 */
	static public void writeFile(File file, String contents) {
		FileWriter writer = null;

		try {
			writer = new FileWriter(file);
			writer.append(contents);
			writer.flush();
		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Unable to write contents out to file %s", file), e);
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

	/**
	 * Make sure a directory exists. If not, it will be created.
	 * 
	 * @param dir
	 *            the directory that should exist
	 * 
	 * @throws InteractiveSpacesException
	 *             if there is a file at the location and it is not a directory
	 */
	public static void directoryExists(File dir) {
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				throw new InteractiveSpacesException(String.format(
						"%s is not a directory", dir.getAbsolutePath()));
			}
		} else {
			if (!dir.mkdirs()) {
				throw new InteractiveSpacesException(String.format(
						"Could not create directory %s", dir.getAbsolutePath()));
			}
		}
	}
}
