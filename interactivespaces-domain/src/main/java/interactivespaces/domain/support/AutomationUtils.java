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

import interactivespaces.domain.system.NamedScript;
import interactivespaces.domain.system.pojo.SimpleNamedScript;

/**
 * A collection of useful utilities for working with automation entities.
 *
 * @author Keith M. Hughes
 */
public class AutomationUtils {

  /**
   * Private constructor for utility class.
   */
  private AutomationUtils() {
  }

  /**
   * Copy the source to a POJO template.
   *
   * <p>
   * The copy includes all fields.
   *
   * @param source
   *          the source script object
   *
   * @return a newly created copy of the source data
   */
  public static SimpleNamedScript toTemplate(NamedScript source) {
    SimpleNamedScript template = new SimpleNamedScript();

    copy(source, template);

    return template;
  }

  /**
   * Copy the script information from the source to the destination.
   *
   * <p>
   * Everything is copied except the ID.
   *
   * @param source
   *          the source script object
   * @param destination
   *          the destination script object
   */
  public static void copy(NamedScript source, NamedScript destination) {
    destination.setName(source.getName());
    destination.setDescription(source.getDescription());
    destination.setLanguage(source.getLanguage());
    destination.setContent(source.getContent());
    destination.setSchedule(source.getSchedule());
    destination.setScheduled(source.getScheduled());
  }
}
