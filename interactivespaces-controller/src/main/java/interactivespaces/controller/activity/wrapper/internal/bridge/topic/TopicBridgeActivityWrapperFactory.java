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

package interactivespaces.controller.activity.wrapper.internal.bridge.topic;

import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.ActivityRuntime;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.activity.wrapper.ActivityWrapper;
import interactivespaces.controller.activity.wrapper.ActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.BaseActivityWrapperFactory;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;

/**
 * A {@link ActivityWrapperFactory} for running topic bridges.
 *
 * @author Keith M. Hughes
 */
public class TopicBridgeActivityWrapperFactory extends BaseActivityWrapperFactory {

  /**
   * The name of the activity type.
   */
  public static final String ACTIVITY_TYPE_NAME = "topic_bridge";

  @Override
  public String getActivityType() {
    return ACTIVITY_TYPE_NAME;
  }

  @Override
  public ActivityWrapper newActivityWrapper(InstalledLiveActivity liapp, ActivityFilesystem activityFilesystem,
      Configuration configuration, ActivityRuntime activityRuntime) {
    return new TopicBridgeActivityWrapper();
  }
}
