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

package interactivespaces.activity.binary;

import interactivespaces.activity.Activity;


/**
 * A runner for native browsers.
 *
 * @author Keith M. Hughes
 */
@Deprecated
public class NativeBrowserRunner extends BasicNativeWebBrowserRunner {

  /**
   * Construct a new browser runner.
   *
   * @param activity
   *          the activity the browser is running
   */
  public NativeBrowserRunner(Activity activity) {
    super(activity);
  }

  /**
   * Get the native activity runner for the browser.
   *
   * @return the native activity runner for the browser
   */
  public NativeActivityRunner getBrowserRunner() {
    return (NativeActivityRunner) getNativeWebBrowserRunner();
  }
}
