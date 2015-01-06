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
 * Classifications for edges between graph nodes.
 *
 * @author Keith M. Hughes
 */
public enum GraphWalkerEdgeClassification {

  /**
   * The edge is unclassified.
   */
  UNCLASSIFIED,

  /**
   * The edge directly connects a node to its parent.
   */
  TREE,

  /**
   * The edge points back to a node already visited..
   */
  BACK,

  /**
   * The edge points forward in the walk.
   */
  FORWARD,

  /**
   * The edge crosses paths in the walk.
   */
  CROSS
}
