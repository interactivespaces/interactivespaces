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

package interactivespaces.master.server.services.internal;

import interactivespaces.domain.space.Space;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Walk over a space performing the given operation at the appropriate
 * points.
 * 
 * <p>
 * Spaces are walked in a breadth first manner.
 * 
 * @author Keith M. Hughes
 */
abstract class SpaceWalker {
	
	/**
	 * Queue needed for breadth-first walk.
	 */
	private Queue<Space> walkingQueue = new LinkedList<Space>();

	/**
	 * Walk the space tree.
	 * 
	 * @param space
	 *            Root of the tree to walk.
	 */
	public void walk(Space space) {
		doBegin();

		walkingQueue.offer(space);
		while (!walkingQueue.isEmpty()) {
			Space curSpace = walkingQueue.remove();
			walkingQueue.addAll(curSpace.getSpaces());

			doVisit(curSpace);
		}

		doEnd();
	}

	/**
	 * Do whatever is needed before the walk begins.
	 */
	protected void doBegin() {
		// Default is do nothing.
	}

	/**
	 * Do whatever is needed at the end of the walk.
	 */
	protected void doEnd() {
		// Default is do nothing.
	}

	/**
	 * Do whatever needs to be done at a particular individual space node.
	 * 
	 * @param space
	 *            the space to operate on.
	 */
	protected abstract void doVisit(Space space);
}
