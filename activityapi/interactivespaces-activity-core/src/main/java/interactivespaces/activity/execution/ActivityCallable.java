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
 * A {@link Callable} which handles activity method invocation properly.
 *
 * @param <V>
 *          the return value of the callable
 *
 * @author Keith M. Hughes
 */
public class ActivityCallable<V> implements Callable<V> {

  /**
   * The execution context for the callable call.
   */
  private ActivityExecutionContext executionContext;

  /**
   * The target callable.
   */
  private Callable<V> callable;

  /**
   * Construct a callable.
   *
   * @param executionContext
   *          the execution context to do the runnable in
   * @param callable
   *          the callable to be run
   */
  public ActivityCallable(ActivityExecutionContext executionContext, Callable<V> callable) {
    this.executionContext = executionContext;
    this.callable = callable;
  }

  @Override
  public V call() throws Exception {
    ActivityMethodInvocation invocation = executionContext.enterMethod();

    try {
      return callable.call();
    } finally {
      executionContext.exitMethod(invocation);
    }
  }
}
