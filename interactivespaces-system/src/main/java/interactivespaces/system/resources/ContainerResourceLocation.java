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

package interactivespaces.system.resources;

/**
 * Where in the container the resource should go.
 *
 * @author Keith M. Hughes
 */
public enum ContainerResourceLocation {

  /**
   * To the bootstrap folder for Core.
   */
  SYSTEM_BOOTSTRAP(true),

  /**
   * The bootstrap folder for user dependencies. This is as distinct from the
   * Core Interactive Spaces dependencies.
   */
  USER_BOOTSTRAP(true),

  /**
   * To lib/system.
   */
  LIB_SYSTEM(false),

  /**
   * To the config folder.
   */
  CONFIG(false),

  /**
   * To the root folder of the container.
   */
  ROOT(false);

  /**
   * {@code true} if the location is an OSGi bundle which will need to be loaded.
   */
  private final boolean immediateLoad;

  /**
   * Construct a container location.
   *
   * @param immediateLoad
   *          {@code true} if this should be immediately loaded
   */
  ContainerResourceLocation(boolean immediateLoad) {
    this.immediateLoad = immediateLoad;
  }

  /**
   * Does this resource location needs immediate load?
   *
   * @return {@code true} if needs an immediate load
   */
  public boolean isImmediateLoad() {
    return immediateLoad;
  }
}
