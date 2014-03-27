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

package interactivespaces.master.api.internal.osgi;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.api.MasterApiActivityManager;
import interactivespaces.master.api.MasterApiSpaceControllerManager;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.SpaceControllerRepository;
import interactivespaces.service.script.FileScriptSource;
import interactivespaces.service.script.ScriptService;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;

import com.google.common.collect.Maps;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.osgi.framework.BundleContext;

import java.io.Console;
import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * A shell for performing some Interactive Spaces Space Master commands from a
 * command line.
 *
 * @author Keith M. Hughes
 */
public class OsgiMasterShell {

  /**
   * Master API manager for operations on activities.
   */
  private MasterApiActivityManager masterApiActivityManager;

  /**
   * Master API manager for operations on controllers.
   */
  private MasterApiSpaceControllerManager masterApiSpaceControllerManager;

  /**
   * Repository for activities.
   */
  private ActivityRepository activityRepository;

  /**
   * Repository for controllers.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * The script engine to use.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The script engine to use.
   */
  private ScriptService scriptService;

  /**
   * Bundle context for hooking into the shell system.
   */
  private BundleContext bundleContext;

  /**
   * Control of the Interactive Spaces system.
   */
  private InteractiveSpacesSystemControl spaceSystemControl;

  /**
   * Activate the shell.
   */
  public void activate() {
    Dictionary<String, Object> dict = new Hashtable<String, Object>();
    dict.put(CommandProcessor.COMMAND_SCOPE, "interactivespaces");
    dict.put(CommandProcessor.COMMAND_FUNCTION, new String[] { "listActivities", "addActivity", "listControllers",
        "addController", "controllerShutdownallapps", "listLiveActivities", "addLiveActivity", "deployLiveActivity",
        "startupLiveActivity", "activateLiveActivity", "deactivateLiveActivity", "shutdownLiveActivity",
        "deleteLiveActivity", "listGroups", "addGroup", "deployGroup", "startupGroup", "activateGroup",
        "deactivateGroup", "shutdownGroup", "deleteGroup", "script", "shutdown" });
    bundleContext.registerService(getClass().getName(), this, dict);
  }

  /**
   * Deactivate the shell.
   */
  public void deactivate() {
  }

