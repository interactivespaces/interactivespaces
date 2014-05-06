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

package interactivespaces.activity.example.common;

import interactivespaces.activity.impl.BaseActivity;
import java.util.Map;

/**
 * Very simple example common class for an activity that just logs things.
 * This is primarily intended as an example of of a common file can be
 * directly included in multiple activities.
 *
 * @author Keith M. Hughes
 */
public abstract class CommonActivity extends BaseActivity {

  @Override
  public void onActivitySetup() {
    getLog().info(getName() + " Activity setup");
  }

  @Override
  public void onActivityStartup() {
    getLog().info(getName() + " Activity startup");
  }

  @Override
  public void onActivityPostStartup() {
    getLog().info(getName() + " Activity post startup");
  }

  @Override
  public void onActivityActivate() {
    getLog().info(getName() + " Activity activate");
  }

  @Override
  public void onActivityDeactivate() {
    getLog().info(getName() + " Activity deactivate");
  }

  @Override
  public void onActivityPreShutdown() {
    getLog().info(getName() + " Activity pre shutdown");
  }

  @Override
  public void onActivityShutdown() {
    getLog().info(getName() + " Activity shutdown");
  }

  @Override
  public void onActivityCleanup() {
    getLog().info(getName() + " Activity cleanup");
  }

  @Override
  public void onActivityFailure() {
    getLog().error(getName() + " Activity failure");
  }

  @Override
  public boolean onActivityCheckState() {
    getLog().info(getName() + " Activity checking state");

    return true;
  }

  @Override
  public void onActivityConfiguration(Map<String, Object> update) {
    getLog().info(String.format(getName() + " Activity config update %s", update));
  }
}
