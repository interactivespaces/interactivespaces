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

package interactivespaces.activity.component;

import com.google.common.collect.ImmutableList;

import interactivespaces.activity.SupportedActivity;
import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.activity.execution.ActivityMethodInvocation;
import interactivespaces.configuration.Configuration;

import java.util.List;

/**
 * A support class for implementations of {@link ActivityComponent}.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseActivityComponent implements ActivityComponent {

  /**
   * A list of no dependencies.
   */
  private static final List<String> NO_DEPENDENCIES = ImmutableList.of();

  /**
   * Component context this component is running under.
   */
  protected ActivityComponentContext componentContext;

  @Override
  public List<String> getDependencies() {
    // Default is no dependencies
    return NO_DEPENDENCIES;
  }

  @Override
  public ActivityComponentContext getComponentContext() {
    return componentContext;
  }

  @Override
  public String getComponentStatusDetail() {
    return null;
  }

  @Override
  public String getDescription() {
    return getName();
  }

  @Override
  public void setComponentContext(ActivityComponentContext componentContext) {
    this.componentContext = componentContext;
  }

  @Override
  public void configureComponent(Configuration configuration) {
    // Default is to do nothing.
  }

  /**
   * Handle an error for this component. Includes basic logging, and then passing off to the
   * activity for any activity-specific processing.
   *
   * @param message
   *          error message text
   * @param t
   *          triggering throwable or {@code null}
   */
  public void handleError(String message, Throwable t) {
    String compositeMessage = String.format("Component %s: %s", getName(), message);

    SupportedActivity activity = getComponentContext().getActivity();
    activity.getLog().error(compositeMessage, t);

    ActivityExecutionContext executionContext = activity.getExecutionContext();
    ActivityMethodInvocation invocation = executionContext.enterMethod();
    try {
      activity.onActivityComponentError(this, compositeMessage, t);
    } finally {
      executionContext.exitMethod(invocation);
    }
  }
}
