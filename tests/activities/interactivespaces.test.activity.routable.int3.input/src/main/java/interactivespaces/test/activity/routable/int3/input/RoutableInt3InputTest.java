/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.test.activity.routable.int3.input;

import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;

import java.util.Map;

/**
 * An Interactive Spaces test which receives 3 ints on a route.
 * 
 * The ints will have 1 added to them each time.
 * 
 * @author Keith M. Hughes
 */
public class RoutableInt3InputTest extends BaseRoutableRosActivity {

	private static final int MESSAGE_SAMPLING_RATE = 100000;

	/**
	 * True when got first message.
	 */
	private boolean gotFirstMessage = false;

	private int num1;

	private int num2;

	private int num3;

	private long numMessages;

	@Override
	public void onNewInputJson(String channelName, Map<String, Object> message) {

		if (gotFirstMessage) {
			numMessages++;

			int tnum1 = (Integer) message.get("num1");
			int tnum2 = (Integer) message.get("num2");
			int tnum3 = (Integer) message.get("num3");

			if (tnum1 != num1 + 1) {
				getLog().error(
						String.format("num1 difference, old %d new %d", num1,
								tnum1));
			}
			if (tnum2 != num2 + 1) {
				getLog().error(
						String.format("num2 difference, old %d new %d", num2,
								tnum2));
			}
			if (tnum3 != num3 + 1) {
				getLog().error(
						String.format("num3 difference, old %d new %d", num3,
								tnum3));
			}

			num1 = tnum1;
			num2 = tnum2;
			num3 = tnum3;

			if (numMessages % MESSAGE_SAMPLING_RATE == 0) {
				getLog().info(
						String.format("Got message block %d: %d %d %d",
								numMessages / MESSAGE_SAMPLING_RATE, num1,
								num2, num3));
			}
		} else {
			num1 = (Integer) message.get("num1");
			num2 = (Integer) message.get("num2");
			num3 = (Integer) message.get("num3");

			numMessages = 0;

			gotFirstMessage = true;

			getLog().info(
					String.format("Got first message %d %d %d", num1, num2,
							num3));
		}
	}
}
