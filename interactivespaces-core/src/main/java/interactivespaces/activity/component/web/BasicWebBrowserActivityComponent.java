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

import interactivespaces.activity.binary.NativeBrowserRunner;
import interactivespaces.activity.component.ActivityComponent;

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
   */
  public static final List<String> BASE_COMPONENT_DEPENDENCIES = ImmutableList.of(WebServerActivityComponent.COMPONENT_NAME);

  /**
   * The browser runner.
   */
  private NativeBrowserRunner browserRunner;

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
      browserRunner = new NativeBrowserRunner(getComponentContext().getActivity());
      browserRunner.startup(initialUrl, browserDebug);
    }
  }

  @Override
  public void shutdownComponent() {
    if (browserRunner != null) {
      browserRunner.shutdown();
      browserRunner = null;
    }
  }

  @Override
  public boolean isComponentRunning() {
    // TODO(keith): Anything to check on the browser?
    return browserRunner != null && browserRunner.isRunning();
  }
}
