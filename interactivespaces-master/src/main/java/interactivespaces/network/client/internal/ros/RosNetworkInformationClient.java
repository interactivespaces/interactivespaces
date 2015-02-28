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

package interactivespaces.network.client.internal.ros;

import interactivespaces.master.server.services.internal.ros.MasterRosContext;
import interactivespaces.network.NetworkNodeInformation;
import interactivespaces.network.NetworkTopicInformation;
import interactivespaces.network.client.NetworkInformationClient;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.apache.commons.logging.Log;
import org.ros.master.client.MasterStateClient;
import org.ros.master.client.SystemState;
import org.ros.master.client.TopicSystemState;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Obtain information from the ROS network.
 *
 * @author Keith M. Hughes
 */
public class RosNetworkInformationClient implements NetworkInformationClient {

  /**
   * ROS context for the master.
   */
  private MasterRosContext masterRosContext;

  /**
   * Client for getting information about the state of the network from the master.
   */
  private MasterStateClient masterStateClient;

  /**
   * Comparator for comparing strings with each other.
   */
  private Comparator<String> lowerCaseStringComparator;

  /**
   * Logger for this client.
   */
  private Log log;

  /**
   * Comparator for comparing {@link NetworkTopicInformation} with each other.
   */
  private Comparator<NetworkTopicInformation> networkTopicInformationComparator;

  /**
   * Comparator for comparing {@link NetworkNodeInformation} with each other.
   */
  private Comparator<NetworkNodeInformation> networkNodeInformationComparator;

  /**
   * Construct a new client.
   */
  public RosNetworkInformationClient() {
    lowerCaseStringComparator = new Comparator<String>() {

      @Override
      public int compare(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
      }
    };

    networkTopicInformationComparator = new Comparator<NetworkTopicInformation>() {

      @Override
      public int compare(NetworkTopicInformation o1, NetworkTopicInformation o2) {
        return o1.getTopicName().compareToIgnoreCase(o2.getTopicName());
      }
    };

    networkNodeInformationComparator = new Comparator<NetworkNodeInformation>() {

      @Override
      public int compare(NetworkNodeInformation o1, NetworkNodeInformation o2) {
        return o1.getNodeName().compareToIgnoreCase(o2.getNodeName());
      }
    };
  }

  @Override
  public void startup() {
    masterStateClient =
        new MasterStateClient(masterRosContext.getMasterNode(), masterRosContext.getRosEnvironment().getMasterUri());
  }

  @Override
  public void shutdown() {
    masterStateClient = null;
  }

  @Override
  public List<NetworkTopicInformation> getTopics() {
    List<NetworkTopicInformation> topics = collectTopics();

    Collections.sort(topics, networkTopicInformationComparator);

    return topics;
  }

  /**
   * Make a call to the master and return all topics on the master.
   *
   * @return a potentially unsorted list of topics on the master
   */
  private List<NetworkTopicInformation> collectTopics() {
    List<NetworkTopicInformation> topics = Lists.newArrayList();
    SystemState systemState = masterStateClient.getSystemState();

    for (TopicSystemState topicState : systemState.getTopics()) {
      List<String> publishers = Lists.newArrayList(topicState.getPublishers());
      Collections.sort(publishers, lowerCaseStringComparator);

      List<String> subscribers = Lists.newArrayList(topicState.getSubscribers());
      Collections.sort(subscribers, lowerCaseStringComparator);

      topics.add(new NetworkTopicInformation(topicState.getTopicName(), publishers, subscribers));
    }

    return topics;
  }

  @Override
  public List<NetworkNodeInformation> getNodes() {
    List<NetworkNodeInformation> nodes = collectNodes();

    Collections.sort(nodes, networkNodeInformationComparator);

    return nodes;
  }

  /**
   * Make a call to the master and return all topics on the master.
   *
   * @return a potentially unsorted list of topics on the master
   */
  private List<NetworkNodeInformation> collectNodes() {
    List<NetworkNodeInformation> nodes = Lists.newArrayList();

    // maps from node name to the appropriate topic type
    Multimap<String, String> publishers = ArrayListMultimap.create();
    Multimap<String, String> subscribers = ArrayListMultimap.create();

    SystemState systemState = masterStateClient.getSystemState();
    for (TopicSystemState topicState : systemState.getTopics()) {
      String topicName = topicState.getTopicName();
      for (String publisher : topicState.getPublishers()) {
        publishers.put(publisher, topicName);
      }
      for (String subscriber : topicState.getSubscribers()) {
        subscribers.put(subscriber, topicName);
      }

    }

    Set<String> nodesSeen = Sets.newHashSet();
    for (String nodeName : publishers.keySet()) {
      nodesSeen.add(nodeName);

      List<String> publisherTopics = Lists.newArrayList(publishers.get(nodeName));
      Collections.sort(publisherTopics, lowerCaseStringComparator);

      List<String> subscriberTopics = Lists.newArrayList(subscribers.get(nodeName));
      Collections.sort(subscriberTopics, lowerCaseStringComparator);

      nodes.add(new NetworkNodeInformation(nodeName, publisherTopics, subscriberTopics));
    }

    for (String nodeName : subscribers.keySet()) {
      if (nodesSeen.contains(nodeName)) {
        continue;
      }

      // No publisher topics or would have gotten above.
      List<String> publisherTopics = Lists.newArrayList();

      List<String> subscriberTopics = Lists.newArrayList(subscribers.get(nodeName));
      Collections.sort(subscriberTopics, lowerCaseStringComparator);

      nodes.add(new NetworkNodeInformation(nodeName, publisherTopics, subscriberTopics));
    }

    return nodes;
  }

  /**
   * Set the Master ROS context.
   *
   * @param masterRosContext
   *          the Master ROS context
   */
  public void setMasterRosContext(MasterRosContext masterRosContext) {
    this.masterRosContext = masterRosContext;
  }

  /**
   * Set the logger.
   *
   * @param log
   *          the log
   */
  public void setLog(Log log) {
    this.log = log;
  }
}
