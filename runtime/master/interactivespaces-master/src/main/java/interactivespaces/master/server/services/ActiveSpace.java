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

package interactivespaces.master.server.services;

import interactivespaces.domain.space.Space;

/**
 * A {@link Space} which is now active in the system.
 *
 * @author Keith M. Hughes
 */
public class ActiveSpace {

  /**
   * The space which is instantiated.
   */
  private Space space;

  public ActiveSpace(Space space) {
    this.space = space;
  }

  /**
   * Get the space this is representing.
   *
   * @return the space
   */
  public Space getSpace() {
    return space;
  }

  /**
   * @param space
   *          the space to set
   */
  public void updateSpace(Space space) {
    this.space = space;
  }

}
