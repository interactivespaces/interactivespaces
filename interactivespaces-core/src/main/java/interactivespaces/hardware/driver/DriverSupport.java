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

package interactivespaces.hardware.driver;

import interactivespaces.configuration.Configuration;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

/**
 * A class which gives driver support by implementing some methods.
 *
 * @author Keith M. Hughes
 */
public class DriverSupport implements Driver {

  /**
   * The space environment the driver is running under.
   */
  protected InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The configuration for the driver.
   */
  protected Configuration configuration;

  /**
   * The log the driver is using.
   */
  protected Log log;

  @Override
  public void startup() {
    // Default is do nothing
  }

  @Override
  public void shutdown() {
    // Default is do nothing
  }

  @Override
  public void prepare(InteractiveSpacesEnvironment spacesEnvironment, Configuration configuration,
      Log log) {
    this.spaceEnvironment = spacesEnvironment;
    this.log = log;
  }
}
