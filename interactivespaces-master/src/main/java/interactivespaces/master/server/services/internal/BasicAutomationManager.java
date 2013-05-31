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

package interactivespaces.master.server.services.internal;

import interactivespaces.domain.system.NamedScript;
import interactivespaces.master.server.services.ActiveControllerManager;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.AutomationManager;
import interactivespaces.master.server.services.ControllerRepository;
import interactivespaces.master.server.services.ScriptingNames;
import interactivespaces.master.server.ui.UiActivityManager;
import interactivespaces.master.server.ui.UiControllerManager;
import interactivespaces.master.server.ui.UiMasterSupportManager;
import interactivespaces.service.scheduler.SchedulerService;
import interactivespaces.service.script.ScriptService;
import interactivespaces.service.script.StringScriptSource;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.directorywatcher.BaseDirectoryWatcherListener;
import interactivespaces.util.io.directorywatcher.DirectoryWatcher;
import interactivespaces.util.io.directorywatcher.SimpleDirectoryWatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;

/**
 * A basic implementation of the {@link AutomationManager}
 * 
 * @author Keith M. Hughes
 */
public class BasicAutomationManager implements AutomationManager {

	public static final String ACTIVITY_IMPORT_DIRECTORY = "master/import/activity";

	/**
	 * The script service to use for the automation master
	 */
	private ScriptService scriptService;

	/**
	 * The scheduling service to use for the automation master.
	 */
	private SchedulerService schedulerService;

	/**
	 * The controller repository to use for the automation master.
	 */
	private ControllerRepository controllerRepository;

	/**
	 * The activity repository to use for the automation master.
	 */
	private ActivityRepository activityRepository;

	/**
	 * The activity controller manager to use for the automation master.
	 */
	private ActiveControllerManager activeControllerManager;

	/**
	 * The ui activity manager to use for the automation master.
	 */
	private UiActivityManager uiActivityManager;

	/**
	 * The ui controller manager to use for the automation master.
	 */
	private UiControllerManager uiControllerManager;

	/**
	 * The ui master support manager to use for the automation master.
	 */
	private UiMasterSupportManager uiMasterSupportManager;

	/**
	 * Interactive Spaces environment being run in.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * A directory watcher for imports.
	 */
	private DirectoryWatcher importDirectoryWatcher;

	private Map<String, Object> automationBindings;

	@Override
	public void startup() {
		prepareImportDirectoryWatcher();
		prepareAutomationBindings();
		schedulerService.addSchedulingEntities(automationBindings);
	}

	@Override
	public void shutdown() {
		if (importDirectoryWatcher != null) {
			importDirectoryWatcher.shutdown();
			importDirectoryWatcher = null;
		}
	}

	@Override
	public Set<String> getScriptingLanguages() {
		return scriptService.getLanguageNames();
	}

	@Override
	public Map<String, Object> getAutomationBindings() {
		return automationBindings;
	}

	@Override
	public void runScript(NamedScript script) {

		try {
			scriptService.executeScriptByName(script.getLanguage(),
					new StringScriptSource(script.getContent()),
					automationBindings);
		} catch (Exception e) {
			spaceEnvironment.getLog().error("Error while running script", e);
		}
	}

	/**
	 * Prepare the directory watcher for automatic import of activities.
	 */
	private void prepareImportDirectoryWatcher() {
		importDirectoryWatcher = new SimpleDirectoryWatcher();
		importDirectoryWatcher.addDirectory(new File(spaceEnvironment
				.getFilesystem().getInstallDirectory(),
				ACTIVITY_IMPORT_DIRECTORY));
		importDirectoryWatcher
				.addDirectoryWatcherListener(new BaseDirectoryWatcherListener() {
					@Override
					public void onFileAdded(File file) {
						onImportActivityFileAdded(file);
					}
				});
		importDirectoryWatcher.startup(spaceEnvironment, 10, TimeUnit.SECONDS);
	}

