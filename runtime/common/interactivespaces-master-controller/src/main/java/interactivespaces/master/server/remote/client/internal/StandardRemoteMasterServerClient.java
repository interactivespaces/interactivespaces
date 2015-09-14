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

package interactivespaces.master.server.remote.client.internal;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.server.remote.RemoteMasterServerMessages;
import interactivespaces.master.server.remote.client.RemoteMasterServerClient;
import interactivespaces.service.web.HttpConstants;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.data.json.JsonMapper;
import interactivespaces.util.data.json.StandardJsonMapper;
import interactivespaces.util.web.HttpClientHttpContentCopier;
import interactivespaces.util.web.HttpContentCopier;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * A client for talking to a remote Master Server.
 *
 * @author Keith M. Hughes
 */
public class StandardRemoteMasterServerClient implements RemoteMasterServerClient {

  /**
   * The number of connections for the HTTP client.
   */
  private static final int HTTP_CLIENT_NUMBER_CONNECTIONS = 1;

  /**
   * The JSON mapper.
   */
  private static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

  /**
   * The HTTP client for communicating with the master.
   */
  private HttpContentCopier httpClient;

  /**
   * The space environment for this communicator.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The network host name for the master.
   */
  private String masterHostname;

  /**
   * The port that the master communication system is listening on.
   */
  private int masterCommunicationPort;

  /**
   * Construct a new client.
   *
   * @param spaceEnvironment
   *          the space environment
   */
  public StandardRemoteMasterServerClient(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void startup() {
    httpClient = new HttpClientHttpContentCopier(HTTP_CLIENT_NUMBER_CONNECTIONS);
    httpClient.startup();

    masterHostname =
        spaceEnvironment.getSystemConfiguration().getRequiredPropertyString(
            RemoteMasterServerMessages.CONFIGURATION_MASTER_HOST);
    masterCommunicationPort =
        spaceEnvironment.getSystemConfiguration().getPropertyInteger(
            RemoteMasterServerMessages.CONFIGURATION_MASTER_COMMUNICATION_PORT,
            RemoteMasterServerMessages.CONFIGURATION_MASTER_COMMUNICATION_PORT_DEFAULT);
  }

  @Override
  public void shutdown() {
    if (httpClient != null) {
      httpClient.shutdown();
      httpClient = null;
    }
  }

  @Override
  public void registerSpaceController(SpaceController controller) {
    try {
      Map<String, Object> registrationData = Maps.newHashMap();
      registrationData.put(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_UUID, controller.getUuid());
      registrationData.put(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_HOST_ID, controller.getHostId());
      registrationData.put(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_NAME, controller.getName());
      registrationData
          .put(RemoteMasterServerMessages.CONTROLLER_REGISTRATION_DESCRIPTION, controller.getDescription());

      StringBuilder sourceUri =
          new StringBuilder().append(HttpConstants.HTTP_URL_PREFIX).append(masterHostname)
              .append(HttpConstants.URL_PORT_SEPARATOR).append(masterCommunicationPort)
              .append(RemoteMasterServerMessages.URI_PREFIX_MASTER_SPACECONTROLLER)
              .append(RemoteMasterServerMessages.MASTER_SPACE_CONTROLLER_METHOD_REGISTER)
              .append(HttpConstants.URL_QUERY_STRING_SEPARATOR)
              .append(RemoteMasterServerMessages.MASTER_METHOD_FIELD_CONTROLLER_REGISTRATION_DATA)
              .append(HttpConstants.URL_QUERY_NAME_VALUE_SEPARATOR)
              .append(URLEncoder.encode(MAPPER.toString(registrationData), Charsets.UTF_8.name()));

      httpClient.getContentAsString(sourceUri.toString());
    } catch (UnsupportedEncodingException e) {
      SimpleInteractiveSpacesException.throwFormattedException("Unsupported encoding for controller registration");
    } catch (InteractiveSpacesException e) {
      throw e;
    }
  }
}
