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
 * A node in a graph for a graph walker.
 *
 * @param <I>
 *          type of IDs in the graph
 * @param <T>
 *          type of the data in the graph
 *
 * @author Keith M. Hughes
 */
public class WalkableGraphNode<I, T> {

  /**
   * ID of the node.
   */
  private I id;

  /**
   * The data for the node.
   */
  private T data;

  /**
   * All neighbors for the node.
   */
  private Set<WalkableGraphNode<I, T>> neighbors = Sets.newHashSet();

  /**
   * The parent of the node.
   */
  private WalkableGraphNode<I, T> parent;

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

  /**
   * Construct a node node.
   *
   * @param id
   *          ID of the node
   *
   */
  public WalkableGraphNode(I id) {
    this.id = id;
  }

  /**
   * Get the ID of the node.
   *
   * @return the ID
   */
  public I getId() {
    return id;
  }

  /**
   * Get the data for the node.
   *
   * @return the data
   */
  public T getData() {
    return data;
  }

  /**
   * Set the data for the node.
   *
   * @param data
   *          the data to set
   */
  public void setData(T data) {
    this.data = data;
  }

  /**
   * Add a new neighbor to the node.
   *
   * @param neighbor
   *          the neighbor
   */
  public void addNeighbor(WalkableGraphNode<I, T> neighbor) {
    neighbors.add(neighbor);
  }

  /**
   * @return the neighbors
   */
  public Set<WalkableGraphNode<I, T>> getNeighbors() {
    return neighbors;
  }

  /**
   * Get the parent of the node.
   *
   * @return the parent
   */
  public WalkableGraphNode<I, T> getParent() {
    return parent;
  }

  /**
   * Set the parent of the node.
   *
   * @param parent
   *          the parent
   */
  public void setParent(WalkableGraphNode<I, T> parent) {
    this.parent = parent;
  }

  /**
   * Has the node been discovered?
   *
   * @return {@code true} if the node is discovered
   */
  public boolean isDiscovered() {
    return discovered;
  }

  /**
   * Set whether the node has been discovered.
   *
   * @param discovered
   *          {@code true} if the node is discovered
   */
  public void setDiscovered(boolean discovered) {
    this.discovered = discovered;
  }

  /**
   * Has the node been processed?
   *
   * @return {@code true} if the node has been processed
   */
  public boolean isProcessed() {
    return processed;
  }

  /**
   * Set whether the node has been processed.
   *
   * @param processed
   *          {@code true} if the node has been processed
   */
  public void setProcessed(boolean processed) {
    this.processed = processed;
  }

  /**
   * Get the entry time from the graph walk.
   *
   * @return the entry time
   */
  public int getEntryTime() {
    return entryTime;
  }

  /**
   * Set the entry time from the graph walk.
   *
   * @param entryTime
   *          the entry time
   */
  public void setEntryTime(int entryTime) {
    this.entryTime = entryTime;
  }

  /**
   * Get the exit time from the graph walk.
   *
   * @return the exit time
   */
  public int getExitTime() {
    return exitTime;
  }

  /**
   * Set the exit time from the graph walk.
   *
   * @param exitTime
   *          the exit time
   */
  public void setExitTime(int exitTime) {
    this.exitTime = exitTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    WalkableGraphNode<I, T> other = (WalkableGraphNode<I, T>) obj;
    return id.equals(other.id);
  }
}
