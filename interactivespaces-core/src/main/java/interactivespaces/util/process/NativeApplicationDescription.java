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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A description of a native application.
 *
 * @author Keith M. Hughes
 */
public class NativeApplicationDescription {

  /**
   * The executable path.
   */
  private String executablePath;

  /**
   * The arguments for the application.
   */
  private List<String> arguments = Lists.newArrayList();

  /**
   * The environment for the native application.
   */
  private Map<String, String> environment = Maps.newHashMap();

  /**
   * The runner parser to use.
   */
  private NativeApplicationRunnerParser runnerParser;

  /**
   * Construct a new description.
   *
   * <p>
   * Uses the standard native application parser.
   */
  public NativeApplicationDescription() {
    this(new StandardNativeApplicationRunnerParser());
  }

  /**
   * Construct a new description.
   *
   * @param runnerParser
   *          the runner parser to use
   */
  public NativeApplicationDescription(NativeApplicationRunnerParser runnerParser) {
    this.runnerParser = runnerParser;
  }

  /**
   * Get the executable path.
   *
   * @return the executable path
   */
  public String getExecutablePath() {
    return executablePath;
  }

  /**
   * Set the executable path.
   *
   * @param executablePath
   *          the executable path
   *
   * @return this description
   */
  public NativeApplicationDescription setExecutablePath(String executablePath) {
    this.executablePath = executablePath;

    return this;
  }

  /**
   * Get the arguments.
   *
   * @return the arguments
   */
  public List<String> getArguments() {
    return arguments;
  }

  /**
   * Add in a new collection of arguments.
   *
   * @param arguments
   *          the arguments to add
   *
   * @return this description
   */
  public NativeApplicationDescription addArguments(String... arguments) {
    if (arguments != null) {
      Collections.addAll(this.arguments, arguments);
    }

    return this;
  }

  /**
   * Parse a collection of arguments.
   *
   * @param arguments
   *          the arguments to add
   *
   * @return this description
   */
  public NativeApplicationDescription parseArguments(String arguments) {
    runnerParser.parseCommandArguments(this.arguments, arguments);

    return this;
  }

  /**
   * Get the environment.
   *
   * @return the environment
   */
  public Map<String, String> getEnvironment() {
    return environment;
  }

  /**
   * Add in a new environment variable.
   *
   * @param name
   *          the name of the variable
   * @param value
   *          the value of the variable
   *
   * @return this description
   */
  public NativeApplicationDescription addEnvironment(String name, String value) {
    environment.put(name, value);

    return this;
  }

  /**
   * Parse a collection of environment variables.
   *
   * @param variables
   *          the variables to add
   *
   * @return this description
   */
  public NativeApplicationDescription parseEnvironment(String variables) {
    runnerParser.parseEnvironment(environment, variables);

    return this;
  }

  @Override
  public String toString() {
    return "NativeApplicationDescription [executablePath=" + executablePath + ", arguments=" + arguments
        + ", environment=" + environment + "]";
  }
}
