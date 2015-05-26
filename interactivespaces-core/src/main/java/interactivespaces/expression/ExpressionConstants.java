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

package interactivespaces.expression;

import java.io.File;

/**
 * Constants for expression evaluation.
 *
 * @author Trevor Pering
 */
public interface ExpressionConstants {
  /**
   * The escape character for the flags configuration for most platforms.
   */
  char NORMAL_ESCAPE_CHARACTER = '\\';

  /**
   * The escape character to use on Windows platform.
   */
  char WINDOWS_ESCAPE_CHARACTER = '^';

  /**
   * Escape character to use (platform dynamic).
   */
  char ESCAPE_CHARACTER = (File.separatorChar == '\\') ? WINDOWS_ESCAPE_CHARACTER : NORMAL_ESCAPE_CHARACTER;

  /**
   * The equals character for environment variables.
   */
  char EQUALS_CHARACTER = '=';
}
