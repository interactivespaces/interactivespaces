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

package interactivespaces.master.api.master.internal;

import interactivespaces.master.api.master.MasterApiMasterSupportManager;
import interactivespaces.master.api.messages.MasterApiMessageSupport;
import interactivespaces.master.api.messages.MasterApiMessages;
import interactivespaces.master.server.services.MasterSupportManager;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import java.io.File;
import java.util.Map;

/**
 * A basic implementation of the {@link MasterApiMasterSupportManager}.
 *
 * @author Keith M. Hughes
 */
public class BasicMasterApiMasterSupportManager extends BaseMasterApiManager implements MasterApiMasterSupportManager {

  /**
   * The name of the master domain description file.
   */
  public static final String MASTER_DOMAIN_FILE = "master-domain.xml";

  /**
   * The master support manager.
   */
  private MasterSupportManager masterSupportManager;

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public Map<String, Object> getMasterDomainDescription() {
    try {
      String description = masterSupportManager.getMasterDomainDescription();

      fileSupport.writeFile(new File(MASTER_DOMAIN_FILE), description);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } catch (Throwable e) {
      spaceEnvironment.getLog().error("Error while writing master domain model", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE, e);
    }
  }

  @Override
  public Map<String, Object> importMasterDomainDescription() {
    try {
      String description = fileSupport.readFile(new File(MASTER_DOMAIN_FILE));

      masterSupportManager.importMasterDomainDescription(description);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } catch (Throwable e) {
      spaceEnvironment.getLog().error("Error while importing master domain model", e);

      return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_FAILURE, e);
    }
  }

  /**
   * @param masterSupportManager
   *          the masterSupportManager to set
   */
  public void setMasterSupportManager(MasterSupportManager masterSupportManager) {
    this.masterSupportManager = masterSupportManager;
  }
}
