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

package interactivespaces.master.server.ui.internal;

import java.io.File;

import interactivespaces.master.server.services.MasterSupportManager;
import interactivespaces.master.server.ui.UiMasterSupportManager;
import interactivespaces.util.io.Files;

/**
 * A basic implementation of the {@link UiMasterSupportManager}
 * 
 * @author Keith M. Hughes
 */
public class BasicUiMasterSupportManager implements UiMasterSupportManager {

	/**
	 * The name of the master domain description file.
	 */
	public static final String MASTER_DOMAIN_FILE = "master-domain.xml";
	
	/**
	 * The master support manager.
	 */
	private MasterSupportManager masterSupportManager;

	@Override
	public String getMasterDomainDescription() {
		String description = masterSupportManager.getMasterDomainDescription();

		Files.writeFile(new File(MASTER_DOMAIN_FILE), description);

		return description;
	}

	@Override
	public void importMasterDomainDescription() {

		String description = Files.readFile(new File(MASTER_DOMAIN_FILE));

		masterSupportManager.importMasterDomainDescription(description);
	}

	/**
	 * @param masterSupportManager
	 *            the masterSupportManager to set
	 */
	public void setMasterSupportManager(
			MasterSupportManager masterSupportManager) {
		this.masterSupportManager = masterSupportManager;
	}
}
