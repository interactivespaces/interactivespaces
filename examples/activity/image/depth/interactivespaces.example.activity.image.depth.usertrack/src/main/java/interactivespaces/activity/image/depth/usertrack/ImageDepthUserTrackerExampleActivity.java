/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.activity.image.depth.usertrack;

import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;
import interactivespaces.interaction.model.entity.TrackedEntity;
import interactivespaces.interaction.model.entity.TrackedEntityListener;
import interactivespaces.service.image.depth.DepthCameraService;
import interactivespaces.service.image.depth.UserTrackerDepthCameraEndpoint;
import interactivespaces.util.data.json.JsonBuilder;
import interactivespaces.util.data.json.StandardJsonBuilder;
import interactivespaces.util.geometry.Vector3;

import java.util.List;

/**
 * An activity that tracks users in front of a depth camera.
 *
 * @author Keith M. Hughes
 */
public class ImageDepthUserTrackerExampleActivity extends BaseRoutableRosActivity {

  /**
   * Route channel to write on.
   */
  public static final String ROUTE_CHANNEL = "output1";

  /**
   * The message property giving an array of detected entities.
   */
  public static final String MESSAGE_PROPERTY_ENTITIES = "entities";

  /**
   * The message property giving the ID for a particular entity position.
   */
  public static final String MESSAGE_PROPERTY_ENTITY_ID = "id";

  /**
   * The message property giving the x coordinate for a particular entity
   * position.
   */
  public static final String MESSAGE_PROPERTY_ENTITY_X = "x";

  /**
   * The message property giving the y coordinate for a particular entity
   * position.
   */
  public static final String MESSAGE_PROPERTY_ENTITY_Y = "y";

  /**
   * The message property giving the z coordinate for a particular entity
   * position.
   */
  public static final String MESSAGE_PROPERTY_ENTITY_Z = "z";

  @Override
  public void onActivitySetup() {
    getLog().info("Depth camera usertrack activity starting!");

    DepthCameraService service =
        getSpaceEnvironment().getServiceRegistry().getRequiredService(DepthCameraService.SERVICE_NAME);

    UserTrackerDepthCameraEndpoint endpoint = service.newUserTrackerDepthCameraEndpoint(getLog());
    endpoint.addTrackedEntityListener(new TrackedEntityListener<Vector3>() {

      @Override
      public void onTrackedEntityUpdate(List<TrackedEntity<Vector3>> entities) {
        handleTrackedEntityUpdate(entities);
      }
    });

    addManagedResource(endpoint);
  }

  /**
   * Handle a tracked entity update.
   *
   * @param entities
   *          the entities
   */
  private void handleTrackedEntityUpdate(List<TrackedEntity<Vector3>> entities) {

    if (isActivated()) {
      JsonBuilder message = new StandardJsonBuilder();

      message.newArray(MESSAGE_PROPERTY_ENTITIES);

      for (TrackedEntity<Vector3> entity : entities) {
        message.newObject();

        message.put(MESSAGE_PROPERTY_ENTITY_ID, entity.getId());

        Vector3 position = entity.getPosition();
        message.put(MESSAGE_PROPERTY_ENTITY_X, position.getV0());
        message.put(MESSAGE_PROPERTY_ENTITY_Y, position.getV1());
        message.put(MESSAGE_PROPERTY_ENTITY_Z, position.getV2());

        message.up();
      }

      getLog().debug(String.format("Entities detected: %s", message));

      sendOutputJsonBuilder(ROUTE_CHANNEL, message);
    }
  }
}
