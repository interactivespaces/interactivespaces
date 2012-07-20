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

package interactivespaces.master.ui.internal.web.admin;

import interactivespaces.domain.system.NamedScript;
import interactivespaces.domain.system.pojo.SimpleNamedScript;
import interactivespaces.master.server.ui.UiAutomationManager;
import interactivespaces.master.ui.internal.web.WebSupport;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

/**
 * The webflow action for automation operations.
 * 
 * @author Keith M. Hughes
 */
public class AutomationAction {

	/**
	 * Repository for automation entities.
	 */
	private UiAutomationManager uiAutomationManager;

	/**
	 * Get a new script model.
	 * 
	 * @return a blank script moel
	 */
	public SimpleNamedScript newNamedScript() {
		return new SimpleNamedScript();
	}

	/**
	 * Save the new script.
	 * 
	 * @param script
	 *            template for the new script
	 */
	public void saveNamedScript(SimpleNamedScript script) {
		NamedScript finalScript = uiAutomationManager.saveNamedScript(script);

		// So the ID gets copied out of the flow.
		script.setId(finalScript.getId());
	}

	/**
	 * Add entities to the flow context needed by the new entity page.
	 * 
	 * @param context
	 *            The Webflow context.
	 */
	public void addNamedScriptEntities(RequestContext context) {
		MutableAttributeMap viewScope = context.getViewScope();
		viewScope.put("languages",
				WebSupport.getScriptingLanguageSelections(uiAutomationManager
						.getScriptingLanguages()));
	}

	/**
	 * @param uiAutomationManager the uiAutomationManager to set
	 */
	public void setUiAutomationManager(UiAutomationManager uiAutomationManager) {
		this.uiAutomationManager = uiAutomationManager;
	}
}
