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

import interactivespaces.master.api.master.MasterApiActivityManager;
import interactivespaces.master.api.master.MasterApiMasterSupportManager;
import interactivespaces.master.api.master.MasterApiSpaceControllerManager;
import interactivespaces.master.api.messages.MasterApiMessageSupport;
import interactivespaces.master.server.services.ActiveSpaceControllerManager;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.AutomationManager;
import interactivespaces.master.server.services.ExtensionManager;
import interactivespaces.master.server.services.ScriptingNames;
import interactivespaces.master.server.services.SpaceControllerRepository;
import interactivespaces.service.scheduler.SchedulerService;
import interactivespaces.service.script.FileScriptSource;
import interactivespaces.service.script.Script;
import interactivespaces.service.script.ScriptService;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.io.directorywatcher.BatchDirectoryWatcher;
import interactivespaces.util.io.directorywatcher.BatchDirectoryWatcherListener;
import interactivespaces.util.io.directorywatcher.DirectoryWatcher;
import interactivespaces.util.io.directorywatcher.DirectoryWatcherListener;
import interactivespaces.util.io.directorywatcher.SimpleBatchDirectoryWatcher;
import interactivespaces.util.io.directorywatcher.SimpleDirectoryWatcher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A basic implementation of an {@link ExtensionManager}.
 *
 * @author Keith M. Hughes
 */
public class BasicExtensionManager implements ExtensionManager {

  /**
   * How often the watched directories should be scanned. In seconds.
   */
  public static final int DIRECTORY_SCAN_TIME = 10;

  /**
   * Directory where startup extensions will be placed.
   */
  public static final String STARTUP_EXTENSIONS_DIRECTORY = "extensions/startup";

  /**
   * Directory where startup extensions will be placed.
   */
  public static final String API_EXTENSIONS_DIRECTORY = "extensions/api";

  /**
   * The script service to use for the extension master.
   */
  private ScriptService scriptService;

  /**
   * The scheduling service to use for the extension master.
   */
  private SchedulerService schedulerService;

  /**
   * The controller repository to use for the extension master.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * The activity repository to use for the extension master.
   */
  private ActivityRepository activityRepository;

  /**
   * The activity controller manager to use for the extension master.
   */
  private ActiveSpaceControllerManager activeSpaceControllerManager;

  /**
   * The Master API activity manager to use for the extension master.
   */
  private MasterApiActivityManager masterApiActivityManager;

  /**
   * The Master API controller manager to use for the extension master.
   */
  private MasterApiSpaceControllerManager masterApiSpaceControllerManager;

  /**
   * The Master API master support manager to use for the extension master.
   */
  private MasterApiMasterSupportManager masterApiMasterSupportManager;

  /**
   * The automation manager to use.
   */
  private AutomationManager automationManager;

  /**
   * Interactive Spaces environment being run in.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * A directory watcher for startup extensions.
   */
  private BatchDirectoryWatcher startupExtensionsDirectoryWatcher;

  /**
   * A directory watcher for api extensions.
   */
  private DirectoryWatcher apiExtensionsDirectoryWatcher;

  /**
   * Bindings for the scripting engines.
   */
  private Map<String, Object> bindings;

  /**
   * Mapping of API name to the script for that name.
   */
  private final Map<String, Script> apiExtensions = Maps.newConcurrentMap();

  @Override
  public void startup() {
    initializeBindings();
    initializeStartupExtensions();
    initializeApiExtensions();
  }

  @Override
  public void shutdown() {
    if (startupExtensionsDirectoryWatcher != null) {
      startupExtensionsDirectoryWatcher.shutdown();
      startupExtensionsDirectoryWatcher = null;
    }
    if (apiExtensionsDirectoryWatcher != null) {
      apiExtensionsDirectoryWatcher.shutdown();
      apiExtensionsDirectoryWatcher = null;
    }
  }

  @Override
  public boolean containsApiExtension(String extensionName) {
    return apiExtensions.containsKey(extensionName);
  }

