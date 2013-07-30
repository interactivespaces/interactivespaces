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

package interactivespaces.service.androidos;

import interactivespaces.service.Service;

import android.content.Context;

/**
 * Access to Android OS services.
 *
 * @author Keith M. Hughes
 */
public interface AndroidOsService extends Service {

  /**
   * The name of the service.
   */
  public static final String SERVICE_NAME = "os.android";

  /**
   * Get the Android context that Interactive Spaces is running under.
   *
   * @return the Android context that Interactive Spaces is running under
   */
  Context getAndroidContext();

  /**
   * Get a named Android system service.
   *
   * @param name
   *          name of the service
   *
   * @return the service or {@code null} if it doesn't exist
   */
  Object getSystemService(String name);
}