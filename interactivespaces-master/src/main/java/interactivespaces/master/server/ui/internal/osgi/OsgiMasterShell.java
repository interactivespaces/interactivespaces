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

package interactivespaces.master.server.ui.internal.osgi;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.ControllerRepository;
import interactivespaces.master.server.ui.UiActivityManager;
import interactivespaces.master.server.ui.UiControllerManager;
import interactivespaces.service.script.FileScriptSource;
import interactivespaces.service.script.ScriptService;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;

import java.io.Console;
import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Maps;

/**
 * A shell for performing some Interactive Spaces Space Master commands from a
 * command line.
 * 
 * @author Keith M. Hughes
 */
public class OsgiMasterShell {

	/**
	 * Manager for UI operations on activities.
	 */
	private UiActivityManager uiActivityManager;

	/**
	 * Manager for UI operations on controllers.
	 */
	private UiControllerManager uiControllerManager;

	/**
	 * Repository for activities.
	 */
	private ActivityRepository activityRepository;

	/**
	 * Repository for controllers.
	 */
	private ControllerRepository controllerRepository;

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

	public void activate() {
		Dictionary<String, Object> dict = new Hashtable<String, Object>();
		dict.put(CommandProcessor.COMMAND_SCOPE, "interactivespaces");
		dict.put(CommandProcessor.COMMAND_FUNCTION, new String[] { "list_apps",
				"list_controllers", "add_controller",
				"controller_shutdownallapps", "list_iapps", "add_iapp",
				"deploy_iapp", "startup_iapp", "activate_iapp",
				"deactivate_iapp", "shutdown_iapp", "delete_iapp",
				"list_groups", "add_group", "deploy_group", "startup_group",
				"activate_group", "deactivate_group", "shutdown_group",
				"delete_group", "script", "shutdown" });
		bundleContext.registerService(getClass().getName(), this, dict);
	}

	public void deactivate() {
	}

	/**
	 * A shell command to shut down Interactive Spaces.
	 * 
	 * @param args
	 */
	public void shutdown(CommandSession session, String[] args) {
		System.out.println("Shutting down");
		spaceSystemControl.shutdown();
	}

	/**
	 * A shell command to list all activities.
	 * 
	 * @param args
	 */
	public void list_apps(CommandSession session, String[] args) {
		// databaseConnection.getTransactionRunner().run(new Runnable() {
		// @Override
		// public void run() {
		List<Activity> apps = activityRepository.getAllActivities();

		System.out.format("Number of activities: %d\n", apps.size());

		for (Activity app : apps) {
			System.out.format("%s\n\t%s\t%s\n", app.getName(), app.getId(),
					app.getIdentifyingName());
		}
		// }
		// });
	}

