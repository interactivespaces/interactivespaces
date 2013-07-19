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

package interactivespaces.activity.impl.ros;

import interactivespaces.activity.Activity;
import interactivespaces.bridge.message.MessageBridge;
import interactivespaces.bridge.message.MessageBridgeFactory;
import interactivespaces.bridge.message.ros.RosMessageBridgeFactory;
import interactivespaces.util.io.Files;

import java.io.File;

/**
 * An {@link Activity} which provides a bridge between two Interactive Spaces
 * topics.
 *
 * @author Keith M. Hughes
 */
public class TopicBridgeRosActivity extends BaseRosActivity {

  /**
   * Configuration property which gives the locations of the bridge
   * configuration files.
   *
   * <p>
   * Relative files are relative to the app install directory.
   */
  public static final String CONFIGURATION_BRIDGE_TOPIC_FILE = "space.bridge.topic";

  /**
   * The bridge between two topics.
   */
  private MessageBridge topicMessageBridge;

  @Override
  public void commonActivitySetup() {
    MessageBridgeFactory messageBridgeFactory = new RosMessageBridgeFactory(getMainNode());

    File confFile =
        new File(getActivityFilesystem().getInstallDirectory(), getConfiguration()
            .getRequiredPropertyString(CONFIGURATION_BRIDGE_TOPIC_FILE));

    topicMessageBridge = messageBridgeFactory.newMessageBridge(Files.readFile(confFile), getLog());
    topicMessageBridge.startup();
  }

  @Override
  public void commonActivityCleanup() {
    if (topicMessageBridge != null) {
      topicMessageBridge.shutdown();
      topicMessageBridge = null;
    }
  }
}
