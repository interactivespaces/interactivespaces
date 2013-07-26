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
 * A {@link GraphWalkerObserver} which provides no nothing defaults for all
 * methods.
 *
 * @author Keith M. Hughes
 */
public class BaseGraphWalkerObserver<Data> implements GraphWalkerObserver<Data> {

  @Override
  public void observeGraphNodeBefore(GraphNode<Data> node) {
    // Default is do nothing
  }

  @Override
  public void observeGraphNodeAfter(GraphNode<Data> node) {
    // Default is do nothing
  }

  @Override
  public void observeGraphEdge(GraphNode<Data> nodeFrom, GraphNode<Data> nodeTo,
      GraphEdgeClassification classification) {
    // Default is do nothing
  }
}
