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

package interactivespaces.bridge.message.ros;

import interactivespaces.bridge.message.MessageBridge;
import interactivespaces.bridge.message.MessageBridgeFactory;

import org.apache.commons.logging.Log;
import org.ros.node.ConnectedNode;

/**
 * A {@link MessageBridgeFactory} for ROS messages.
 * 
 * @author Keith M. Hughes
 */
public class RosMessageBridgeFactory implements MessageBridgeFactory {
	/**
	 * The specification compiler.
	 */
	private RosMessageBridgeSpecificationCompiler specificationCompiler;

	/**
	 * Node for the factory to use.
	 */
	private ConnectedNode node;

	/**
	 * @param node
	 *            the ROS node all bridges should be attached to
	 */
	public RosMessageBridgeFactory(ConnectedNode node) {
		this.node = node;
		specificationCompiler = new OgnlRosMessageBridgeSpecificationCompiler();
	}

	@Override
	public MessageBridge newMessageBridge(String specification, Log log) {
		RosMessageBridgeSpecification<Object, Object> spec = specificationCompiler
				.compile(specification);
		
		return new RosRosMessageBridge(node, spec, log);
	}

}