  @Override
  public Map<String, Object> evaluateApiExtension(String extensionName, Map<String, Object> args) {
    Script script = apiExtensions.get(extensionName);
    if (script != null) {
      Map<String, Object> completeBindings = Maps.newHashMap(bindings);
      completeBindings.put("args", args);

      try {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) script.eval(completeBindings);

        return MasterApiMessageSupport.getSuccessResponse(result);
      } catch (Throwable e) {
        spaceEnvironment.getLog().error(String.format("Could not run extension %s", extensionName), e);

        return MasterApiMessageSupport.getFailureResponse(RESULT_KEY_SPACE_MASTER_EXTENSION_EXCEPTION, e);
      }
    } else {
      return MasterApiMessageSupport.getFailureResponse(RESULT_KEY_SPACE_MASTER_EXTENSION_UNKNOWN,
          String.format("The requested master extension %s is not found", extensionName));
    }
  }

  /**
   * Create the bindings object which sets the context for all scripting.
   */
  private void initializeBindings() {
    bindings = Maps.newHashMap();
    bindings.put(ScriptingNames.SCRIPTING_NAME_ACTIVITY_REPOSITORY, activityRepository);
    bindings.put(ScriptingNames.SCRIPTING_NAME_SPACE_CONTROLLER_REPOSITORY, spaceControllerRepository);
    bindings.put(ScriptingNames.SCRIPTING_NAME_SCRIPT_SERVICE, scriptService);
    bindings.put(ScriptingNames.SCRIPTING_NAME_SCHEDULER_SERVICE, schedulerService);
    bindings.put(ScriptingNames.SCRIPTING_NAME_ACTIVE_SPACE_CONTROLLER_MANAGER, activeSpaceControllerManager);
    bindings.put(ScriptingNames.SCRIPTING_NAME_MASTER_API_ACTIVITY_MANAGER, masterApiActivityManager);
    bindings.put(ScriptingNames.SCRIPTING_NAME_MASTER_API_SPACE_CONTROLLER_MANAGER, masterApiSpaceControllerManager);
    bindings.put(ScriptingNames.SCRIPTING_NAME_MASTER_API_MASTER_SUPPORT_MANAGER, masterApiMasterSupportManager);
    bindings.put(ScriptingNames.SCRIPTING_NAME_SPACE_ENVIRONMENT, spaceEnvironment);
    bindings.put(ScriptingNames.SCRIPTING_NAME_AUTOMATION_MANAGER, automationManager);
  }

  /**
   * Initialize the Startup extensions.
   */
  private void initializeStartupExtensions() {
    startupExtensionsDirectoryWatcher = new SimpleBatchDirectoryWatcher();
    startupExtensionsDirectoryWatcher.addDirectory(new File(spaceEnvironment.getFilesystem().getInstallDirectory(),
        STARTUP_EXTENSIONS_DIRECTORY));
    startupExtensionsDirectoryWatcher.addBatchDirectoryWatcherListener(new BatchDirectoryWatcherListener() {
      @Override
      public void onFilesAdded(Set<File> files) {
        onStartupExtensionFileAdded(files);
      }
    });
    Set<File> initialFiles =
        startupExtensionsDirectoryWatcher.startupWithScan(spaceEnvironment, DIRECTORY_SCAN_TIME, TimeUnit.SECONDS);
    onStartupExtensionFileAdded(initialFiles);
  }

  /**
   * File have been added to the startup extension folder.
   *
   * <p>
   * Sort them by name then run them.
   *
   * @param files
   *          the files which have been added
   */
  private void onStartupExtensionFileAdded(Set<File> files) {
    List<File> sortedFiles = Lists.newArrayList(files);
    Collections.sort(sortedFiles, new Comparator<File>() {
      @Override
      public int compare(File f1, File f2) {
        return f1.getName().compareToIgnoreCase(f2.getName());
      }
    });

    for (File file : sortedFiles) {
      spaceEnvironment.getLog().info(String.format("Running script %s", file.getName()));
      String extension = file.getName().substring(file.getName().indexOf(".") + 1);
      scriptService.executeScriptByExtension(extension, new FileScriptSource(file), bindings);
    }
  }

  /**
   * Initialize the API extensions.
   */
  private void initializeApiExtensions() {
    apiExtensionsDirectoryWatcher = new SimpleDirectoryWatcher();
    apiExtensionsDirectoryWatcher.addDirectory(new File(spaceEnvironment.getFilesystem().getInstallDirectory(),
        API_EXTENSIONS_DIRECTORY));
    apiExtensionsDirectoryWatcher.addDirectoryWatcherListener(new DirectoryWatcherListener() {
      @Override
      public void onFileAdded(File file) {
        onApiExtensionFileAdded(file);
      }

      @Override
      public void onFileRemoved(File file) {
        onApiExtensionFileRemoved(file);
      }
    });
    apiExtensionsDirectoryWatcher.startup(spaceEnvironment, DIRECTORY_SCAN_TIME, TimeUnit.SECONDS);
  }

  /**
   * An API extension is being added.
   *
   * @param file
   *          file of the extension being added
   */
  private void onApiExtensionFileAdded(File file) {
    try {
      String name = file.getName();
      int dotPos = name.indexOf(".");
      String extensionName = name.substring(0, dotPos);
      String extensionExtension = name.substring(dotPos + 1);

      Script extensionScript = scriptService.newScriptByExtension(extensionExtension, new FileScriptSource(file));

      apiExtensions.put(extensionName, extensionScript);

      spaceEnvironment.getLog().info(
          String.format("Added API extension %s with extension %s", extensionName, extensionExtension));
    } catch (Exception e) {
      spaceEnvironment.getLog().error(String.format("Could not load API extension %s", file.getAbsolutePath()), e);
    }
  }

  /**
   * An API extension is being removed.
   *
   * @param file
   *          file of the extension being removed
   */
  private void onApiExtensionFileRemoved(File file) {
    String extensionName = file.getName().substring(0, file.getName().indexOf("."));

    spaceEnvironment.getLog().info(String.format("Removing API extension %s", extensionName));

    apiExtensions.remove(extensionName);
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
   * @param spaceControllerRepository
   *          the controllerRepository to set
   */
  public void setSpaceControllerRepository(SpaceControllerRepository spaceControllerRepository) {
    this.spaceControllerRepository = spaceControllerRepository;
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
   * Set the automation manager.
   *
   * @param automationManager
   *          the automation manager to use
   */
  public void setAutomationManager(AutomationManager automationManager) {
    this.automationManager = automationManager;
  }

  /**
   * Set the space environment.
   *
   * @param spaceEnvironment
   *          the space environment to use
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

}
