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

package interactivespaces.activity.impl;

/**
 * Constant set for working with status details.
 *
 * @author Trevor Pering
 */
public interface StatusDetail {

  /**
   * Format string for a header of a collection.
   */
  String HEADER_FORMAT = "<table class=\"%s\">";

  /**
   * Format string for a header of a sub-element.
   */
  String PREFIX_FORMAT = "<tr class=\"%s\"><td>";

  /**
   * Separator between sub-elements.
   */
  String SEPARATOR = "</td><td>";

  /**
   * Chunk at the end of a sub-element.
   */
  String POSTFIX = "</td></tr>\n";

  /**
   * Footer for the end of a collection.
   */
  String FOOTER = "</table>";

  /**
   * Indicate a break for sub-details.
   */
  String BREAK = "<br/>";

  /**
   * Format string used for constructing the status detail.
   */
  String LINK_FORMAT = "<a href=\"%s\">%1$s</a>";

  /**
   * Left arrow character.
   */
  String ARROW_LEFT = "&#8592;";

  /**
   * Right arrow character.
   */
  String ARROW_RIGHT = "&#8594;";

  /**
   * An "item is" designator.
   */
  String ITEM_IS = ":";
}
