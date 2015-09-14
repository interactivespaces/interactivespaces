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

package interactivespaces.master.api.master;

import interactivespaces.master.server.services.MasterSupportManager;

import java.util.Map;

/**
 * Master API access for the {@link MasterSupportManager}.
 *
 * @author Keith M. Hughes
 */
public interface MasterApiMasterSupportManager {

  /**
   * The name of the master domain description file.
   */
  String MASTER_DOMAIN_FILE = "master-domain.xml";

  /**
   * Export the master domain model.
   *
   * @return the Master API response
   */
  Map<String, Object> exportMasterDomainModel();

  /**
   * Import the master domain model.
   *
   * @param model
   *          the model to be imported
   *
   * @return the Master API response
   */
  Map<String, Object> importMasterDomainModel(String model);

  /**
   * Export the master domain description from the file system.
   *
   * <p>
   * The exported model file will be in the root folder of the master in the file named {@link #MASTER_DOMAIN_FILE}.
   *
   * @return the Master API response
   */
  Map<String, Object> exportToFileSystemMasterDomainModel();

  /**
   * Import the master domain description from the file system.
   *
   * <p>
   * The imported model file should be in the root folder of the master in the file named {@link #MASTER_DOMAIN_FILE}.
   *
   * @return the Master API response
   */
  Map<String, Object> importFromFileSystemMasterDomainModel();

  /**
   * Get the Interactive Spaces Version.
   *
   * @return the Master API response
   */
  Map<String, Object> getInteractiveSpacesVersion();
}
