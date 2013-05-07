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

import interactivespaces.domain.support.AutomationUtils;
import interactivespaces.domain.system.NamedScript;
import interactivespaces.domain.system.pojo.SimpleNamedScript;
import interactivespaces.master.server.services.AutomationManager;
import interactivespaces.master.server.services.AutomationRepository;
import interactivespaces.master.server.services.EntityNotFoundInteractiveSpacesException;
import interactivespaces.master.server.ui.UiAutomationManager;
import interactivespaces.service.scheduler.SchedulerService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.Set;

/**
 * A basic implementation of the {@link UiAutomationManager}.
 * 
 * @author Keith M. Hughes
 */
public class BasicUiAutomationManager implements UiAutomationManager {

	/**
	 * The automation repository.
	 */
	private AutomationRepository automationRepository;

	/**
	 * The automation manager.
	 */
	private AutomationManager automationManager;

	/**
	 * The space environment.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	@Override
	public Set<String> getScriptingLanguages() {
		return automationManager.getScriptingLanguages();
	}

	@Override
	public NamedScript saveNamedScript(SimpleNamedScript script) {
		NamedScript finalScript = automationRepository.newNamedScript(script);

		finalScript = automationRepository.saveNamedScript(finalScript);

		potentiallyScheduleScript(finalScript);

		return finalScript;
	}

	@Override
	public NamedScript updateNamedScript(String id, SimpleNamedScript template) {
		NamedScript script = automationRepository.getNamedScriptById(id);
		if (script != null) {
			AutomationUtils.copy(template, script);
			script = automationRepository.saveNamedScript(script);

			potentiallyScheduleScript(script);

			return script;
		} else {
			throw new EntityNotFoundInteractiveSpacesException(String.format(
					"Named script with id %s does not exist", id));
		}
	}

	/**
	 * Start a script if it is scheduled
	 * 
	 * @param script
	 *            the script to schedule
	 */
	private void potentiallyScheduleScript(NamedScript script) {
		if (script.getScheduled()) {
			SchedulerService schedulerService = spaceEnvironment
					.getServiceRegistry().getService(
							SchedulerService.SERVICE_NAME);
			if (schedulerService != null) {
				spaceEnvironment.getLog()
						.info(String.format("Scheduling script %s",
								script.getName()));

				String schedule = script.getSchedule();
				if (schedule.startsWith("once:")) {
					// schedulerService.schedule("foo", "bar", "goober",
					// schedule)
				} else if (schedule.startsWith("repeat:")) {
					schedulerService.scheduleScriptWithCron("foo", "bar",
							"goober", "0 " + schedule.substring(7));
				} else {
					spaceEnvironment.getLog().error(
							String.format(
									"Script %s has an illegal schedule: %s",
									script.getName(), schedule));
				}
			} else {
				spaceEnvironment
						.getLog()
						.warn(String
								.format("No scheduling service for scheduling script %s",
										script.getName()));
			}
		}
	}

	@Override
	public UiAutomationManager deleteNamedScript(String id) {
		automationRepository.deleteNamedScript(getNamedScript(id));

		return this;
	}

	@Override
	public UiAutomationManager runScript(String id) {
		spaceEnvironment.getLog().info(
				String.format("Running script with id %s", id));

		automationManager.runScript(getNamedScript(id));

		return this;
	}

	/**
	 * Attempt to get a named script by ID.
	 * 
	 * <p>
	 * Will throw an exception if no such script.
	 * 
	 * @param id
	 *            ID of the script
	 * @return
	 */
	private NamedScript getNamedScript(String id) {
		NamedScript script = automationRepository.getNamedScriptById(id);
		if (script != null) {
			return script;
		} else {
			spaceEnvironment.getLog().error(
					String.format("Unknown named script %s", id));

			throw new EntityNotFoundInteractiveSpacesException(String.format(
					"Named script with ID %s not found", id));
		}
	}

	/**
	 * @param automationRepository
	 *            the automationRepository to set
	 */
	public void setAutomationRepository(AutomationRepository scriptRepository) {
		this.automationRepository = scriptRepository;
	}

	/**
	 * @param automationManager
	 *            the automationManager to set
	 */
	public void setAutomationManager(AutomationManager automationManager) {
		this.automationManager = automationManager;
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}
}
