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

package interactivespaces.master.server.remote.master.internal;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.logging.ExtendedLog;
import interactivespaces.master.communication.MasterCommunicationManager;
import interactivespaces.master.server.remote.RemoteMasterServerMessages;
import interactivespaces.master.server.remote.master.RemoteMasterServer;
import interactivespaces.master.server.remote.master.RemoteMasterServerListener;
import interactivespaces.service.web.HttpResponseCode;
import interactivespaces.service.web.server.HttpDynamicRequestHandler;
import interactivespaces.service.web.server.HttpRequest;
import interactivespaces.service.web.server.HttpResponse;
import interactivespaces.util.data.json.JsonMapper;
import interactivespaces.util.data.json.StandardJsonMapper;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A {@link RemoteMasterServer} using a web server.
 *
 * @author Keith M. Hughes
 */
public class StandardRemoteMasterServer implements RemoteMasterServer {

  /**
   * The JSON mapper to use for the server.
   */
  private static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * Logger for the controller.
   */
  private ExtendedLog log;

  /**
   * The master communication manager.
   */
  private MasterCommunicationManager masterCommunicationManager;

  /**
   * All listeners for master server events.
   */
  private List<RemoteMasterServerListener> listeners = Lists.newCopyOnWriteArrayList();

  @Override
  public void startup() {
    // TODO(keith): Make a web socket connection when the master/controller comms move from ROS.
    masterCommunicationManager.getWebServer().addDynamicContentHandler(
        RemoteMasterServerMessages.URI_PREFIX_MASTER_SPACECONTROLLER, true, new MyHttpDynamicRequestHandler());
  }

  @Override
  public void shutdown() {
    // Nothing to do for now
  }

  @Override
  public void addListener(RemoteMasterServerListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeListener(RemoteMasterServerListener listener) {
    listeners.remove(listener);
  }

  /**
   * A controller registration has come in.
   *
   * @param data
   *          the registration data
   */
  private void handleControllerRegistration(Map<String, Object> data) {
    String uuid = (String) data.get(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_UUID);
    String hostId = (String) data.get(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_HOST_ID);

    log.formatInfo("Controller %s (Host ID %s) is online.", uuid, hostId);

    SpaceController controller = new SimpleSpaceController();
    controller.setUuid(uuid);
    controller.setName((String) data.get(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_NAME));
    controller.setDescription((String) data.get(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_DESCRIPTION));
    controller.setHostId(hostId);

    signalControllerRegistration(controller);
  }

  /**
   * Signal all listeners about a new controller registration.
   *
   * @param controller
   *          information about the controller
   */
  private void signalControllerRegistration(SpaceController controller) {
    for (RemoteMasterServerListener listener : listeners) {
      listener.onControllerRegistration(controller);
    }
  }

  /**
   * Set the master communication manager.
   *
   * @param masterCommunicationManager
   *          the master communication manager
   */
  public void setMasterCommunicationManager(MasterCommunicationManager masterCommunicationManager) {
    this.masterCommunicationManager = masterCommunicationManager;
  }

  /**
   * Set the logger.
   *
   * @param log
   *          the log to set
   */
  public void setLog(ExtendedLog log) {
    this.log = log;
  }

  /**
   * HTTP dynamic request handler for calls coming into the remote master server.
   *
   * @author Keith M. Hughes
   */
  private class MyHttpDynamicRequestHandler implements HttpDynamicRequestHandler {

    @Override
    public void handle(HttpRequest request, HttpResponse response) {
      String methodName =
          request.getUri().getPath().substring(RemoteMasterServerMessages.URI_PREFIX_MASTER_SPACECONTROLLER.length());
      if (methodName.equals(RemoteMasterServerMessages.MASTER_SPACE_CONTROLLER_METHOD_REGISTER)) {
        handleRegister(request, response);
      } else {
        log.formatWarn("Received unknown remote master server method name %s", methodName);
        response.setResponseCode(HttpResponseCode.NOT_FOUND);
        try {
          response.getOutputStream().write(RemoteMasterServerMessages.MASTER_METHOD_RESPONSE_FAILURE.getBytes());
        } catch (IOException e) {
          throw InteractiveSpacesException.newFormattedException(e,
              "Could not write failure response for remote master server request %s", request.getUri());
        }
      }
    }

    /**
     * Handle a space controller registration request.
     *
     * @param request
     *          the http request
     * @param response
     *          the http response
     */
    private void handleRegister(HttpRequest request, HttpResponse response) {
      try {
        String data =
            request.getUriQueryParameters().get(
                RemoteMasterServerMessages.MASTER_METHOD_FIELD_CONTROLLER_REGISTRATION_DATA);
        if (data != null) {
          Map<String, Object> registrationData = MAPPER.parseObject(data);
          handleControllerRegistration(registrationData);
        }
        response.setContentType(RemoteMasterServerMessages.REMOTE_MASTER_RESPONSE_CONTENT_TYPE);
        response.getOutputStream().write(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_SUCCESS.getBytes());
      } catch (Throwable e) {
        response.setResponseCode(HttpResponseCode.INTERNAL_SERVER_ERROR);
        try {
          response.getOutputStream().write(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_FAILURE.getBytes());
        } catch (IOException e1) {
          throw InteractiveSpacesException.newFormattedException(e,
              "Could not write failure response for controller registration %s", request.getUri());
        }

        throw InteractiveSpacesException.newFormattedException(e, "Could not process controller registration %s",
            request.getUri());
      }
    }
  }
}
