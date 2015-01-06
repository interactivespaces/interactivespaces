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

import interactivespaces.InteractiveSpacesException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A {@link GraphWalkerObserver} which gives a dependency mapping.
 *
 * <p>
 * If a depends on b, a will appear after b in the ordering. The sort is stable with respect to the order of nodes added
 * to the resolver, that is nodes which have no dependency connection to each other will appear in the order added.
 *
 * <p>
 * Does not allow acyclic graphs and will throw an exception during the walk.
 *
 * @param <I>
 *          type of IDs in the graph
 * @param <T>
 *          type of the data in the graph
 *
 * @author Keith M. Hughes
 */
public class DependencyResolver<I, T> extends BaseGraphWalkerObserver<I, T> {

  /**
   * The ordering of the data.
   */
  private List<T> ordering = Lists.newArrayList();

  /**
   * The nodes added to the resolver.
   *
   * <p>
   * Making this a linked hash set is what makes the dependency sort stable.
   */
  private Set<WalkableGraphNode<I, T>> nodes = Sets.newLinkedHashSet();

  /**
   * The walker that will walk the graph.
   */
  private DepthFirstGraphWalker<I, T> walker = new DepthFirstGraphWalker<I, T>();

  /**
   * Add a new node to the graph.
   *
   * @param nodeId
   *          the ID of the node
   * @param data
   *          the node data
   */
  public void addNode(I nodeId, T data) {
    WalkableGraphNode<I, T> node = walker.getNode(nodeId);
    node.setData(data);

    nodes.add(node);
  }

  /**
   * Add a collection of dependencies to a node.
   *
   * @param nodeId
   *          the ID of the node the dependencies will be added to
   * @param dependencyIds
   *          the IDs of the dependencies
   */
  public void addNodeDependencies(I nodeId, I... dependencyIds) {
    WalkableGraphNode<I, T> node = walker.getNode(nodeId);
    if (dependencyIds != null) {
      for (I neighborId : dependencyIds) {
        node.addNeighbor(walker.getNode(neighborId));
      }
    }
  }

  /**
   * Add a collection of dependencies for a node.
   *
   * @param nodeId
   *          the ID of the node the dependencies will be added to
   * @param dependencyIds
   *          the IDs of neighbors
   */
  public void addNodeDependencies(I nodeId, Collection<I> dependencyIds) {
    WalkableGraphNode<I, T> node = walker.getNode(nodeId);
    if (dependencyIds != null) {
      for (I neighborId : dependencyIds) {
        node.addNeighbor(walker.getNode(neighborId));
      }
    }
  }

  @Override
  public void observeGraphNodeAfter(WalkableGraphNode<I, T> node) {
    ordering.add(node.getData());
  }

  @Override
  public void observeGraphEdge(WalkableGraphNode<I, T> nodeFrom, WalkableGraphNode<I, T> nodeTo,
      GraphWalkerEdgeClassification classification) {
    if (classification.equals(GraphWalkerEdgeClassification.BACK)) {
      throw new InteractiveSpacesException(String.format("Cycle in dependency graph from %s to %s", nodeFrom.getData(),
          nodeTo.getData()));
    }
  }

  /**
   * Calculate the dependency ordering.
   */
  public void resolve() {
    walker.setDirected(true);

    for (WalkableGraphNode<I, T> node : nodes) {
      if (!node.isDiscovered()) {
        walker.walkNode(node, this);
      }
    }
  }

  /**
   * Get the final ordering.
   *
   * @return the ordering
   */
  public List<T> getOrdering() {
    return ordering;
  }
}
