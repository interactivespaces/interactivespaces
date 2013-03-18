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

package interactivespaces.activity.repository.internal;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.repository.ActivityRepositoryStorageManager;
import interactivespaces.configuration.Configuration;
import interactivespaces.domain.basic.Activity;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Maps;

/**
 * A {@link ActivityRepositoryStorageManager} which stores activities in the
 * file system.
 * 
 * <p>
 * This storage manager assumes that staged activities are stored as ZIP files.
 * 
 * @author Keith M. Hughes
 */
public class FileSystemActivityRepositoryStorageManager implements
		ActivityRepositoryStorageManager {
	/**
	 * Configuration property for the location of the activities repository.
	 */
	public static final String CONFIGURATION_REPOSITORY_ACTIVITY_LOCATION = "interactivespaces.repository.activities.location";

	/**
	 * Where files will be staged during installation by the manager.
	 */
	public static final String CONFIGURATION_REPOSITORY_STAGING_LOCATION = "interactivespaces.repository.activities.staging.location";

	/**
	 * Default value for the location of the activities repository.
	 */
	public static final String DEFAULT_REPOSITORY_ACTIVITY_LOCATION = "repository/interactivespaces/activities";

	/**
	 * Extension placed on activity archives for transmission.
	 */
	public static final String ACTIVITY_ARCHIVE_EXTENSION = "zip";

	/**
	 * Base location of the repository.
	 */
	private File repositoryBaseLocation;

	/**
	 * Directory where files are staged.
	 */
	private File stagingDirectory;

	/**
	 * Path to the repository in the file system.
	 */
	private String repositoryPath;

	/**
	 * Map of staging handles to staging files.
	 */
	private Map<String, File> stagingFiles = Maps.newConcurrentMap();

	/**
	 * The Interactive Spaces environment.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	@Override
	public void startup() {
		Configuration systemConfiguration = spaceEnvironment
				.getSystemConfiguration();

		File baseInstallDir = spaceEnvironment.getFilesystem()
				.getInstallDirectory();

		stagingDirectory = new File(
				baseInstallDir,
				systemConfiguration
						.getRequiredPropertyString(CONFIGURATION_REPOSITORY_STAGING_LOCATION));
		// TODO(keith): Good utility function?
		if (stagingDirectory.exists()) {
			if (!stagingDirectory.isDirectory()) {
				throw new InteractiveSpacesException(
						String.format(
								"Activity repository staging directory %s is not a directory",
								stagingDirectory.getAbsolutePath()));
			} else if (!stagingDirectory.canWrite()) {
				throw new InteractiveSpacesException(
						String.format(
								"Activity repository staging directory %s is not writable",
								stagingDirectory.getAbsolutePath()));
			}
		} else {
			if (!stagingDirectory.mkdirs()) {
				throw new InteractiveSpacesException(
						String.format(
								"Could not create activity repository staging directory %s",
								stagingDirectory.getAbsolutePath()));
			}
		}

		// TODO(keith): Check repository base same way as above.
		repositoryPath = systemConfiguration.getPropertyString(
				CONFIGURATION_REPOSITORY_ACTIVITY_LOCATION,
				DEFAULT_REPOSITORY_ACTIVITY_LOCATION);
		repositoryBaseLocation = new File(baseInstallDir, repositoryPath);
	}

	@Override
	public void shutdown() {
		// Nothing to do.
	}

	@Override
	public String getRepositoryBaseLocation() {
		return repositoryBaseLocation.getAbsolutePath();
	}

	@Override
	public boolean containsActivity(Activity activity) {
		return getRepositoryFile(activity).exists();
	}

	@Override
	public String getRepositoryActivityName(Activity activity) {
		return activity.getIdentifyingName() + "-" + activity.getVersion()
				+ "." + ACTIVITY_ARCHIVE_EXTENSION;
	}

	@Override
	public String stageActivity(InputStream activityStream) {
		try {
			File stagedFile = File.createTempFile("activity-", ".zip",
					stagingDirectory);
			Files.copyInputStream(activityStream, stagedFile);

			// +2 to get beyond path separator.
			String handle = stagedFile.getAbsolutePath().substring(
					stagingDirectory.getAbsolutePath().length() + 1);
			stagingFiles.put(handle, stagedFile);

			return handle;
		} catch (IOException e) {
			throw new InteractiveSpacesException(
					"Could not stage activity file", e);
		}
	}

	@Override
	public void removeStagedActivity(String stageHandle) {
		File stageFile = stagingFiles.remove(stageHandle);
		if (stageFile != null) {
			stageFile.delete();
		}
	}

	@Override
	public InputStream getStagedActivityDescription(String stageHandle) {
		File stageFile = stagingFiles.get(stageHandle);
		if (stageFile != null) {
			try {
				ZipFile zip = new ZipFile(stageFile);
				ZipEntry entry = zip.getEntry("activity.xml");

				return new MyZipInputStream(zip, zip.getInputStream(entry));
			} catch (Exception e) {
				throw new InteractiveSpacesException(
						String.format(
								"Cannot get activity description from staged activity %s",
								stageHandle), e);
			}
		} else {
			throw new InteractiveSpacesException(String.format(
					"%s is not a valid staging handle", stageHandle));
		}
	}

	@Override
	public void addActivity(Activity activity, String stageHandle) {
		File stagingFile = stagingFiles.get(stageHandle);
		try {
			if (stagingFile != null) {
				Files.copyFile(stagingFile, getRepositoryFile(activity));
			} else {
				throw new InteractiveSpacesException("Unknown staging handle "
						+ stageHandle);
			}
		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Could not copy staging handle %s to %s", stageHandle,
					stagingFile.getAbsolutePath()), e);
		}
	}

	/**
	 * Get the repository filename used for a given activity.
	 * 
	 * @param activity
	 * @return
	 */
	private File getRepositoryFile(Activity activity) {
		return new File(repositoryBaseLocation,
				getRepositoryActivityName(activity));
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

	/**
	 * An {@link InputStream} based on a zip entry's input stream which will
	 * close the underlying zip file when the stream is closed.
	 * 
	 * @author Keith M. Hughes
	 */
	public static class MyZipInputStream extends InputStream {
		/**
		 * The zip file which gave the entry.
		 */
		private ZipFile zip;

		/**
		 * Input stream from the zip entry being read.
		 */
		private InputStream inputStream;

		public MyZipInputStream(ZipFile zip, InputStream inputStream) {
			this.zip = zip;
			this.inputStream = inputStream;
		}

		/**
		 * @return
		 * @throws IOException
		 * @see java.io.InputStream#available()
		 */
		public int available() throws IOException {
			return inputStream.available();
		}

		/**
		 * @throws IOException
		 * @see java.io.InputStream#close()
		 */
		public void close() throws IOException {
			// Closing the zip closes all input streams created.
			zip.close();
		}

		/**
		 * @param obj
		 * @return
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			return inputStream.equals(obj);
		}

		/**
		 * @return
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return inputStream.hashCode();
		}

		/**
		 * @param readlimit
		 * @see java.io.InputStream#mark(int)
		 */
		public void mark(int readlimit) {
			inputStream.mark(readlimit);
		}

		/**
		 * @return
		 * @see java.io.InputStream#markSupported()
		 */
		public boolean markSupported() {
			return inputStream.markSupported();
		}

		/**
		 * @return
		 * @throws IOException
		 * @see java.io.InputStream#read()
		 */
		public int read() throws IOException {
			return inputStream.read();
		}

		/**
		 * @param arg0
		 * @param arg1
		 * @param arg2
		 * @return
		 * @throws IOException
		 * @see java.io.InputStream#read(byte[], int, int)
		 */
		public int read(byte[] arg0, int arg1, int arg2) throws IOException {
			return inputStream.read(arg0, arg1, arg2);
		}

		/**
		 * @param b
		 * @return
		 * @throws IOException
		 * @see java.io.InputStream#read(byte[])
		 */
		public int read(byte[] b) throws IOException {
			return inputStream.read(b);
		}

		/**
		 * @throws IOException
		 * @see java.io.InputStream#reset()
		 */
		public void reset() throws IOException {
			inputStream.reset();
		}

		/**
		 * @param arg0
		 * @return
		 * @throws IOException
		 * @see java.io.InputStream#skip(long)
		 */
		public long skip(long arg0) throws IOException {
			return inputStream.skip(arg0);
		}

		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return inputStream.toString();
		}

	}

}
