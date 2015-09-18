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

/**
 * A specification for translating one message into another.
 *
 * @param <SourceMessage>
 *          the class of the source message
 * @param <DestinationMessage>
 *          the class of the destination message
 *
 * @author Keith M. Hughes
 */
public abstract class RosMessageBridgeSpecification<SourceMessage, DestinationMessage> extends
    BaseMessageBridgeSpecification<SourceMessage, DestinationMessage> {

  /**
   * Topic message type of the source.
   */
  private String sourceTopicMessageType;

  /**
   * Topic message type of the source.
   */
  private String destinationTopicMessageType;

  /**
   * Construct a specification.
   *
   * @param sourceTopicName
   *          the topic name for the source message
   * @param sourceTopicMessageType
   *          the message type for the source message
   * @param destinationTopicName
   *          the topic name for the destination message
   * @param destinationTopicMessageType
   *          the message type for the destination message
   */
  public RosMessageBridgeSpecification(String sourceTopicName, String sourceTopicMessageType,
      String destinationTopicName, String destinationTopicMessageType) {
    super(sourceTopicName, destinationTopicName);

    this.sourceTopicMessageType = sourceTopicMessageType;
    this.destinationTopicMessageType = destinationTopicMessageType;
  }

  /**
   * Get the message type of the source topic message.
   *
   * @return the source message type
   */
  public String getSourceTopicMessageType() {
    return sourceTopicMessageType;
  }

  /**
   * Get the message type of the destination topic message.
   *
   * @return the destination message type
   */
  public String getDestinationTopicMessageType() {
    return destinationTopicMessageType;
  }
}
