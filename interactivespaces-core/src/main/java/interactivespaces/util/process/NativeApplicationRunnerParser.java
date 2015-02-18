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

package interactivespaces.util.process;

import java.util.List;
import java.util.Map;

/**
 * The parser for native activity runner values.
 *
 * @author Keith M. Hughes
 */
public interface NativeApplicationRunnerParser {

  /**
   * Extract command line flags from a string.
   *
   * @param commandFlagsList
   *          the list to place the command flags in
   * @param commandFlags
   *          the string containing the flags
   */
  void parseCommandArguments(List<String> commandFlagsList, String commandFlags);

  /**
   * Extract environment variables.
   *
   * @param environment
   *          the environment map to add into
   * @param variables
   *          the string containing the environment variables
   */
  void parseEnvironment(Map<String, String> environment, String variables);
}
