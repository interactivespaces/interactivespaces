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

package interactivespaces;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * An Interactive Spaces exception happened.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesException extends RuntimeException {

  /**
   * Get the stack trace from an exception as a string.
   *
   * @param t
   *          the exception
   *
   * @return the stack trace
   */
  public static String getStackTrace(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    return sw.toString();
  }

  public InteractiveSpacesException(String message) {
    super(message);
  }

  public InteractiveSpacesException(String message, Throwable cause) {
    super(message, cause);
  }
}
