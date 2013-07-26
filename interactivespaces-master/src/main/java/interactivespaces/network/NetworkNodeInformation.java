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

package interactivespaces.network;

import java.util.List;

/**
 * Information about a network node.
 *
 * @author Keith M. Hughes
 */
public class NetworkNodeInformation {

  /**
   * name of the node.
   */
  private String nodeName;

  /**
   * Sorted list of publisher topics.
   */
  private List<String> publisherTopics;

  /**
   * Sorted list of subscriber topics.
   */
  private List<String> subscriberTopics;

  public NetworkNodeInformation(String topicName, List<String> publisherTopics,
      List<String> subscriberTopics) {
    this.nodeName = topicName;
    this.publisherTopics = publisherTopics;
    this.subscriberTopics = subscriberTopics;
  }

  /**
   * @return the nodeName
   */
  public String getNodeName() {
    return nodeName;
  }

  /**
   * Get the name of all publisherTopics.
   *
   * @return sorted list of names of publisherTopics
   */
  public List<String> getPublisherTopics() {
    return publisherTopics;
  }

  /**
   * Get the name of all subscriberTopics.
   *
   * @return sorted list of names of subscriberTopics
   */
  public List<String> getSubscriberTopics() {
    return subscriberTopics;
  }
}
