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

package interactivespaces.domain.support;

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;

/**
 * A collection of useful utilities for working with a {@link SpaceController}.
 *
 * @author Keith M. Hughes
 */
public final class SpaceControllerUtils {

  /**
   * Private constructor for utility class.
   */
  private SpaceControllerUtils() {
  }

  /**
   * Copy the source to a POJO template.
   *
   * <p>
   * The copy includes all fields.
   *
   * @param source
   *          the source live activity group object
   *
   * @return a newly created copy of the source data
   */
  public static SpaceController toTemplate(SpaceController source) {
    SimpleSpaceController template = new SimpleSpaceController();

    copy(source, template);

    template.setUuid(source.getUuid());

    return template;
  }

  /**
   * Copy the fields from the source to the destination.
   *
   * <p>
   * Everything is copied except the ID and UUID.
   *
   * @param source
   *          the source space controller object
   * @param destination
   *          the destination space controller object
   */
  public static void copy(SpaceController source, SpaceController destination) {
    destination.setName(source.getName());
    destination.setDescription(source.getDescription());
    destination.setHostId(source.getHostId());
    destination.setMode(source.getMode());
  }
}
