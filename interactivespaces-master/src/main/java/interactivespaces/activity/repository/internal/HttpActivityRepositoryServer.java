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

import interactivespaces.activity.repository.ActivityRepositoryServer;
import interactivespaces.activity.repository.ActivityRepositoryStorageManager;
import interactivespaces.domain.basic.Activity;
import interactivespaces.service.web.server.WebServer;
import interactivespaces.service.web.server.internal.netty.NettyWebServer;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.io.File;

import org.apache.commons.logging.Log;
import org.ros.osgi.common.RosEnvironment;

/**
 * An Interactive Spaces activity repository server using HTTP.
 * 
 * @author Keith M. Hughes
 */
public class HttpActivityRepositoryServer implements
		ActivityRepositoryServer {

	/**
	 * The internal name given to the web server being used for the activity
	 * repository.
	 */
	private static final String ACTIVITY_REPOSITORY_SERVER_NAME = "interactivespaces_activity_repository";

	/**
	 * Webserver for the activity repository.
	 */
	private WebServer repositoryServer;

	/**
	 * Port the repository server listens on.
	 */
	private int repositoryPort;

	/**
	 * Base URL of the repository.
	 */
	private String repositoryBaseUrl;

	/**
	 * Path prefix for the repository URL.
	 */
	private String repositoryUrlPathPrefix = "interactivespaces/repository/activity";

	/**
	 * Environment the repository is running in.
	 */
	private RosEnvironment rosEnvironment;

	/**
	 * The Interactive Spaces environment.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * Storage manager for the activity repository.
	 */
	private ActivityRepositoryStorageManager repositoryStorageManager;

	/**
	 * Logger for this server.
	 */
	private Log log;

	@Override
	public void startup() {
		repositoryPort = 10000;
		repositoryServer = new NettyWebServer(
				ACTIVITY_REPOSITORY_SERVER_NAME, repositoryPort,
				spaceEnvironment.getExecutorService(),
				spaceEnvironment.getExecutorService(), log);

		repositoryServer.start();

		// TODO(keith): Web server should bind to a host or localhost. Add
		// getHost()
		// method.
		String webappPath = "/" + repositoryUrlPathPrefix;
		repositoryBaseUrl = "http://" + rosEnvironment.getHost() + ":"
				+ repositoryServer.getPort() + webappPath;

		repositoryServer.addStaticContentHandler(webappPath, new File(
				repositoryStorageManager.getRepositoryBaseLocation()));
	}

	@Override
	public void shutdown() {
		repositoryServer.shutdown();
	}

	@Override
	public String getActivityUri(Activity activity) {
		// TODO(keith): Get this from something fancier which we can store apps
		// in,
		// get their meta-data, etc.
		return repositoryBaseUrl
				+ "/"
				+ repositoryStorageManager
						.getRepositoryActivityName(activity);
	}

	/**
	 * @param rosEnvironment
	 *            the rosEnvironment to set. Can be null.
	 */
	public void setRosEnvironment(RosEnvironment rosEnvironment) {
		this.rosEnvironment = rosEnvironment;
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

	/**
	 * @param repositoryStorageManager
	 *            the repositoryStorageManager to set
	 */
	public void setRepositoryStorageManager(
			ActivityRepositoryStorageManager repositoryStorageManager) {
		this.repositoryStorageManager = repositoryStorageManager;
	}

	/**
	 * @param log
	 *            the log to set
	 */
	public void setLog(Log log) {
		this.log = log;
	}

}
