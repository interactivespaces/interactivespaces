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
 * A {@link GraphWalkerObserver} which provides no nothing defaults for all methods.
 *
 * @param <I>
 *          type of IDs in the graph
 * @param <T>
 *          type of the data in the graph
 *
 * @author Keith M. Hughes
 */
public class BaseGraphWalkerObserver<I, T> implements GraphWalkerObserver<I, T> {

  @Override
  public void observeGraphNodeBefore(WalkableGraphNode<I, T> node) {
    // Default is do nothing
  }

  @Override
  public void observeGraphNodeAfter(WalkableGraphNode<I, T> node) {
    // Default is do nothing
  }

  @Override
  public void observeGraphEdge(WalkableGraphNode<I, T> nodeFrom, WalkableGraphNode<I, T> nodeTo,
      GraphWalkerEdgeClassification classification) {
    // Default is do nothing
  }
}
