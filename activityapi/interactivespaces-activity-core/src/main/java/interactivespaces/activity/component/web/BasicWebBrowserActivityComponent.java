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

package interactivespaces.activity.component.web;

import interactivespaces.activity.binary.BasicNativeWebBrowserRunner;
import interactivespaces.activity.binary.NativeWebBrowserRunner;
import interactivespaces.activity.component.ActivityComponent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * An {@link ActivityComponent} which starts up a web browser.
 *
 * @author Keith M. Hughes
 */
public class BasicWebBrowserActivityComponent extends BaseWebBrowserActivityComponent {

  /**
   * Dependencies for the component.
   *
   * TODO(keith): Should this actually depend on there being a web server? What will break if the dependency is removed?
   */
  public static final List<String> BASE_COMPONENT_DEPENDENCIES = ImmutableList
      .of(WebServerActivityComponent.COMPONENT_NAME);

  /**
   * The browser runner.
   */
  private NativeWebBrowserRunner webBrowserRunner;

  @Override
  public String getName() {
    return COMPONENT_NAME;
  }

  @Override
  public List<String> getBaseDependencies() {
    return BASE_COMPONENT_DEPENDENCIES;
  }

  @Override
  public void startupComponent() {
    if (browserStartup) {
      webBrowserRunner = newWebBrowserRunner();
      webBrowserRunner.startup(initialUrl, browserDebug);
    }
  }

  @Override
  public void shutdownComponent() {
    if (webBrowserRunner != null) {
      webBrowserRunner.shutdown();
      webBrowserRunner = null;
    }
  }

  @Override
  public boolean isComponentRunning() {
    // Either the web browser is running, or it was requested that the browser not actually start up.
    return (webBrowserRunner != null && webBrowserRunner.isRunning()) || (!browserStartup);
  }

  /**
   * Get the web browser runner being used.
   *
   * @return the web browser runner being used
   */
  public NativeWebBrowserRunner getWebBrowserRunner() {
    return webBrowserRunner;
  }

  /**
   * Create a new web browser runner.
   *
   * @return a new web browser runner
   */
  @VisibleForTesting
  protected BasicNativeWebBrowserRunner newWebBrowserRunner() {
    return new BasicNativeWebBrowserRunner(getComponentContext().getActivity());
  }
}
