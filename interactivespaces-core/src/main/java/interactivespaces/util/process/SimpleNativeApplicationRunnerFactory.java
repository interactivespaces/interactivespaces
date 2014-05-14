/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.util.process;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.SystemConfiguration;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

/**
 * A factory for receiving native application launchers.
 *
 * @author Keith M. Hughes
 */
public class SimpleNativeApplicationRunnerFactory implements NativeApplicationRunnerFactory {

  /**
   * The Interactive Spaces environment being run under.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Construct a new factory.
   *
   * @param spaceEnvironment
   *          the space environment to use
   */
  public SimpleNativeApplicationRunnerFactory(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public NativeApplicationRunner newPlatformNativeApplicationRunner(Log log) {
    String os = spaceEnvironment.getSystemConfiguration().getRequiredPropertyString(SystemConfiguration.PLATFORM_OS);

    if (LinuxNativeApplicationRunner.OPERATING_SYSTEM_TAG.equals(os)) {
      return new LinuxNativeApplicationRunner(spaceEnvironment, log);
    } else if (OsxNativeApplicationRunner.OPERATING_SYSTEM_TAG.equals(os)) {
      return new OsxNativeApplicationRunner(spaceEnvironment, log);
    } else if (WindowsNativeApplicationRunner.OPERATING_SYSTEM_TAG.equals(os)) {
      return new WindowsNativeApplicationRunner(spaceEnvironment, log);
    } else {
      throw new SimpleInteractiveSpacesException("Cannot create native activity launcher. Unknown OS " + os);
    }
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
