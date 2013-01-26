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

package interactivespaces.activity.example.android.simple;

import interactivespaces.activity.impl.BaseActivity;

import java.util.Map;

import android.util.Log;

/**
 * A sample Interactive Spaces Android-based activity.
 * 
 * @author Keith M. Hughes
 */
public class SimpleAndroidExampleActivity extends BaseActivity {

	@Override
	public void onActivitySetup() {
		getLog().info("Simple Android Example Activity setup");
		Log.i("foo", "bar");
	}

	@Override
	public void onActivityStartup() {
		getLog().info("Simple Android Example Activity startup");
	}

	@Override
	public void onActivityActivate() {
		getLog().info("Simple Android Example Activity activate");
	}

	@Override
	public void onActivityDeactivate() {
		getLog().info("Simple Android Example Activity deactivate");
	}

	@Override
	public void onActivityShutdown() {
		getLog().info("Simple Android Example Activity shutdown");
	}

	@Override
	public void onActivityCleanup() {
		getLog().info("Simple Android Example Activity cleanup");
	}

	@Override
	public void onActivityFailure() {
		getLog().error("Simple Android Example Activity failure");
	}
}
