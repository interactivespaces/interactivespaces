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

package interactivespaces.service.speech.synthesis.internal.festival;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.binary.NativeActivityRunner;
import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.SpaceController;
import interactivespaces.service.speech.synthesis.SpeechSynthesisPlayer;
import interactivespaces.service.speech.synthesis.SpeechSynthesisService;
import interactivespaces.service.speech.synthesis.internal.festival.client.Request;
import interactivespaces.service.speech.synthesis.internal.festival.client.RequestListener;
import interactivespaces.service.speech.synthesis.internal.festival.client.Session;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.InteractiveSpacesUtilities;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * A speech synthesis service based on Festival.
 * 
 * @author Keith M. Hughes
 */
public class FestivalSpeechSynthesisService implements SpeechSynthesisService {

	/**
	 * The name of the configuration property giving the binary.
	 */
	public static final String CONFIGURATION_PROPERTY_NAME_BINARY = "interactivespaces.service.speech.synthesis.external.festival.binary.linux";

	/**
	 * The name of the configuration property giving the flags for the binary.
	 */
	public static final String CONFIGURATION_PROPERTY_NAME_FLAGS = "interactivespaces.service.speech.synthesis.external.festival.flags.linux";

	/**
	 * The name of the configuration property saying when the binary should
	 * start.
	 */
	public static final String CONFIGURATION_PROPERTY_NAME_STARTUP = "interactivespaces.service.speech.synthesis.external.festival.flags.linux";

	/**
	 * The value of the configuration property saying when the binary should
	 * start when the service starts up.
	 */
	public static final String CONFIGURATION_PROPERTY_VALUE_ON_STARTUP = "on_startup";

	/**
	 * The name of the configuration property saying when the binary should
	 * start when it is first used.
	 */
	public static final String CONFIGURATION_PROPERTY_VALUE_ON_FIRST_USE = "on_first_use";

	/**
	 * The name of the configuration property saying when the binary should
	 * start.
	 */
	public static final String CONFIGURATION_PROPERTY_DEFAULT_STARTUP = CONFIGURATION_PROPERTY_VALUE_ON_FIRST_USE;

	/**
	 * The name of the configuration property for the host where Festival is
	 * running.
	 */
	public static final String CONFIGURATION_PROPERTY_NAME_HOST = "interactivespaces.service.speech.synthesis.external.festival.host";

	/**
	 * The default value configuration property for the host where Festival is
	 * running.
	 */
	public static final String CONFIGURATION_PROPERTY_DEFAULT_HOST = "localhost";

	/**
	 * The name of the configuration property for the port where Festival server
	 * is listening.
	 */
	public static final String CONFIGURATION_PROPERTY_NAME_PORT = "interactivespaces.service.speech.synthesis.external.festival.host";

	/**
	 * The default value configuration property for the port where Festival
	 * server is listening.
	 */
	public static final int CONFIGURATION_PROPERTY_DEFAULT_PORT = 1314;

	/**
	 * Runner for the speech server.
	 */
	private NativeActivityRunner speechServer;

	/**
	 * Host the festival server is running on.
	 */
	private String host;

	/**
	 * Port the festival host is listening on.
	 */
	private int port;

	/**
	 * Handler for speech requests
	 */
	private RequestListener requestHandler;

	/**
	 * Session with the Festival server.
	 */
	private Session session;

