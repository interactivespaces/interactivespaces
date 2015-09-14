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
 * Information about a network topic.
 *
 * @author Keith M. Hughes
 */
public class NetworkTopicInformation {

  /**
   * name of the topic.
   */
  private String topicName;

  /**
   * Sorted list of publishers.
   */
  private List<String> publishers;

  /**
   * Sorted list of subscribers.
   */
  private List<String> subscribers;

  public NetworkTopicInformation(String topicName, List<String> publishers, List<String> subscribers) {
    this.topicName = topicName;
    this.publishers = publishers;
    this.subscribers = subscribers;
  }

  /**
   * @return the topicName
   */
  public String getTopicName() {
    return topicName;
  }

  /**
   * Get the name of all publishers.
   *
   * @return sorted list of names of publishers
   */
  public List<String> getPublishers() {
    return publishers;
  }

  /**
   * Get the name of all subscribers.
   *
   * @return sorted list of names of subscribers
   */
  public List<String> getSubscribers() {
    return subscribers;
  }
}
