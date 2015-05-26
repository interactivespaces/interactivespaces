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

import interactivespaces.expression.ExpressionConstants;

import java.util.List;
import java.util.Map;

/**
 * The standard parser for native application runners.
 *
 * @author Keith M. Hughes
 */
public class StandardNativeApplicationRunnerParser implements NativeApplicationRunnerParser {

  @Override
  public void parseCommandArguments(List<String> commandFlagsList, String commandFlags) {
    if (commandFlags == null) {
      return;
    }

    // Now collect the individual arguments. The escape character will always
    // pass through the following character as part of the current token.
    StringBuilder component = new StringBuilder();
    for (int i = 0; i <= commandFlags.length(); i++) {
      // Force a space on the end to keep the end of a term processing from being duplicated.
      char ch = (i == commandFlags.length()) ? ' ' : commandFlags.charAt(i);
      if (Character.isWhitespace(ch)) {
        if (component.length() != 0) {
          commandFlagsList.add(component.toString());
          component.setLength(0);
        }
      } else if (ch == ExpressionConstants.ESCAPE_CHARACTER) {
        i++;
        if (i < commandFlags.length()) {
          component.append(commandFlags.charAt(i));
        }
      } else {
        component.append(ch);
      }
    }
  }

  /**
   * Extract environment variables.
   *
   * @param environment
   *          the environment map to add into
   * @param variables
   *          the string containing the environment variables
   */
  public void parseEnvironment(Map<String, String> environment, String variables) {
    if (variables == null) {
      return;
    }

    // Now collect the individual arguments. The escape character will always
    // pass through the following character as part of the current token.
    String variableName = null;
    StringBuilder component = new StringBuilder();
    for (int i = 0; i <= variables.length(); i++) {
      // Force a space on the end to keep the end of a term processing from being duplicated.
      char ch = (i == variables.length()) ? ' ' : variables.charAt(i);
      if (Character.isWhitespace(ch)) {
        if (component.length() != 0) {
          if (variableName != null) {
            environment.put(variableName, component.toString());
            variableName = null;
          } else {
            // No variable name so must have a variable name and a null value.
            environment.put(component.toString(), null);
          }
          component.setLength(0);
        }
      } else if (ch == ExpressionConstants.ESCAPE_CHARACTER) {
        i++;
        if (i < variables.length()) {
          component.append(variables.charAt(i));
        }
      } else if (ch == ExpressionConstants.EQUALS_CHARACTER && variableName == null) {
        variableName = component.toString();
        component.setLength(0);
      } else {
        component.append(ch);
      }
    }
  }

}
