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

package interactivespaces.system.core.logging;

import org.apache.commons.logging.Log;

/**
 * The platform logging provider.
 *
 * @author Keith M. Hughes
 */
public interface LoggingProvider {

  /**
   * Base name attached to loggers.
   */
  public static final String LOGGER_BASE_NAME = "interactivespaces";

  /**
   * The log level for warnings and above.
   */
  public static final String LOG_LEVEL_WARN = "warn";

  /**
   * The log level for trace and above.
   */
  public static final String LOG_LEVEL_TRACE = "trace";

  /**
   * The log level for no logging.
   */
  public static final String LOG_LEVEL_OFF = "off";

  /**
   * The log level for info and above.
   */
  public static final String LOG_LEVEL_INFO = "info";

  /**
   * The log level for debug and above.
   */
  public static final String LOG_LEVEL_DEBUG = "debug";

  /**
   * The log level for fatal.
   */
  public static final String LOG_LEVEL_FATAL = "fatal";

  /**
   * The log level for error and above.
   */
  public static final String LOG_LEVEL_ERROR = "error";

  /**
   * Get the container log.
   *
   * @return
   */
  Log getLog();

  /**
   * Get a named log.
   *
   * @param logName
   * @return
   */
  Log getLog(String logName, String level);

  /**
   * Modify the log level.
   *
   * <p>
   * This method will only work if the level is legal and the log is modifiable.
   *
   * @param log
   *          the log to modify
   * @param level
   *          the new level
   *
   * @return {@code true} if able to modify the log.
   */
  boolean modifyLogLevel(Log log, String level);
}
