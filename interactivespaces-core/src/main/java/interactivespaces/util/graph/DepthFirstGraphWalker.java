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
 * The graph can be directed or undirected, and should be labeled as such.
 *
 * @param <I>
 *          type of IDs in the graph
 * @param <T>
 *          type of the data in the graph
 *
 * @author Keith M. Hughes
 */
public class DepthFirstGraphWalker<I, T> {

  /**
   * Map from node IDs to their data.
   */
  private Map<I, WalkableGraphNode<I, T>> idToNode = Maps.newHashMap();

  /**
   * Current time in the walker.
   */
  private int time = 0;

  /**
   * {@code true} if the graph is directed.
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
   * @param id
   *          the ID of the node
   * @param data
   *          the data for the node
   *
   * @return the graph node
   */
  public WalkableGraphNode<I, T> addNode(I id, T data) {
    WalkableGraphNode<I, T> node = getNode(id);
    node.setData(data);

    return node;
  }

  /**
   * Add a neighbor for a node.
   *
   * @param node
   *          the node the neighbor will be added to
   * @param neighborNames
   *          the IDs of neighbors
   */
  public void addNodeNeighbor(WalkableGraphNode<I, T> node, I... neighborNames) {
    if (neighborNames != null) {
      for (I neighborName : neighborNames) {
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
  public void walkNode(WalkableGraphNode<I, T> node, GraphWalkerObserver<I, T> observer) {
    node.setDiscovered(true);

    ++time;
    node.setEntryTime(time);

    observer.observeGraphNodeBefore(node);

    for (WalkableGraphNode<I, T> neighbor : node.getNeighbors()) {
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
   * @param id
   *          the ID of the node
   *
   * @return either an existing node or a brand new one if there was none
   */
  public WalkableGraphNode<I, T> getNode(I id) {
    WalkableGraphNode<I, T> node = idToNode.get(id);
    if (node == null) {
      node = new WalkableGraphNode<I, T>(id);
      idToNode.put(id, node);
    }
    return node;
  }

  /**
   * Get a classification for an edge.
   *
   * @param nodeFrom
   *          the node the walker is walking from
   * @param nodeTo
   *          the node the walker is walking to
   *
   * @return the edge classification
   */
  public GraphWalkerEdgeClassification getEdgeClassification(WalkableGraphNode<I, T> nodeFrom,
      WalkableGraphNode<I, T> nodeTo) {
    if (nodeFrom.equals(nodeTo.getParent())) {
      return GraphWalkerEdgeClassification.TREE;
    } else if (nodeTo.isDiscovered() && !nodeTo.isProcessed()) {
      return GraphWalkerEdgeClassification.BACK;
    } else if (nodeTo.isProcessed()) {
      if (nodeTo.getEntryTime() > nodeFrom.getEntryTime()) {
        return GraphWalkerEdgeClassification.FORWARD;
      } else {
        return GraphWalkerEdgeClassification.CROSS;
      }
    }

    return GraphWalkerEdgeClassification.UNCLASSIFIED;
  }
}
