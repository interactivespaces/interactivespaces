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

/**
 * Observes as the graph is walked.
 *
 * @author Keith M. Hughes
 */
public interface GraphWalkerObserver<Data> {

  /**
   * The node is known, but the neighbors haven't been processed yet.
   *
   * @param node
   *          the node whose neighbors are about to be processed
   */
  void observeGraphNodeBefore(GraphNode<Data> node);

  /**
   * The node is known and the neighbors have been processed.
   *
   * @param node
   *          the node whose neighbors have just been processed
   */
  void observeGraphNodeAfter(GraphNode<Data> node);

  /**
   * The node is known and the neighbors have been processed.
   *
   * @param nodeFrom
   *          the node being traveled from
   * @param nodeTo
   *          the node being traveled to
   */
  void observeGraphEdge(GraphNode<Data> nodeFrom, GraphNode<Data> nodeTo,
      GraphEdgeClassification classification);
}
