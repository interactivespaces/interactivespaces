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

package interactivespaces.controller.ui.internal.osgi;

import interactivespaces.controller.SpaceController;
import interactivespaces.controller.repository.LocalSpaceControllerRepository;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.util.resource.ManagedResource;

import org.osgi.framework.BundleContext;

/**
 * A shell for controllers.
 *
 * @author Keith M. Hughes
 */
public class OsgiControllerShell implements ManagedResource {

  /**
   * Repository for controller items.
   */
  private final LocalSpaceControllerRepository controllerRepository;

  /**
   * Bundle context for hooking into the shell system.
   */
  private final BundleContext bundleContext;

  /**
   * The {@link SpaceController} to be controlled.
   */
  private final SpaceController spaceController;

  /**
   * Control of the Interactive Spaces system.
   */
  private final InteractiveSpacesSystemControl spaceSystemControl;

  /**
   * Construct a controller shell.
   *
   * @param spaceController
   *          the space controller this is the shell for
   * @param spaceSystemControl
   *          system control of the controller
   * @param controllerRepository
   *          the controller repository
   * @param bundleContext
   *          the bundle context the shell runs under
   */
  public OsgiControllerShell(SpaceController spaceController, InteractiveSpacesSystemControl spaceSystemControl,
      LocalSpaceControllerRepository controllerRepository, BundleContext bundleContext) {
    this.spaceController = spaceController;
    this.spaceSystemControl = spaceSystemControl;
    this.controllerRepository = controllerRepository;
    this.bundleContext = bundleContext;
  }

  @Override
  public void startup() {
    // Dictionary<String, Object> dict = new Hashtable<String, Object>();
    // dict.put(CommandProcessor.COMMAND_SCOPE, "interactivespaces");
    // dict.put(CommandProcessor.COMMAND_FUNCTION, new String[] {
    // "list_iapps", "startup_iapp", "activate_iapp",
    // "deactivate_iapp", "shutdown_iapp", "delete_iapp", "shutdown" });
    // bundleContext.registerService(getClass().getName(), this, dict);
  }

  @Override
  public void shutdown() {
  }

  // /**
  // * A shell command to shut down Interactive Spaces.
  // *
  // * @param args
  // */
  // public void shutdown(CommandSession session, String[] args) {
  // spaceSystemControl.shutdown();
  // }
  //
  // /**
  // * A shell command to list all activities.
  // *
  // * @param args
  // */
  // public void list_iapps(CommandSession session, String[] args) {
  // List<InstalledLiveActivity> apps = controllerRepository
  // .getAllInstalledLiveActivities();
  //
  // System.out.format("Number of activities: %d\n", apps.size());
  //
  // for (InstalledLiveActivity app : apps) {
  // System.out.format("%s\n\t%s\t%s\n", app.getUuid(),
  // app.getIdentifyingName(), app.getVersion());
  // }
  // }
  //
  // /**
  // * A shell command to start up an activity.
  // *
  // * @param args
  // */
  // public void startup_iapp(CommandSession session, final String[] args) {
  // if (args.length < 1) {
  // System.out.println("startup_iapp id1 id2 id3...");
  // }
  //
  // for (String id : args) {
  // System.out.format("Starting up activity %s\n", id);
  // spaceController.startupActivity(id);
  // }
  // }
  //
  // /**
  // * A shell command to activate an activity.
  // *
  // * @param args
  // */
  // public void activate_iapp(CommandSession session, final String[] args) {
  // if (args.length < 1) {
  // System.out.println("activate_iapp id1 id2 id3...");
  // }
  //
  // for (String id : args) {
  // System.out.format("Activating activity %s\n", id);
  // spaceController.activateActivity(id);
  // }
  // }
  //
  // /**
  // * A shell command to deactivate an activity.
  // *
  // * @param args
  // */
  // public void deactivate_iapp(CommandSession session, final String[] args) {
  // if (args.length < 1) {
  // System.out.println("deactivate_iapp id1 id2 id3...");
  // }
  //
  // for (String id : args) {
  // System.out.format("Deactivating activity %s\n", id);
  // spaceController.deactivateActivity(id);
  // }
  // }
  //
  // /**
  // * A shell command to shutdown an activity.
  // *
  // * @param args
  // */
  // public void shutdown_iapp(CommandSession session, final String[] args) {
  // if (args.length < 1) {
  // System.out.println("shutdown_iapp id1 id2 id3...");
  // }
  //
  // for (String id : args) {
  // System.out.format("Shutting down activity %s\n", id);
  // spaceController.shutdownActivity(id);
  // }
  // }
  //
  // /**
  // * A shell command to delete an activity.
  // *
  // * @param args
  // */
  // public void delete_iapp(CommandSession session, final String[] args) {
  // if (args.length < 1) {
  // System.out.println("delete_iapp id1 id2 id3...");
  // }
  //
  // for (String id : args) {
  // System.out
  // .format("(Not implemented) Deleting activity %s\n", id);
  // }
  // }
}