	/**
	 * A shell command to add an activity.
	 * 
	 * @param args
	 */
	public void add_app(CommandSession session, String[] args) {
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
			if (!description.trim().isEmpty())
				app.setDescription(description);

			activityRepository.saveActivity(app);
			// }
			// });
		}
	}

	/**
	 * A shell command to list all controllers.
	 * 
	 * @param args
	 */
	public void list_controllers(CommandSession session, String[] args) {
		List<SpaceController> controllers = controllerRepository
				.getAllSpaceControllers();

		System.out.format("Number of controllers: %d\n", controllers.size());

		for (SpaceController controller : controllers) {
			System.out.format("%s\n\tID: %s\tUUID: %s\n", controller.getName(),
					controller.getId(), controller.getUuid());
			System.out.format("\tHostId: %s\n", controller.getHostId());
		}
	}

	/**
	 * A shell command to shut down all activities on a controller.
	 * 
	 * @param args
	 */
	public void controller_shutdownallapps(CommandSession session,
			final String[] args) {
		if (args.length < 1) {
			System.out.println("controller_shutdownallapps id1 id2 id3...");
		}

		for (String id : args) {
			System.out.format("Shutting down all apps on controller %s\n", id);
			uiControllerManager.shutdownAllActivities(id);
		}
	}

	/**
	 * A shell command to list all installed activities.
	 * 
	 * @param args
	 */
	public void list_iapps(CommandSession session, String[] args) {
		List<LiveActivity> iapps = activityRepository.getAllLiveActivities();

		System.out.format("Number of installed activities: %d\n", iapps.size());

		for (LiveActivity iapp : iapps) {
			System.out.format("%s\n\t%s\t%s\n", iapp.getName(), iapp.getId(),
					iapp.getUuid());
			System.out.format("\tActivity: ID: %s\tName: %s\n", iapp
					.getActivity().getId(), iapp.getActivity().getName());
			System.out.format("\tController: ID: %s\tName: %s\n", iapp
					.getController().getId(), iapp.getController().getName());
		}
	}

	/**
	 * A shell command to add an activity.
	 * 
	 * @param args
	 */
	public void add_iapp(CommandSession session, String[] args) {
		final Console console = System.console();

		if (console != null) {
			String appId = console.readLine("Activity ID: ");
			Activity app = activityRepository.getActivityById(appId);
			if (app == null) {
				console.printf("Could not find app with id %s\n", appId);
				return;
			}

			String controllerId = console.readLine("Controller ID: ");
			SpaceController controller = controllerRepository
					.getSpaceControllerById(controllerId);
			if (controller == null) {
				console.printf("Could not find controller with id %s\n",
						controllerId);
				return;
			}

			LiveActivity iapp = activityRepository.newLiveActivity();
			iapp.setController(controller);
			iapp.setActivity(app);

			String name = console.readLine("Name: ");
			iapp.setName(name);

			String description = console.readLine("Description: ");
			if (!description.trim().isEmpty())
				iapp.setDescription(description);

			activityRepository.saveLiveActivity(iapp);
		}
	}

	/**
	 * A shell command to deploy an activity.
	 * 
	 * @param args
	 */
	public void deploy_iapp(CommandSession session, final String[] args) {
		if (args.length < 1) {
			System.out.println("deploy_iapp id1 id2 id3...");
		}

		for (String id : args) {
			System.out.format("Deploying activity %s\n", id);
			uiControllerManager.deployLiveActivity(id);
		}
	}

	/**
	 * A shell command to start up an activity.
	 * 
	 * @param args
	 */
	public void startup_iapp(CommandSession session, final String[] args) {
		if (args.length < 1) {
			System.out.println("startup_iapp id1 id2 id3...");
		}

		for (String id : args) {
			System.out.format("Starting up activity %s\n", id);
			uiControllerManager.startupLiveActivity(id);
		}
	}

	/**
	 * A shell command to activate an activity.
	 * 
	 * @param args
	 */
	public void activate_iapp(CommandSession session, final String[] args) {
		if (args.length < 1) {
			System.out.println("activate_iapp id1 id2 id3...");
		}

		for (String id : args) {
			System.out.format("Activating activity %s\n", id);
			uiControllerManager.activateLiveActivity(id);
		}
	}

	/**
	 * A shell command to deactivate an activity.
	 * 
	 * @param args
	 */
	public void deactivate_iapp(CommandSession session, final String[] args) {
		if (args.length < 1) {
			System.out.println("deactivate_iapp id1 id2 id3...");
		}

		for (String id : args) {
			System.out.format("Deactivating activity %s\n", id);
			uiControllerManager.deactivateLiveActivity(id);
		}
	}

	/**
	 * A shell command to shutdown an activity.
	 * 
	 * @param args
	 */
	public void shutdown_iapp(CommandSession session, final String[] args) {
		if (args.length < 1) {
			System.out.println("shutdown_iapp id1 id2 id3...");
		}

		for (String id : args) {
			System.out.format("Shutting down activity %s\n", id);
			uiControllerManager.shutdownLiveActivity(id);
		}
	}

	/**
	 * A shell command to delete an activity.
	 * 
	 * @param args
	 */
	public void delete_iapp(CommandSession session, final String[] args) {
		if (args.length < 1) {
			System.out.println("delete_iapp id1 id2 id3...");
		}

		for (String id : args) {
			System.out.format("Deleting activity %s\n", id);
			uiActivityManager.deleteLiveActivity(id);
		}
	}

	/**
	 * A shell command to list all installed activity groups.
	 * 
	 * @param args
	 */
	public void list_group(CommandSession session, String[] args) {
		List<LiveActivityGroup> groups = activityRepository
				.getAllLiveActivityGroups();

		System.out.format("Number of installed activity groups: %d\n",
				groups.size());

		for (LiveActivityGroup group : groups) {
			System.out.format("%s\n\t%s\n", group.getName(), group.getId());
		}
	}

	/**
	 * A shell command to add an activity.
	 * 
	 * @param args
	 */
	public void add_group(CommandSession session, String[] args) {
		System.out.println("Not implemented yet");
	}

	/**
	 * A shell command to deploy an activity group.
	 * 
	 * @param args
	 */
	public void deploy_group(CommandSession session, final String[] args) {
		if (args.length < 1) {
			System.out.println("deploy_group id1 id2 id3...");
		}

		for (String id : args) {
			System.out.format("Deploying activity group %s\n", id);
			uiControllerManager.deployLiveActivityGroup(id);
		}
	}

	/**
	 * A shell command to start up an activity.
	 * 
	 * @param args
	 */
	public void startup_group(CommandSession session, final String[] args) {
		if (args.length < 1) {
			System.out.println("startup_group id1 id2 id3...");
		}

		for (String id : args) {
			System.out.format("Starting up activity group %s\n", id);
			uiControllerManager.startupLiveActivityGroup(id);
		}
	}

	/**
	 * A shell command to activate an activity group.
	 * 
	 * @param args
	 */
	public void activate_group(CommandSession session, final String[] args) {
		if (args.length < 1) {
			System.out.println("activate_group id1 id2 id3...");
		}

		for (String id : args) {
			System.out.format("Activating activity group %s\n", id);
			uiControllerManager.activateLiveActivityGroup(id);
		}
	}

	/**
	 * A shell command to deactivate an activity group.
	 * 
	 * @param args
	 */
	public void deactivate_group(CommandSession session, final String[] args) {
		if (args.length < 1) {
			System.out.println("deactivate_group id1 id2 id3...");
		}

		for (String id : args) {
			System.out.format("Deactivating live activity group %s\n", id);
			uiControllerManager.deactivateLiveActivityGroup(id);
		}
	}

	/**
	 * A shell command to shutdown an activity group.
	 * 
	 * @param args
	 */
	public void shutdown_group(CommandSession session, final String[] args) {
		if (args.length < 1) {
			System.out.println("shutdown_group id1 id2 id3...");
		}

		for (String id : args) {
			System.out.format("Shutting down activity group %s\n", id);
			uiControllerManager.shutdownLiveActivityGroup(id);
		}
	}

	/**
	 * A shell command to delete an activity group.
	 * 
	 * @param args
	 */
	public void delete_group(CommandSession session, final String[] args) {
		if (args.length < 1) {
			System.out.println("delete_group id1 id2 id3...");
		}

		for (String id : args) {
			System.out.format("Deleting activity group %s\n", id);
			uiActivityManager.deleteActivityGroup(id);
		}
	}

	public void script(CommandSession session, final String[] args) {
		if (args.length < 1) {
			System.out
					.println("script script1 script2 script3... where each script is a filename");
		}

		Map<String, Object> bindings = Maps.newHashMap();
		bindings.put("controllerManager", uiControllerManager);

		try {
			for (int i = 0; i < args.length; i++) {
				File scriptFile = new File(spaceEnvironment.getFilesystem()
						.getInstallDirectory(), args[i]);
				if (scriptFile.exists()) {
					if (scriptFile.canRead()) {
						String name = scriptFile.getName();
						String extension = name
								.substring(name.lastIndexOf('.') + 1);
						scriptService.executeScriptByExtension(extension,
								new FileScriptSource(scriptFile), bindings);
					} else {
						spaceEnvironment.getLog().error(
								String.format("Script file %s is not readable",
										scriptFile.getAbsolutePath()));
					}

				} else {
					spaceEnvironment.getLog().error(
							String.format("Script file %s does not exist",
									scriptFile.getAbsolutePath()));
				}
			}
		} catch (Exception ex) {
			spaceEnvironment.getLog().error("Error while running script", ex);
		}
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
	 * @param activityRepository
	 *            the activityRepository to set
	 */
	public void setActivityRepository(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
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
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

	/**
	 * @param scriptService
	 *            the scriptService to set
	 */
	public void setScriptService(ScriptService scriptService) {
		this.scriptService = scriptService;
	}

	/**
	 * @param spaceSystemControl
	 *            the spaceSystemControl to set
	 */
	public void setSpaceSystemControl(
			InteractiveSpacesSystemControl spaceSystemControl) {
		this.spaceSystemControl = spaceSystemControl;
	}

	/**
	 * @param bundleContext
	 *            the bundleContext to set
	 */
	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

}
