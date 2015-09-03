/*
 * Copyright (C) 2015 Google Inc.
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

import java.io.Serializable;

/**
 * The form for simple cloning operations.
 *
 * @author Keith M. Hughes
 */

public class SimpleCloneForm implements Serializable {

  /**
   *  The serialization UUID.
   */
  private static final long serialVersionUID = -8673809296282297486L;
  /**
   * The name prefix for the clone.
   */
  private String namePrefix;

  /**
   * Get the name prefix for the clone.
   *
   * @return the name prefix
   */
  public String getNamePrefix() {
    return namePrefix;
  }

  /**
   * Set the name prefix for the clone.
   *
   * @param namePrefix
   *          the name prefix
   */
  public void setNamePrefix(String namePrefix) {
    this.namePrefix = namePrefix;
  }
}
