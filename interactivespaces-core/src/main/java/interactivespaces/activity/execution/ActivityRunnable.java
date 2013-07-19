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

/**
 * A {@link Runnable} which handles activity method invocation properly.
 *
 * @author Keith M. Hughes
 */
public class ActivityRunnable implements Runnable {

  /**
   * The execution context for the runnable call.
   */
  private ActivityExecutionContext executionContext;

  /**
   * The target runnable.
   */
  private Runnable runnable;

  public ActivityRunnable(ActivityExecutionContext executionContext, Runnable runnable) {
    this.executionContext = executionContext;
    this.runnable = runnable;
  }

  @Override
  public void run() {
    ActivityMethodInvocation invocation = executionContext.enterMethod();

    try {
      runnable.run();
    } finally {
      executionContext.exitMethod(invocation);
    }
  }
}
