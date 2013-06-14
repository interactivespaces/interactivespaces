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

package interactivespaces.service.androidos.impl;

import com.google.common.collect.Maps;

import android.content.Context;
import interactivespaces.service.androidos.AndroidOsService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.Map;

/**
 * A simple implementation of {@link AndroidOsService}
 *
 * @author Keith M. Hughes
 */
public class SimpleAndroidOsService implements AndroidOsService {

  /**
   * The Android context the service was started under.
   */
  private Context context;

  /**
   * Interactive Spaces environment for the service.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The metadata for the service.
   */
  private Map<String, Object> metadata = Maps.newHashMap();

  public SimpleAndroidOsService(Context context) {
    this.context = context;
  }

  @Override
  public Context getAndroidContext() {
    return context;
  }

  @Override
  public Object getSystemService(String name) {
    return context.getSystemService(name);
  }

  @Override
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  @Override
  public String getName() {
    return AndroidOsService.SERVICE_NAME;
  }

  @Override
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
