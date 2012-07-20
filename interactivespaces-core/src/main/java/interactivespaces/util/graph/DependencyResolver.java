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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A {@link GraphWalkerObserver} which gives a dependency mapping.
 * 
 * <p>
 * If a depends on b, a will appear after b in the ordering.
 * 
 * <p>
 * Does not allow acyclic graphs and will throw an exception during the walk.
 * 
 * @author Keith M. Hughes
 */
public class DependencyResolver<Data> extends BaseGraphWalkerObserver<Data> {

	/**
	 * The ordering of the data.
	 */
	private List<Data> ordering = Lists.newArrayList();

	/**
	 * The nodes added to the resolver.
	 */
	private Set<GraphNode<Data>> nodes = Sets.newHashSet();

	/**
	 * The walker that will walk the graph
	 */
	private DepthFirstGraphWalker<Data> walker = new DepthFirstGraphWalker<Data>();

	/**
	 * Add a new node to the graph.
	 * 
	 * @param nodeName
	 *            the name of the node
	 * @param data
	 *            the node data
	 */
	public void addNode(String nodeName, Data data) {
		GraphNode<Data> node = walker.getNode(nodeName);
		node.setData(data);

		nodes.add(node);
	}

	/**
	 * Add a collection of dependencies to a node.
	 * 
	 * @param nodeName
	 *            the name of the node the dependencies will be added to
	 * @param dependencyNames
	 *            the names of the dependencies
	 */
	public void addNodeDependencies(String nodeName, String... dependencyNames) {
		GraphNode<Data> node = walker.getNode(nodeName);
		if (dependencyNames != null) {
			for (String neighborName : dependencyNames) {
				node.addNeighbor(walker.getNode(neighborName));
			}
		}
	}

	/**
	 * Add a collection of dependencies for a node.
	 * 
	 * @param nodeName
	 *            the name of the node the dependencies will be added to
	 * @param dependencyNames
	 *            the names of neighbors
	 */
	public void addNodeDependencies(String nodeName,
			Collection<String> dependencyNames) {
		GraphNode<Data> node = walker.getNode(nodeName);
		if (dependencyNames != null) {
			for (String neighborName : dependencyNames) {
				node.addNeighbor(walker.getNode(neighborName));
			}
		}
	}

	@Override
	public void observeGraphNodeAfter(GraphNode<Data> node) {
		ordering.add(node.getData());
	}

	@Override
	public void observeGraphEdge(GraphNode<Data> nodeFrom,
			GraphNode<Data> nodeTo, GraphEdgeClassification classification) {
		if (classification.equals(GraphEdgeClassification.BACK)) {
			throw new InteractiveSpacesException(String.format(
					"Cycle in dependency graph from %s to %s",
					nodeFrom.getData(), nodeTo.getData()));
		}
	}

	/**
	 * Calculate the dependency ordering.
	 */
	public void resolve() {
		walker.setDirected(true);

		for (GraphNode<Data> node : nodes) {
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
	public List<Data> getOrdering() {
		return ordering;
	}
}