	/**
	 * Prepare the bindings for automation.
	 */
	private void prepareAutomationBindings() {
		automationBindings = Maps.newHashMap();
		automationBindings.put(
				ScriptingNames.SCRIPTING_NAME_ACTIVITY_REPOSITORY,
				activityRepository);
		automationBindings.put(
				ScriptingNames.SCRIPTING_NAME_CONTROLLER_REPOSITORY,
				controllerRepository);
		automationBindings.put(ScriptingNames.SCRIPTING_NAME_SCRIPT_SERVICE,
				scriptService);
		automationBindings.put(ScriptingNames.SCRIPTING_NAME_SCHEDULER_SERVICE,
				schedulerService);
		automationBindings.put(
				ScriptingNames.SCRIPTING_NAME_ACTIVE_CONTROLLER_MANAGER,
				activeControllerManager);
		automationBindings.put(
				ScriptingNames.SCRIPTING_NAME_UI_ACTIVITY_MANAGER,
				uiActivityManager);
		automationBindings.put(
				ScriptingNames.SCRIPTING_NAME_UI_CONTROLLER_MANAGER,
				uiControllerManager);
		automationBindings.put(
				ScriptingNames.SCRIPTING_NAME_UI_MASTER_SUPPORT_MANAGER,
				uiMasterSupportManager);
		automationBindings.put(ScriptingNames.SCRIPTING_NAME_SPACE_ENVIRONMENT,
				spaceEnvironment);
		automationBindings.put(
				ScriptingNames.SCRIPTING_NAME_AUTOMATION_MANAGER, this);
	}

	/**
	 * An activity file has been added to the scanned folders.
	 * 
	 * @param file
	 *            the folder which has been added
	 */
	private void onImportActivityFileAdded(File file) {
		spaceEnvironment.getLog().info(
				String.format("Activity file  %s found in autoinput folder",
						file));

		FileInputStream activityStream = null;
		try {
			activityStream = new FileInputStream(file);
			uiActivityManager.saveActivity(null, activityStream);
		} catch (Exception e) {
			spaceEnvironment.getLog().error(
					String.format("Could not read imported activity file %s",
							file), e);
		} finally {
			if (activityStream != null) {
				try {
					activityStream.close();
				} catch (IOException e) {
					// Don't care
				}
			}

			file.delete();
		}
	}

	/**
	 * @param scriptService
	 *            the scriptService to set
	 */
	public void setScriptService(ScriptService scriptService) {
		this.scriptService = scriptService;
	}

	/**
	 * @param schedulerService
	 *            the schedulerService to set
	 */
	public void setSchedulerService(SchedulerService schedulerService) {
		this.schedulerService = schedulerService;
	}

	/**
	 * @param controllerRepository
	 *            the controllerRepository to set
	 */
	public void setControllerRepository(
			ControllerRepository controllerRepository) {
		this.controllerRepository = controllerRepository;
	}

	/**
	 * @param activityRepository
	 *            the activityRepository to set
	 */
	public void setActivityRepository(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	/**
	 * @param activeControllerManager
	 *            the activeControllerManager to set
	 */
	public void setActiveControllerManager(
			ActiveControllerManager activeControllerManager) {
		this.activeControllerManager = activeControllerManager;
	}

	/**
	 * @param uiActivityManager
	 *            the uiActivityManager to set
	 */
	public void setUiActivityManager(UiActivityManager uiActivityManager) {
		this.uiActivityManager = uiActivityManager;
	}

	/**
	 * @param uiControllerManager
	 *            the uiControllerManager to set
	 */
	public void setUiControllerManager(UiControllerManager uiControllerManager) {
		this.uiControllerManager = uiControllerManager;
	}

	/**
	 * @param uiMasterSupportManager
	 *            the uiMasterSupportManager to set
	 */
	public void setUiMasterSupportManager(
			UiMasterSupportManager uiMasterSupportManager) {
		this.uiMasterSupportManager = uiMasterSupportManager;
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
