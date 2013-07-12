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

package interactivespaces.service.script;

import java.util.Map;

/**
 * A script which can be evaluated.
 *
 * @author Keith M. Hughes
 */
public interface Script {

  /**
   * Evaluate the script with the given bindings.
   *
   * @param bindings
   *          map of bindings for the script
   *
   * @return the value, if any, of the script
   */
  Object eval(Map<String, Object> bindings);
}