  /**
   * A shell command to shut down Interactive Spaces.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void shutdown(CommandSession session, String[] args) {
    System.out.println("Shutting down");
    spaceSystemControl.shutdown();
  }

  /**
   * A shell command to list all activities.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void listActivities(CommandSession session, String[] args) {
    // databaseConnection.getTransactionRunner().run(new Runnable() {
    // @Override
    // public void run() {
    List<Activity> apps = activityRepository.getAllActivities();

    System.out.format("Number of activities: %d\n", apps.size());

    for (Activity app : apps) {
      System.out.format("%s\n\t%s\t%s\n", app.getName(), app.getId(), app.getIdentifyingName());
    }
    // }
    // });
  }

  /**
   * A shell command to add an activity.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void addActivity(CommandSession session, String[] args) {
    final Console console = System.console();

    if (console != null) {
      // TODO(keith): This should be given a URL, which could include a
      // file URI
      Activity app = activityRepository.newActivity();

      String name = console.readLine("Name: ");
      app.setName(name);

      String identifyingName = console.readLine("Identifying name: ");
      app.setIdentifyingName(identifyingName);

      String description = console.readLine("Description: ");
      if (!description.trim().isEmpty()) {
        app.setDescription(description);
      }

      activityRepository.saveActivity(app);
    }
  }

  /**
   * A shell command to list all controllers.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void listControllers(CommandSession session, String[] args) {
    List<SpaceController> controllers = spaceControllerRepository.getAllSpaceControllers();

    System.out.format("Number of controllers: %d\n", controllers.size());

    for (SpaceController controller : controllers) {
      System.out.format("%s\n\tID: %s\tUUID: %s\n", controller.getName(), controller.getId(), controller.getUuid());
      System.out.format("\tHostId: %s\n", controller.getHostId());
    }
  }

  /**
   * A shell command to shut down all activities on a controller.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void controllerShutdownallapps(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("controllerShutdownallapps id1 id2 id3...");
    }

    for (String id : args) {
      System.out.format("Shutting down all apps on controller %s\n", id);
      masterApiSpaceControllerManager.shutdownAllActivities(id);
    }
  }

  /**
   * A shell command to list all installed activities.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void listLiveAictivites(CommandSession session, String[] args) {
    List<LiveActivity> liveAictivites = activityRepository.getAllLiveActivities();

    System.out.format("Number of installed activities: %d\n", liveAictivites.size());

    for (LiveActivity liveActivity : liveAictivites) {
      System.out.format("%s\n\t%s\t%s\n", liveActivity.getName(), liveActivity.getId(), liveActivity.getUuid());
      System.out.format("\tActivity: ID: %s\tName: %s\n", liveActivity.getActivity().getId(), liveActivity
          .getActivity().getName());
      System.out.format("\tController: ID: %s\tName: %s\n", liveActivity.getController().getId(), liveActivity
          .getController().getName());
    }
  }

  /**
   * A shell command to add an activity.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void addLiveActivity(CommandSession session, String[] args) {
    final Console console = System.console();

    if (console != null) {
      String appId = console.readLine("Activity ID: ");
      Activity app = activityRepository.getActivityById(appId);
      if (app == null) {
        console.printf("Could not find app with id %s\n", appId);
        return;
      }

      String controllerId = console.readLine("Controller ID: ");
      SpaceController controller = spaceControllerRepository.getSpaceControllerById(controllerId);
      if (controller == null) {
        console.printf("Could not find controller with id %s\n", controllerId);
        return;
      }

      LiveActivity liveActivity = activityRepository.newLiveActivity();
      liveActivity.setController(controller);
      liveActivity.setActivity(app);

      String name = console.readLine("Name: ");
      liveActivity.setName(name);

      String description = console.readLine("Description: ");
      if (!description.trim().isEmpty()) {
        liveActivity.setDescription(description);
      }

      activityRepository.saveLiveActivity(liveActivity);
    }
  }

  /**
   * A shell command to deploy an activity.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void deployLiveActivity(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("deployLiveActivity id1 id2 id3...");
    }

    for (String id : args) {
      System.out.format("Deploying activity %s\n", id);
      masterApiSpaceControllerManager.deployLiveActivity(id);
    }
  }

  /**
   * A shell command to start up an activity.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void startupLiveActivity(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("startupLiveActivity id1 id2 id3...");
    }

    for (String id : args) {
      System.out.format("Starting up activity %s\n", id);
      masterApiSpaceControllerManager.startupLiveActivity(id);
    }
  }

  /**
   * A shell command to activate an activity.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void activateLiveActivity(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("activateLiveActivity id1 id2 id3...");
    }

    for (String id : args) {
      System.out.format("Activating activity %s\n", id);
      masterApiSpaceControllerManager.activateLiveActivity(id);
    }
  }

  /**
   * A shell command to deactivate an activity.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void deactivateLiveActivity(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("deactivateLiveActivity id1 id2 id3...");
    }

    for (String id : args) {
      System.out.format("Deactivating activity %s\n", id);
      masterApiSpaceControllerManager.deactivateLiveActivity(id);
    }
  }

  /**
   * A shell command to shutdown an activity.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void shutdownLiveActivity(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("shutdownLiveActivity id1 id2 id3...");
    }

    for (String id : args) {
      System.out.format("Shutting down activity %s\n", id);
      masterApiSpaceControllerManager.shutdownLiveActivity(id);
    }
  }

  /**
   * A shell command to delete an activity.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void deleteLiveActivity(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("deleteLiveActivity id1 id2 id3...");
    }

    for (String id : args) {
      System.out.format("Deleting activity %s\n", id);
      masterApiActivityManager.deleteLiveActivity(id);
    }
  }

  /**
   * A shell command to list all installed activity groups.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void listGroup(CommandSession session, String[] args) {
    List<LiveActivityGroup> groups = activityRepository.getAllLiveActivityGroups();

    System.out.format("Number of installed activity groups: %d\n", groups.size());

    for (LiveActivityGroup group : groups) {
      System.out.format("%s\n\t%s\n", group.getName(), group.getId());
    }
  }

  /**
   * A shell command to add an activity.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void addGroup(CommandSession session, String[] args) {
    System.out.println("Not implemented yet");
  }

  /**
   * A shell command to deploy an activity group.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void deployGroup(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("deployGroup id1 id2 id3...");
    }

    for (String id : args) {
      System.out.format("Deploying activity group %s\n", id);
      masterApiSpaceControllerManager.deployLiveActivityGroup(id);
    }
  }

  /**
   * A shell command to start up an activity.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void startupGroup(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("startupGroup id1 id2 id3...");
    }

    for (String id : args) {
      System.out.format("Starting up activity group %s\n", id);
      masterApiSpaceControllerManager.startupLiveActivityGroup(id);
    }
  }

  /**
   * A shell command to activate an activity group.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void activateGroup(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("activateGroup id1 id2 id3...");
    }

    for (String id : args) {
      System.out.format("Activating activity group %s\n", id);
      masterApiSpaceControllerManager.activateLiveActivityGroup(id);
    }
  }

  /**
   * A shell command to deactivate an activity group.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void deactivateGroup(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("deactivateGroup id1 id2 id3...");
    }

    for (String id : args) {
      System.out.format("Deactivating live activity group %s\n", id);
      masterApiSpaceControllerManager.deactivateLiveActivityGroup(id);
    }
  }

  /**
   * A shell command to shutdown an activity group.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void shutdownGroup(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("shutdownGroup id1 id2 id3...");
    }

    for (String id : args) {
      System.out.format("Shutting down activity group %s\n", id);
      masterApiSpaceControllerManager.shutdownLiveActivityGroup(id);
    }
  }

  /**
   * A shell command to delete an activity group.
   *
   * @param session
   *          the command session
   * @param args
   *          the args for the command
   */
  public void deleteGroup(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("deleteGroup id1 id2 id3...");
    }