	/**
	 * The space environment for the service.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	@Override
	public void startup() {

		session = null;

		host = spaceEnvironment.getSystemConfiguration().getPropertyString(
				CONFIGURATION_PROPERTY_NAME_HOST,
				CONFIGURATION_PROPERTY_DEFAULT_HOST);

		port = spaceEnvironment.getSystemConfiguration().getPropertyInteger(
				CONFIGURATION_PROPERTY_NAME_PORT,
				CONFIGURATION_PROPERTY_DEFAULT_PORT);

		String startupType = spaceEnvironment.getSystemConfiguration()
				.getPropertyString(CONFIGURATION_PROPERTY_NAME_STARTUP,
						CONFIGURATION_PROPERTY_DEFAULT_STARTUP);

		if (CONFIGURATION_PROPERTY_VALUE_ON_STARTUP.equals(startupType)) {
			startupServer(false);
		}
	}

	/**
	 * Startup the Festival server.
	 * 
	 * <p>
	 * Does nothing if the server is running already.
	 * 
	 * @param immediate
	 *            {@code true} if the server is being started up for immediate
	 *            use
	 * 
	 * @throws InteractiveSpacesException
	 */
	public void startupServer(boolean immediate)
			throws InteractiveSpacesException {
		if (speechServer == null) {
			NativeActivityRunnerFactory runnerFactory = spaceEnvironment
					.getValue(SpaceController.ENVIRONMENT_CONTROLLER_NATIVE_RUNNER);

			speechServer = runnerFactory
					.newPlatformNativeActivityRunner(spaceEnvironment.getLog());
			Configuration configuration = spaceEnvironment.getSystemConfiguration();
			Map<String, Object> appConfig = Maps.newHashMap();
			appConfig
					.put(NativeActivityRunner.ACTIVITYNAME,
							configuration
									.getRequiredPropertyString(FestivalSpeechSynthesisService.CONFIGURATION_PROPERTY_NAME_BINARY));
			String commandFlags = configuration
					.getRequiredPropertyString(FestivalSpeechSynthesisService.CONFIGURATION_PROPERTY_NAME_FLAGS);

			appConfig.put(NativeActivityRunner.FLAGS, commandFlags);
			speechServer.configure(appConfig);
			speechServer.startup();

			if (immediate) {
				InteractiveSpacesUtilities.delay(3000);
			}
		}

		if (session == null) {
			try {
				session = new Session(host, port);
			} catch (UnknownHostException e) {
				throw new InteractiveSpacesException(
						"Festival client has unknown host", e);
			} catch (IOException e) {
				throw new InteractiveSpacesException(
						"Festival client can't connect", e);
			}

			session.initialise();

			requestHandler = new RequestHandler();
		}
	}

	@Override
	public void shutdown() {
		if (speechServer != null) {
			speechServer.shutdown();
			speechServer = null;
		}
		
		if (session != null) {
			session.terminate(true);
			session = null;
		}
	}

	@Override
	public SpeechSynthesisPlayer newPlayer() {
		return newPlayer(spaceEnvironment.getLog());
	}

	@Override
	public SpeechSynthesisPlayer newPlayer(Log log) {
		return new FestivalSpeechSynthesisPlayer();
	}

	@Override
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

	/**
	 * Festival client request handler.
	 * 
	 * @author Keith M. Hughes
	 */
	private class RequestHandler implements RequestListener {
		public void requestRunning(Request request) {
			spaceEnvironment.getLog().info(
					String.format("Request %s running\n", request.command));
		}

		public void requestResult(Request request, Object result) {
			spaceEnvironment.getLog().info(
					String.format("Request %s, result %s\n", request.command,
							result.toString()));
		}

		public void requestError(Request request, String message) {
			spaceEnvironment.getLog().info(
					String.format("Request %s, Error %s\n", request.command,
							message));
		}

		public void requestFinished(Request request) {
			spaceEnvironment.getLog().info(
					String.format("Request %s finished\n", request.command));
		}
	}

	private class FestivalSpeechSynthesisPlayer implements
			SpeechSynthesisPlayer {

		@Override
		public void startup() {
			// Nothing to do
		}

		@Override
		public void shutdown() {
			// Nothing to do
		}

		@Override
		public void speak(String text, boolean sync) {
			startupServer(true);

			String textCommand = String.format("(SayText \"%s\")", text);

			Request r = session.request(textCommand, requestHandler);
			if (sync)
				while (!r.isFinished())
					r.waitForUpdate();
		}

	}
};
