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
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.binary.NativeActivityRunner;
import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.SpaceController;
import interactivespaces.service.BaseSupportedService;
import interactivespaces.service.speech.synthesis.SpeechSynthesisPlayer;
import interactivespaces.service.speech.synthesis.SpeechSynthesisService;
import interactivespaces.service.speech.synthesis.internal.festival.client.Request;
import interactivespaces.service.speech.synthesis.internal.festival.client.RequestListener;
import interactivespaces.service.speech.synthesis.internal.festival.client.Session;
import interactivespaces.util.InteractiveSpacesUtilities;

import org.apache.commons.logging.Log;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * A speech synthesis service based on Festival.
 *
 * @author Keith M. Hughes
 */
public class FestivalSpeechSynthesisService extends BaseSupportedService implements SpeechSynthesisService {

  /**
   * The name of the configuration property giving the binary.
   */
  public static final String CONFIGURATION_PROPERTY_NAME_BINARY =
      "interactivespaces.service.speech.synthesis.external.festival.binary.linux";

  /**
   * The name of the configuration property giving the flags for the binary.
   */
  public static final String CONFIGURATION_PROPERTY_NAME_FLAGS =
      "interactivespaces.service.speech.synthesis.external.festival.flags.linux";

  /**
   * The name of the configuration property saying when the binary should start.
   */
  public static final String CONFIGURATION_PROPERTY_NAME_STARTUP =
      "interactivespaces.service.speech.synthesis.external.festival.flags.linux";

  /**
   * The value of the configuration property saying when the binary should start when the service starts up.
   */
  public static final String CONFIGURATION_PROPERTY_VALUE_ON_STARTUP = "on_startup";

  /**
   * The name of the configuration property saying when the binary should start when it is first used.
   */
  public static final String CONFIGURATION_PROPERTY_VALUE_ON_FIRST_USE = "on_first_use";

  /**
   * The name of the configuration property saying when the binary should start.
   */
  public static final String CONFIGURATION_PROPERTY_DEFAULT_STARTUP = CONFIGURATION_PROPERTY_VALUE_ON_FIRST_USE;

  /**
   * The name of the configuration property for the host where Festival is running.
   */
  public static final String CONFIGURATION_PROPERTY_NAME_HOST =
      "interactivespaces.service.speech.synthesis.external.festival.host";

  /**
   * The default value configuration property for the host where Festival is running.
   */
  public static final String CONFIGURATION_PROPERTY_DEFAULT_HOST = "localhost";

  /**
   * The name of the configuration property for the port where Festival server is listening.
   */
  public static final String CONFIGURATION_PROPERTY_NAME_PORT =
      "interactivespaces.service.speech.synthesis.external.festival.host";

  /**
   * The default value configuration property for the port where Festival server is listening.
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
   * Handler for speech requests.
   */
  private RequestListener requestHandler;

  /**
   * Session with the Festival server.
   */
  private Session session;

  @Override
  public String getName() {
    return SpeechSynthesisService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    session = null;

    host =
        getSpaceEnvironment().getSystemConfiguration().getPropertyString(CONFIGURATION_PROPERTY_NAME_HOST,
            CONFIGURATION_PROPERTY_DEFAULT_HOST);

    port =
        getSpaceEnvironment().getSystemConfiguration().getPropertyInteger(CONFIGURATION_PROPERTY_NAME_PORT,
            CONFIGURATION_PROPERTY_DEFAULT_PORT);

    String startupType =
        getSpaceEnvironment().getSystemConfiguration().getPropertyString(CONFIGURATION_PROPERTY_NAME_STARTUP,
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
   *          {@code true} if the server is being started up for immediate use
   *
   * @throws InteractiveSpacesException
   *           unable to start up the server
   */
  private void startupServer(boolean immediate) throws InteractiveSpacesException {
    if (speechServer == null) {
      NativeActivityRunnerFactory runnerFactory =
          getSpaceEnvironment().getValue(SpaceController.ENVIRONMENT_CONTROLLER_NATIVE_RUNNER);

      speechServer = runnerFactory.newPlatformNativeActivityRunner(getSpaceEnvironment().getLog());

      Configuration configuration = getSpaceEnvironment().getSystemConfiguration();
      speechServer.setExecutablePath(configuration
          .getRequiredPropertyString(FestivalSpeechSynthesisService.CONFIGURATION_PROPERTY_NAME_BINARY));
      speechServer.addCommandArguments(configuration
          .getRequiredPropertyString(FestivalSpeechSynthesisService.CONFIGURATION_PROPERTY_NAME_FLAGS));

      speechServer.startup();

      if (immediate) {
        InteractiveSpacesUtilities.delay(3000);
      }
    }

    if (session == null) {
      try {
        session = new Session(host, port);
      } catch (UnknownHostException e) {
        throw new SimpleInteractiveSpacesException("Festival client has unknown host");
      } catch (IOException e) {
        throw new InteractiveSpacesException("Festival client can't connect", e);
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
    return newPlayer(getSpaceEnvironment().getLog());
  }

  @Override
  public SpeechSynthesisPlayer newPlayer(Log log) {
    return new FestivalSpeechSynthesisPlayer();
  }

  /**
   * Festival client request handler.
   *
   * @author Keith M. Hughes
   */
  private class RequestHandler implements RequestListener {
    @Override
    public void requestRunning(Request request) {
      getSpaceEnvironment().getLog().info(String.format("Request %s running\n", request.command));
    }

    @Override
    public void requestResult(Request request, Object result) {
      getSpaceEnvironment().getLog().info(String.format("Request %s, result %s\n", request.command, result.toString()));
    }

    @Override
    public void requestError(Request request, String message) {
      getSpaceEnvironment().getLog().info(String.format("Request %s, Error %s\n", request.command, message));
    }

    @Override
    public void requestFinished(Request request) {
      getSpaceEnvironment().getLog().info(String.format("Request %s finished\n", request.command));
    }
  }

  /**
   * A speech player for Festival.
   *
   * @author Keith M. Hughes
   */
  private class FestivalSpeechSynthesisPlayer implements SpeechSynthesisPlayer {

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
      if (sync) {
        while (!r.isFinished()) {
          r.waitForUpdate();
        }
      }
    }

  }
};
