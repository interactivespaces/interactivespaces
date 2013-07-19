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

package interactivespaces.domain.basic.pojo;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityDependency;

/**
 * A POJO implementation of a {@link ActivityDependency}.
 *
 * @author Keith M. Hughes
 */
public class SimpleActivityDependency implements ActivityDependency {

  /**
   * The activity which has the dependency.
   */
  private Activity activity;

  /**
   * The name of the dependency.
   */
  private String name;

  /**
   * The minimum version necessary for the activity.
   */
  private String minimumVersion;

  /**
   * The maximum version necessary for the activity.
   */
  private String maximumVersion;

  /**
   * Is the dependency required?
   *
   * <p>
   * {@code true} if the dependency is required
   */
  private boolean required;

  @Override
  public Activity getActivity() {
    return activity;
  }

  @Override
  public void setActivity(Activity activity) {
    this.activity = activity;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getMinimumVersion() {
    return minimumVersion;
  }

  @Override
  public void setMinimumVersion(String minimumVersion) {
    this.minimumVersion = minimumVersion;
  }

  @Override
  public String getMaximumVersion() {
    return maximumVersion;
  }

  @Override
  public void setMaximumVersion(String maximumVersion) {
    this.maximumVersion = maximumVersion;
  }

  @Override
  public boolean isRequired() {
    return required;
  }

  @Override
  public void setRequired(boolean required) {
    this.required = required;
  }

}
