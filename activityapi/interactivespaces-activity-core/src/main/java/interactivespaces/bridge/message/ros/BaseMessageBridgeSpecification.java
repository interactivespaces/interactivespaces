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

package interactivespaces.bridge.message.ros;

import interactivespaces.bridge.message.MessageBridgeSpecification;

/**
 * A base implentation class for {@link MessageBridgeSpecification} instances.
 *
 * @param <SourceMessage>
 *          the class of the source message
 * @param <DestinationMessage>
 *          the class of the destination message
 *
 * @author Keith M. Hughes
 */
public abstract class BaseMessageBridgeSpecification<SourceMessage, DestinationMessage> implements
    MessageBridgeSpecification<SourceMessage, DestinationMessage> {

  /**
   * Topic name of the source.
   */
  private String sourceTopicName;

  /**
   * Topic name of the destination.
   */
  private String destinationTopicName;

  /**
   * Construct a new specification.
   *
   * @param sourceTopicName
   *          the topic name for the source message
   * @param destinationTopicName
   *          the topic name for the destination message
   */
  public BaseMessageBridgeSpecification(String sourceTopicName, String destinationTopicName) {
    this.sourceTopicName = sourceTopicName;
    this.destinationTopicName = destinationTopicName;
  }

  @Override
  public String getSourceTopicName() {
    return sourceTopicName;
  }

  @Override
  public String getDestinationTopicName() {
    return destinationTopicName;
  }
}
