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

package interactivespaces.master.ui.internal.web;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * useful utilities for the user interface.
 *
 * @author Keith M. Hughes
 */
public class UiUtilities {

  /**
   * The option used when nothing should be selected in a multiple selection
   * box.
   */
  public static final String MULTIPLE_SELECT_NONE = "--none--";

  /**
   * Take the given map and turn it into a sorted set of labeled values.
   *
   * <p>
   * The map key will be the label and the value will be the value.
   *
   * @param metadata
   *          the data to be rendered
   *
   * @return a sorted list of labeled values.
   */
  public static List<LabeledValue> getMetadataView(Map<String, Object> metadata) {
    List<LabeledValue> values = Lists.newArrayList();

    for (Entry<String, Object> entry : metadata.entrySet()) {
      values.add(new LabeledValue(entry.getKey(), entry.getValue().toString()));
    }

    Collections.sort(values);

    return values;
  }
}
