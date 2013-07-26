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

package interactivespaces.master.server.services;

/**
 * Manager for supporting master operations.
 *
 * @author Keith M. Hughes
 */
public interface MasterSupportManager {

  /**
   * Start the manager up.
   */
  void startup();

  /**
   * Shut the manager down.
   */
  void shutdown();

  /**
   * Get a description of the entire master domain.
   *
   * @return the entire domain description
   */
  String getMasterDomainDescription();

  /**
   * Import a master domain description.
   *
   * @param description
   *          the description to import.
   */
  void importMasterDomainDescription(String description);
}
