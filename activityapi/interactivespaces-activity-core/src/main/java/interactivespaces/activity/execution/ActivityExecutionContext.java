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
 * An execution context for an activity.
 *
 * @author Keith M. Hughes
 */
public interface ActivityExecutionContext {

  /**
   * Entering a method context.
   *
   * @return the method invocation to be used by the method execution
   */
  ActivityMethodInvocation enterMethod();

  /**
   * The method is complete, cleanup.
   *
   * @param invocation
   *          the method invocation
   */
  void exitMethod(ActivityMethodInvocation invocation);

  /**
   * Get an activity runnable that will handle method invocation properly for the supplied runnable.
   *
   * @param runnable
   *          the runnable to be handled by the method invocation
   *
   * @return the activity runnable
   */
  ActivityRunnable newActivityCallable(Runnable runnable);

  /**
   * Get an activity callable that will handle method invocation properly for the supplied callable.
   *
   * @param <V>
   *          the type of the return from the callable
   * @param callable
   *          the callable
   *
   * @return activity callable
   */
  <V> ActivityCallable<V> newActivityCallable(Callable<V> callable);
}
