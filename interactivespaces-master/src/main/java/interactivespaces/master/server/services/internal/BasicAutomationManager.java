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

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.system.NamedScript;
import interactivespaces.master.api.MasterApiActivityManager;
import interactivespaces.master.api.MasterApiSpaceControllerManager;
import interactivespaces.master.api.MasterApiMasterSupportManager;
import interactivespaces.master.server.services.ActiveSpaceControllerManager;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.AutomationManager;
import interactivespaces.master.server.services.SpaceControllerRepository;
import interactivespaces.master.server.services.ScriptingNames;
import interactivespaces.service.scheduler.SchedulerService;
import interactivespaces.service.script.ScriptService;
import interactivespaces.service.script.StringScriptSource;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.directorywatcher.BaseDirectoryWatcherListener;
import interactivespaces.util.io.directorywatcher.DirectoryWatcher;
import interactivespaces.util.io.directorywatcher.SimpleDirectoryWatcher;

import com.google.common.collect.Maps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A basic implementation of the {@link AutomationManager}.
 *
 * @author Keith M. Hughes
 */
public class BasicAutomationManager implements AutomationManager {

  /**
   * How often the watched directories should be scanned. In seconds.
   */
  public static final int DIRECTORY_SCAN_TIME = 10;

  /**
   * Watched subfolder for importing new activities.
   */
  public static final String ACTIVITY_IMPORT_DIRECTORY = "master/activity/import";

  /**
   * Watched subfolder for importing and deploying new activities.
   */
  public static final String ACTIVITY_DEPLOY_DIRECTORY = "master/activity/deploy";

  /**
   * The script service to use for the automation master.
   */
  private ScriptService scriptService;

  /**
   * The scheduling service to use for the automation master.
   */
  private SchedulerService schedulerService;

  /**
   * The controller repository to use for the automation master.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * The activity repository to use for the automation master.
   */
  private ActivityRepository activityRepository;

  /**
   * The activity controller manager to use for the automation master.
   */
  private ActiveSpaceControllerManager activeSpaceControllerManager;

  /**
   * The Master API activity manager to use for the automation master.
   */
  private MasterApiActivityManager masterApiActivityManager;

  /**
   * The Master API controller manager to use for the automation master.
   */
  private MasterApiSpaceControllerManager masterApiSpaceControllerManager;

  /**
   * The Master API master support manager to use for the automation master.
   */
  private MasterApiMasterSupportManager masterApiMasterSupportManager;

  /**
   * Interactive Spaces environment being run in.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * A directory watcher for imports.
   */
  private DirectoryWatcher importDirectoryWatcher;

  /**
   * The map of bindings that every automation invocation will receive.
   */
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
      scriptService.executeScriptByName(script.getLanguage(), new StringScriptSource(script.getContent()),
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
    importDirectoryWatcher.addDirectory(new File(spaceEnvironment.getFilesystem().getInstallDirectory(),
        ACTIVITY_IMPORT_DIRECTORY));
    importDirectoryWatcher.addDirectory(new File(spaceEnvironment.getFilesystem().getInstallDirectory(),
        ACTIVITY_DEPLOY_DIRECTORY));
    importDirectoryWatcher.addDirectoryWatcherListener(new BaseDirectoryWatcherListener() {
      @Override
      public void onFileAdded(File file) {
        handleImportActivityFileAdded(file);
      }
    });
    importDirectoryWatcher.startup(spaceEnvironment, DIRECTORY_SCAN_TIME, TimeUnit.SECONDS);
  }

  /**
   * Prepare the bindings for automation.
   */
  private void prepareAutomationBindings() {
    automationBindings = Maps.newHashMap();
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_ACTIVITY_REPOSITORY, activityRepository);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_SPACE_CONTROLLER_REPOSITORY, spaceControllerRepository);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_SCRIPT_SERVICE, scriptService);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_SCHEDULER_SERVICE, schedulerService);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_ACTIVE_SPACE_CONTROLLER_MANAGER, activeSpaceControllerManager);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_MASTER_API_ACTIVITY_MANAGER, masterApiActivityManager);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_MASTER_API_SPACE_CONTROLLER_MANAGER, masterApiSpaceControllerManager);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_MASTER_API_MASTER_SUPPORT_MANAGER, masterApiMasterSupportManager);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_SPACE_ENVIRONMENT, spaceEnvironment);
    automationBindings.put(ScriptingNames.SCRIPTING_NAME_AUTOMATION_MANAGER, this);
  }

  /**
   * An activity file has been added to the scanned folders.
   *
   * @param file
   *          the folder which has been added
   */
  private void handleImportActivityFileAdded(File file) {
    spaceEnvironment.getLog().info(String.format("Activity file  %s found in autoinput folders", file));

    FileInputStream activityStream = null;
    try {
      activityStream = new FileInputStream(file);
      Activity activity = masterApiActivityManager.saveActivity(null, activityStream);

      String watchedFolder = file.getParent();
      if (watchedFolder.endsWith(ACTIVITY_DEPLOY_DIRECTORY)) {
        masterApiSpaceControllerManager.deployAllActivityInstances(activity.getId());
      }
    } catch (Exception e) {
      spaceEnvironment.getLog().error(String.format("Could not read imported activity file %s", file), e);
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
   *          the scriptService to set
   */
  public void setScriptService(ScriptService scriptService) {
    this.scriptService = scriptService;
  }

  /**
   * @param schedulerService
   *          the schedulerService to set
   */
  public void setSchedulerService(SchedulerService schedulerService) {
    this.schedulerService = schedulerService;
  }

  /**
   * @param controllerRepository
   *          the controllerRepository to set
   */
  public void setSpaceControllerRepository(SpaceControllerRepository controllerRepository) {
    this.spaceControllerRepository = controllerRepository;
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * @param activeControllerManager
   *          the activeControllerManager to set
   */
  public void setActiveSpaceControllerManager(ActiveSpaceControllerManager activeControllerManager) {
    this.activeSpaceControllerManager = activeControllerManager;
  }

  /**
   * @param masterApiActivityManager
   *          the uiActivityManager to set
   */
  public void setMasterApiActivityManager(MasterApiActivityManager masterApiActivityManager) {
    this.masterApiActivityManager = masterApiActivityManager;
  }

  /**
   * @param masterApiControllerManager
   *          the uiControllerManager to set
   */
  public void setMasterApiSpaceControllerManager(MasterApiSpaceControllerManager masterApiControllerManager) {
    this.masterApiSpaceControllerManager = masterApiControllerManager;
  }

  /**
   * @param masterApiMasterSupportManager
   *          the uiMasterSupportManager to set
   */
  public void setMasterApiMasterSupportManager(MasterApiMasterSupportManager masterApiMasterSupportManager) {
    this.masterApiMasterSupportManager = masterApiMasterSupportManager;
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
