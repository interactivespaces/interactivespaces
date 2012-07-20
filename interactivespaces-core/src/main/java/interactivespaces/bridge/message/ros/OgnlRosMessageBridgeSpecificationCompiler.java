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

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A {@link RosMessageBridgeSpecificationCompiler} which uses OGNL.
 * 
 * @author Keith M. Hughes
 */
public class OgnlRosMessageBridgeSpecificationCompiler implements
		RosMessageBridgeSpecificationCompiler {
	@Override
	public <SourceType, DestinationType> RosMessageBridgeSpecification<SourceType, DestinationType> compile(
			String source) {
		String[] lines = source.split(";");
		if (lines.length < 2) {
			throw new InteractiveSpacesException(
					"Not enough lines in the bridge specification");
		}

		String[] sources = lines[0].split(":");
		if (sources.length != 2) {
			throw new InteractiveSpacesException(String.format(
					"Illegal message bridge source %s", lines[0]));
		}

		String[] destinations = lines[1].split(":");
		if (destinations.length != 2) {
			throw new InteractiveSpacesException(String.format(
					"Illegal message bridge destination %s", lines[1]));
		}

		// TODO(keith): Check these
		String sourceTopicName = sources[0].trim();
		String sourceMessageType = sources[1].trim();
		String destinationTopicName = destinations[0].trim();
		String destinationMessageType = destinations[1].trim();

		List<String> expressions = Lists.newArrayList();
		for (int i = 2; i < lines.length; i++) {
			// Figure out how to compile the expressions. Classloaders in
			// OGNL and Javassist are screwing me up and no time to figure out
			// now.
			if (!lines[i].trim().isEmpty()) {
				expressions.add(lines[i]);
			}
		}

		return new OgnlRosMessageBridgeSpecification<SourceType, DestinationType>(
				sourceTopicName, sourceMessageType, destinationTopicName,
				destinationMessageType, expressions);
	}
}
