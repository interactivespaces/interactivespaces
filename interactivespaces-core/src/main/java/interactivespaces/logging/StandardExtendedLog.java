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

package interactivespaces.logging;

import interactivespaces.InteractiveSpacesExceptionUtils;

import org.apache.commons.logging.Log;

/**
 * The standard extended logger.
 *
 * @author Keith M. Hughes
 */
public class StandardExtendedLog implements ExtendedLog {

  /**
   * The logging delegate.
   */
  private Log delegate;

  /**
   * Construct the new logger.
   *
   * @param delegate
   *          the logging delegate
   */
  public StandardExtendedLog(Log delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean isDebugEnabled() {
    return delegate.isDebugEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return delegate.isErrorEnabled();
  }

  @Override
  public boolean isFatalEnabled() {
    return delegate.isFatalEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return delegate.isInfoEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return delegate.isTraceEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return delegate.isWarnEnabled();
  }

  @Override
  public void trace(Object message) {
    delegate.trace(message);
  }

  @Override
  public void trace(Object message, Throwable t) {
    delegate.trace(message, t);
  }

  @Override
  public void debug(Object message) {
    delegate.debug(message);
  }

  @Override
  public void debug(Object message, Throwable t) {
    delegate.debug(message, t);
  }

  @Override
  public void info(Object message) {
    delegate.info(message);
  }

  @Override
  public void info(Object message, Throwable t) {
    delegate.info(message, t);
  }

  @Override
  public void warn(Object message) {
    delegate.warn(message);
  }

  @Override
  public void warn(Object message, Throwable t) {
    delegate.warn(message, t);
  }

  @Override
  public void error(Object message) {
    delegate.error(message);
  }

  @Override
  public void error(Object message, Throwable t) {
    delegate.error(message, t);
  }

  @Override
  public void fatal(Object message) {
    delegate.fatal(message);
  }

  @Override
  public void fatal(Object message, Throwable t) {
    delegate.fatal(message, t);
  }

  @Override
  public void formattedTrace(String message, Object... args) {
    delegate.trace(String.format(message, args));
  }

  @Override
  public void formattedTrace(Throwable throwable, String message, Object... args) {
    formattedTrace(message, args);
    delegate.trace(InteractiveSpacesExceptionUtils.getExceptionDetail(throwable));
  }

  @Override
  public void formattedDebug(String message, Object... args) {
    delegate.debug(String.format(message, args));
  }

  @Override
  public void formattedDebug(Throwable throwable, String message, Object... args) {
    formattedDebug(message, args);
    delegate.debug(InteractiveSpacesExceptionUtils.getExceptionDetail(throwable));
  }

  @Override
  public void formattedInfo(String message, Object... args) {
    delegate.info(String.format(message, args));
  }

  @Override
  public void formattedInfo(Throwable throwable, String message, Object... args) {
    formattedInfo(message, args);
    delegate.info(InteractiveSpacesExceptionUtils.getExceptionDetail(throwable));
  }

  @Override
  public void formattedWarn(String message, Object... args) {
    delegate.warn(String.format(message, args));
  }

  @Override
  public void formattedWarn(Throwable throwable, String message, Object... args) {
    formattedWarn(message, args);
    delegate.warn(InteractiveSpacesExceptionUtils.getExceptionDetail(throwable));
  }

  @Override
  public void formattedError(String message, Object... args) {
    delegate.error(String.format(message, args));
  }

  @Override
  public void formattedError(Throwable throwable, String message, Object... args) {
    formattedError(message, args);
    delegate.error(InteractiveSpacesExceptionUtils.getExceptionDetail(throwable));
  }

  @Override
  public void formattedFatal(String message, Object... args) {
    delegate.fatal(String.format(message, args));
  }

  @Override
  public void formattedFatal(Throwable throwable, String message, Object... args) {
    formattedFatal(message, args);
    delegate.fatal(InteractiveSpacesExceptionUtils.getExceptionDetail(throwable));
  }
}
