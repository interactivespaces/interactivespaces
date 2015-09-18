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

package interactivespaces.activity.execution;

import java.util.concurrent.Callable;

/**
 * A base implementation of a {@link ActivityExecutionContext}.
 *
 * <p>
 * Subclasses can extend this to get some basic functionality, or it can be used
 * standalone.
 *
 * @author Keith M. Hughes
 */
public class BaseActivityExecutionContext implements ActivityExecutionContext {

  @Override
  public ActivityMethodInvocation enterMethod() {
    ActivityMethodInvocation invocation = newInvocation();
    invocation.methodEnter();

    return invocation;
  }

  @Override
  public void exitMethod(ActivityMethodInvocation invocation) {
    invocation.methodExit();
  }

  @Override
  public ActivityRunnable newActivityCallable(Runnable runnable) {
    return new ActivityRunnable(this, runnable);
  }

  @Override
  public <V> ActivityCallable<V> newActivityCallable(Callable<V> callable) {
    return new ActivityCallable<V>(this, callable);
  }

  /**
   * Create a new method invocation.
   *
   * @return a new method invocation
   */
  protected ActivityMethodInvocation newInvocation() {
    return new DoNothingActivityMethodInvocation();
  }
}