    for (String id : args) {
      System.out.format("Deleting activity group %s\n", id);
      masterApiActivityManager.deleteLiveActivityGroup(id);
    }
  }

  /**
   * Run a script.
   *
   * @param session
   *          the command session
   * @param args
   *          the arguments for the script command
   */
  public void script(CommandSession session, final String[] args) {
    if (args.length < 1) {
      System.out.println("script script1 script2 script3... where each script is a filename");
    }

    Map<String, Object> bindings = Maps.newHashMap();
    bindings.put("controllerManager", masterApiSpaceControllerManager);

    try {
      for (int i = 0; i < args.length; i++) {
        File scriptFile = new File(spaceEnvironment.getFilesystem().getInstallDirectory(), args[i]);
        if (scriptFile.exists()) {
          if (scriptFile.canRead()) {
            String name = scriptFile.getName();
            String extension = name.substring(name.lastIndexOf('.') + 1);
            scriptService.executeScriptByExtension(extension, new FileScriptSource(scriptFile), bindings);
          } else {
            spaceEnvironment.getLog().error(
                String.format("Script file %s is not readable", scriptFile.getAbsolutePath()));
          }

        } else {
          spaceEnvironment.getLog().error(String.format("Script file %s does not exist", scriptFile.getAbsolutePath()));
        }
      }
    } catch (Exception ex) {
      spaceEnvironment.getLog().error("Error while running script", ex);
    }
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
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  /**
   * @param spaceControllerRepository
   *          the controllerRepository to set
   */
  public void setSpaceControllerRepository(SpaceControllerRepository spaceControllerRepository) {
    this.spaceControllerRepository = spaceControllerRepository;
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  /**
   * @param scriptService
   *          the scriptService to set
   */
  public void setScriptService(ScriptService scriptService) {
    this.scriptService = scriptService;
  }

  /**
   * @param spaceSystemControl
   *          the spaceSystemControl to set
   */
  public void setSpaceSystemControl(InteractiveSpacesSystemControl spaceSystemControl) {
    this.spaceSystemControl = spaceSystemControl;
  }

  /**
   * @param bundleContext
   *          the bundleContext to set
   */
  public void setBundleContext(BundleContext bundleContext) {
    this.bundleContext = bundleContext;
  }
}
