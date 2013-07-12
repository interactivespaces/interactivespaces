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

import interactivespaces.activity.execution.ActivityMethodInvocation;

/**
 * An {@link ActivityMethodInvocation} for scripts.
 *
 * @author Keith M. Hughes
 */
public class ScriptActivityMethodInvocation implements ActivityMethodInvocation {

  /**
   * The classloader to use during invocation.
   */
  private ClassLoader newLoader;

  /**
   * The previous classloader.
   */
  private ClassLoader oldLoader;

  public ScriptActivityMethodInvocation(ClassLoader newLoader) {
    this.newLoader = newLoader;
  }

  @Override
  public void methodEnter() {
    oldLoader = Thread.currentThread().getContextClassLoader();

    Thread.currentThread().setContextClassLoader(newLoader);
  }

  @Override
  public void methodExit() {
    Thread.currentThread().setContextClassLoader(oldLoader);
  }
}
