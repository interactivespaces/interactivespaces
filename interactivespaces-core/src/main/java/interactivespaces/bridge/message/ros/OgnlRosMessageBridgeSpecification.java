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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.bridge.message.BridgeObject;

import java.util.List;

import org.apache.commons.ognl.Ognl;
import org.apache.commons.ognl.OgnlContext;
import org.apache.commons.ognl.OgnlException;

/**
 * Specification for a ROS Message Bridge
 * 
 * @author Keith M. Hughes
 */
public class OgnlRosMessageBridgeSpecification<SourceMessage, DestinationMessage>
		extends
		RosMessageBridgeSpecification<SourceMessage, DestinationMessage> {
	/**
	 * OGNL expressions to be evaluated for the bridge.
	 */
	private List<String> expressions;

	public OgnlRosMessageBridgeSpecification(String sourceTopicName,
			String sourceTopicMessageType, String destinationTopicName,
			String destinationTopicMessageType, List<String> expressions) {
		super(sourceTopicName, sourceTopicMessageType, destinationTopicName,
				destinationTopicMessageType);
		
		this.expressions = expressions;
	}

	/**
	 * Execute the bridge.
	 * 
	 * @param source
	 *            the source message to translate from
	 * @param destination
	 *            the destination message to translate to
	 */
	public void execute(SourceMessage source, DestinationMessage destination) {
		BridgeObject bo = new BridgeObject(source, destination);
		OgnlContext context = newContext();

		try {
			for (String expression : expressions) {
				Ognl.getValue(expression, context, bo);
			}
		} catch (OgnlException e) {
			throw new InteractiveSpacesException("Could not execute bridge expression", e);
		}
	}

	/**
	 * Create a new context for the expression evaluation.
	 * 
	 * @return
	 */
	protected OgnlContext newContext() {
		return new OgnlContext();
	}
}
