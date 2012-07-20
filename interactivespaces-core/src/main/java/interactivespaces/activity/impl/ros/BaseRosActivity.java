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

package interactivespaces.activity.impl.ros;

import interactivespaces.activity.component.ros.RosActivityComponent;
import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.activity.ros.RosActivity;

import org.ros.node.Node;
import org.ros.osgi.common.RosEnvironment;

/**
 * Support for ROS Interactive Spaces activities.
 * 
 * @author Keith M. Hughes
 */
public abstract class BaseRosActivity extends BaseActivity
		implements RosActivity {

	/**
	 * The ROS activity component.
	 */
	private RosActivityComponent rosActivityComponent;

	@Override
	public void commonActivitySetup() {
		super.commonActivitySetup();

		rosActivityComponent = addActivityComponent(new RosActivityComponent());
	}

	@Override
	public RosEnvironment getRosEnvironment() {
		return rosActivityComponent.getRosEnvironment();
	}

	@Override
	public Node getMainNode() {
		return rosActivityComponent.getNode();
	}
}
