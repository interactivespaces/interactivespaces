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

package interactivespaces.activity.example.hello;

import interactivespaces.activity.impl.BaseActivity;

import java.util.Map;

/**
 * A very simple Interactive Spaces Java-based activity.
 * It merely logs its lifecycle messages in the logs.
 *
 * @author Keith M. Hughes
 */
public class HelloActivity extends BaseActivity {

	@Override
	public void onActivitySetup() {
		getLog().info("Hello Activity setup");
	}

	@Override
	public void onActivityStartup() {
		getLog().info("Hello Activity startup");
	}

	@Override
	public void onActivityPostStartup() {
		getLog().info("Hello Activity post startup");
	}

	@Override
	public void onActivityActivate() {
		getLog().info("Hello Activity activate");
	}

	@Override
	public void onActivityDeactivate() {
		getLog().info("Hello Activity deactivate");
	}

	@Override
	public void onActivityPreShutdown() {
		getLog().info("Hello Activity pre shutdown");
	}

	@Override
	public void onActivityShutdown() {
		getLog().info("Hello Activity shutdown");
	}

	@Override
	public void onActivityCleanup() {
		getLog().info("Hello Activity cleanup");
	}

	@Override
	public void onActivityFailure() {
		getLog().error("Hello Activity failure");
	}

	@Override
	public boolean onActivityCheckState() {
		getLog().info("Hello Activity checking state");

		return true;
	}

	@Override
	public void onActivityConfigurationUpdate(Map<String, Object> update) {
		getLog().info(String.format("Hello Activity config update %s", update));
        }
}
