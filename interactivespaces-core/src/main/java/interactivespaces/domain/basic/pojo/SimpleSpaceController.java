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

package interactivespaces.domain.basic.pojo;

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.SpaceControllerConfiguration;
import interactivespaces.domain.basic.SpaceControllerMode;
import interactivespaces.domain.pojo.SimpleObject;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * A POJO implementation of a {@link SpaceController}.
 *
 * @author Keith M. Hughes
 */
public class SimpleSpaceController extends SimpleObject implements SpaceController {

  /**
   * For serialization.
   */
  private static final long serialVersionUID = 172497560415594020L;

  /**
   * ID of the host. This should be usable to find the host in the network.
   */
  private String hostId;

  /**
   * UUID of the controller.
   */
  private String uuid;

  /**
   * Name of the controller.
   */
  private String name;

  /**
   * Description of the controller.
   */
  private String description;

  /**
   * Mode of the controller.
   */
  private SpaceControllerMode mode;

  /**
   * The configuration for the space controller.
   */
  private SpaceControllerConfiguration configuration;

  /**
   * The meta data for this space controller.
   */
  private Map<String, Object> metadata = Maps.newHashMap();

  @Override
  public String getHostId() {
    return hostId;
  }

  @Override
  public void setHostId(String hostId) {
    this.hostId = hostId;
  }

  @Override
  public String getUuid() {
    return uuid;
  }

  @Override
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

//  @Override
//  public SpaceControllerConfiguration getConfiguration() {
//    return configuration;
//  }
//
//  @Override
//  public void setConfiguration(SpaceControllerConfiguration configuration) {
//    this.configuration = configuration;
//  }

  @Override
  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  @Override
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  @Override
  public SpaceControllerMode getMode() {
    return mode;
  }

  @Override
  public void setMode(SpaceControllerMode mode) {
    this.mode = mode;
  }
}
