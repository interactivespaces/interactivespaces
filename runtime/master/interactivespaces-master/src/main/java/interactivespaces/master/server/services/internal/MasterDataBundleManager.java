/*
 * Copyright (C) 2013 Google Inc.
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
package interactivespaces.master.server.services.internal;

import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.util.resource.ManagedResource;

/**
 * Interface for master side capability of capture/restore of data bundles with a controller.
 */
public interface MasterDataBundleManager extends ManagedResource {

  /**
   * Trigger the capture of the controller data bundle.
   *
   * @param controller
   *          controller to capture from
   */
  void captureControllerDataBundle(ActiveSpaceController controller);

  /**
   * Trigger the restore of the controller data bundle.
   *
   * @param controller
   *          controller to restore to
   */
  void restoreControllerDataBundle(ActiveSpaceController controller);
}
