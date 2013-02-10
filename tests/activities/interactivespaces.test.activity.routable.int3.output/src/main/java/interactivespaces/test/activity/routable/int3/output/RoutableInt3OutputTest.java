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

package interactivespaces.test.activity.routable.int3.output;

import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;
import interactivespaces.util.concurrency.CancellableLoop;

import java.util.Map;
import java.util.concurrent.Future;

import com.google.common.collect.Maps;

/**
 * An Interactive Spaces test which sends 3 ints on a route.
 *
 * The ints will have 1 added to them each time.
 *
 * @author Keith M. Hughes
 */
public class RoutableInt3OutputTest extends BaseRoutableRosActivity {
	
	/**
	 * First number to be sent.
	 */
	private int num1;
	
	/**
	 * Second number to be sent.
	 */
	private int num2;
	
	/**
	 * Third number to be sent.
	 */
	private int num3;

	/**
	 * The loop sending the ints.
	 */
	private CancellableLoop loop;

	@Override
	public void onActivityActivate() {
		final Map<String, Object> message = Maps.newHashMap();
		
		num1 = 0;
		num2 = 1;
		num3 = 2;
		
		loop = new CancellableLoop() {
			
			@Override
			protected void loop() throws InterruptedException {
				message.put("num1", num1++);
				message.put("num2", num2++);
				message.put("num3", num3++);
				
				sendOutputJson("output1", message);
			}
		};
		
		getSpaceEnvironment().getExecutorService().submit(loop);
	}

	@Override
	public void onActivityDeactivate() {
		loop.cancel();
	}
}
