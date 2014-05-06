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

package interactivespaces.service;

import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.Collections;
import java.util.Map;

/**
 * A support class for {@link SupportedService} subclasses.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseSupportedService implements SupportedService {

  /**
   * The space environment for this service.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  @Override
  public Map<String, Object> getMetadata() {
    return Collections.emptyMap();
  }

  @Override
  public ServiceDescription getServiceDescription() {
    return new MyServiceDescription();
  }

  @Override
  public void startup() {
    // Default is do nothing
  }

  @Override
  public void shutdown() {
    // Default is do nothing
  }

  @Override
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  /**
   * Get the space environment.
   *
   * @return the space environment
   */
  protected InteractiveSpacesEnvironment getSpaceEnvironment() {
    return spaceEnvironment;
  }

  /**
   * A service description that delegates to the enclosing object.
   *
   * @author Keith M. Hughes
   */
  private class MyServiceDescription implements ServiceDescription {
    @Override
    public String getName() {
      return BaseSupportedService.this.getName();
    }

    @Override
    public Map<String, Object> getMetadata() {
      return BaseSupportedService.this.getMetadata();
    }

    @Override
    public String toString() {
      return "MyServiceDescription [name=" + getName() + ", metadata=" + getMetadata() + "]";
    }
  }
}
