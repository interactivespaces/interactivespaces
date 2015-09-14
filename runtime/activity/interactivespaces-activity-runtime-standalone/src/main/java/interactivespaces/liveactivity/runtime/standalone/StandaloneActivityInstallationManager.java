/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime.standalone;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.liveactivity.runtime.installation.ActivityInstallationListener;
import interactivespaces.liveactivity.runtime.installation.ActivityInstallationManager;
import interactivespaces.resource.Version;

import java.util.Date;

/**
 * A standalone activity installation manager. It mostly throws exepctions if it is called.
 *
 * @author Keith M. Hughes
 */
public class StandaloneActivityInstallationManager implements ActivityInstallationManager {

  @Override
  public void startup() {
    // Nothing to do.
  }

  @Override
  public void shutdown() {
    // Nothing to do.
  }

  @Override
  public void copyActivity(String uuid, String uri) {
    SimpleInteractiveSpacesException.throwFormattedException("Cannot install activities");
  }

  @Override
  public Date installActivity(String uuid, String activityIdentifyingName, Version version) {
    SimpleInteractiveSpacesException.throwFormattedException("Cannot install activities");

    // here for compilation reasons.
    return null;
  }

  @Override
  public void removePackedActivity(String uuid) {
    SimpleInteractiveSpacesException.throwFormattedException("Cannot install activities");
  }

  @Override
  public RemoveActivityResult removeActivity(String uuid) {
    SimpleInteractiveSpacesException.throwFormattedException("Cannot install activities");

    // here for compilation reasons.
    return null;
  }

  @Override
  public void addActivityInstallationListener(ActivityInstallationListener listener) {
    // Nothing to do.
  }

  @Override
  public void removeActivityInstallationListener(ActivityInstallationListener listener) {
    // Nothing to do.
  }
}
