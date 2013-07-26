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

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * A node in a graph.
 *
 * @author Keith M. Hughes
 */
public class GraphNode<Data> {

  /**
   * Name of the node.
   */
  private String name;

  /**
   * The data for the node.
   */
  private Data data;

  /**
   * All neighbors for the node.
   */
  private Set<GraphNode<Data>> neighbors = Sets.newHashSet();

  /**
   * The parent of the node.
   */
  private GraphNode<Data> parent;

  /**
   * Has this node been discovered yet?
   */
  private boolean discovered;

  /**
   * Has this node been processed yet?
   */
  private boolean processed;

  /**
   * When, in the walk, was the node discovered?
   */
  private int entryTime;

  /**
   * When, in the walk, was the node exited?
   */
  private int exitTime;

  public GraphNode(String name) {
    this.name = name;
  }

  /**
   * @return the data
   */
  public Data getData() {
    return data;
  }

  /**
   * @param data
   *          the data to set
   */
  public void setData(Data data) {
    this.data = data;
  }

  /**
   * Add a new neighbor to the node.
   *
   * @param neighbor
   *          the neighbor
   */
  public void addNeighbor(GraphNode<Data> neighbor) {
    neighbors.add(neighbor);
  }

  /**
   * @return the neighbors
   */
  public Set<GraphNode<Data>> getNeighbors() {
    return neighbors;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the parent
   */
  public GraphNode<Data> getParent() {
    return parent;
  }

  /**
   * @param parent
   *          the parent to set
   */
  public void setParent(GraphNode<Data> parent) {
    this.parent = parent;
  }

  /**
   * @return the discovered
   */
  public boolean isDiscovered() {
    return discovered;
  }

  /**
   * @param discovered
   *          the discovered to set
   */
  public void setDiscovered(boolean discovered) {
    this.discovered = discovered;
  }

  /**
   * @return the processed
   */
  public boolean isProcessed() {
    return processed;
  }

  /**
   * @param processed
   *          the processed to set
   */
  public void setProcessed(boolean processed) {
    this.processed = processed;
  }

  /**
   * @return the entryTime
   */
  public int getEntryTime() {
    return entryTime;
  }

  /**
   * @param entryTime
   *          the entryTime to set
   */
  public void setEntryTime(int entryTime) {
    this.entryTime = entryTime;
  }

  /**
   * @return the exitTime
   */
  public int getExitTime() {
    return exitTime;
  }

  /**
   * @param exitTime
   *          the exitTime to set
   */
  public void setExitTime(int exitTime) {
    this.exitTime = exitTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GraphNode<Data> other = (GraphNode<Data>) obj;
    if (!name.equals(other.name))
      return false;
    return true;
  }
}
