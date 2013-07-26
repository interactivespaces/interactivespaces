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

package interactivespaces.util.graph;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Walk a graph in a depth-first manner with an observer.
 *
 * <p>
 * The graph can be directed or undirected, and should be labeled as such
 *
 * @author Keith M. Hughes
 */
public class DepthFirstGraphWalker<Data> {

  /**
   * Map from node names to their data.
   */
  private Map<String, GraphNode<Data>> nameToNode = Maps.newHashMap();

  /**
   * Current time in the walker.
   */
  private int time = 0;

  /**
   * True if the graph is directed, false otherwise.
   */
  private boolean directed = true;

  /**
   * Is the graph directed or not?
   *
   * @return {@code true} if the graph is directed.
   */
  public boolean isDirected() {
    return directed;
  }

  /**
   * Set whether or not the graph is directed.
   *
   * @param directed
   *          {@code true} if the graph is directed
   */
  public void setDirected(boolean directed) {
    this.directed = directed;
  }

  /**
   * Add a new node to the graph.
   *
   * @param name
   * @param data
   * @return
   */
  public GraphNode<Data> addNode(String name, Data data) {
    GraphNode<Data> node = getNode(name);
    node.setData(data);

    return node;
  }

  /**
   * Add a neighbor for a node.
   *
   * @param node
   *          the node the neighbor will be added to
   * @param neighborNames
   *          the names of neighbors
   */
  public void addNodeNeighbor(GraphNode<Data> node, String... neighborNames) {
    if (neighborNames != null) {
      for (String neighborName : neighborNames) {
        node.addNeighbor(getNode(neighborName));
      }
    }
  }

  /**
   * Walk a particular node of the graph.
   *
   * @param node
   *          the node to start down
   * @param observer
   *          the observer watching the walk
   */
  public void walkNode(GraphNode<Data> node, GraphWalkerObserver<Data> observer) {
    node.setDiscovered(true);

    ++time;
    node.setEntryTime(time);

    observer.observeGraphNodeBefore(node);

    for (GraphNode<Data> neighbor : node.getNeighbors()) {
      if (!neighbor.isDiscovered()) {
        neighbor.setParent(node);

        observer.observeGraphEdge(node, neighbor, getEdgeClassification(node, neighbor));

        walkNode(neighbor, observer);
      } else if (!neighbor.isProcessed() || directed) {
        observer.observeGraphEdge(node, neighbor, getEdgeClassification(node, neighbor));
      }
    }

    observer.observeGraphNodeAfter(node);

    ++time;
    node.setExitTime(time);

    node.setProcessed(true);
  }

  /**
   * Get the node associated with a given data item.
   *
   * @param name
   *
   * @return either an existing node or a brand new one if there was none
   */
  public GraphNode<Data> getNode(String name) {
    GraphNode<Data> node = nameToNode.get(name);
    if (node == null) {
      node = new GraphNode<Data>(name);
      nameToNode.put(name, node);
    }
    return node;
  }

  /**
   * Get a classification for an edge
   *
   * @param node1
   *          the node the walker is walking from
   * @param node2
   *          the node the walker is walking to
   *
   * @return the edge classification
   */
  public GraphEdgeClassification
      getEdgeClassification(GraphNode<Data> node1, GraphNode<Data> node2) {
    if (node1.equals(node2.getParent())) {
      return GraphEdgeClassification.TREE;
    } else if (node2.isDiscovered() && !node2.isProcessed()) {
      return GraphEdgeClassification.BACK;
    } else if (node2.isProcessed()) {
      if (node2.getEntryTime() > node1.getEntryTime()) {
        return GraphEdgeClassification.FORWARD;
      } else {
        return GraphEdgeClassification.CROSS;
      }
    }

    return GraphEdgeClassification.UNCLASSIFIED;
  }
}
