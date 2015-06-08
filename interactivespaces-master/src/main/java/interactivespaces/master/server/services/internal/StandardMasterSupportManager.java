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

import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.server.services.AutomationRepository;
import interactivespaces.master.server.services.MasterSupportManager;
import interactivespaces.master.server.services.SpaceControllerRepository;
import interactivespaces.master.server.services.internal.support.JdomMasterDomainModelCreator;
import interactivespaces.master.server.services.internal.support.JdomMasterDomainModelImporter;
import interactivespaces.system.InteractiveSpacesEnvironment;

/**
 * The standard implementation of the {@link MasterSupportManager}.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterSupportManager implements MasterSupportManager {

  /**
   * Repository for activity entities.
   */
  private ActivityRepository activityRepository;

  /**
   * Repository for controller entities.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * Repository for automation entities.
   */
  private AutomationRepository automationRepository;

  /**
   * The space environment being run under.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  @Override
  public void startup() {
    // Nothing right now
  }

  @Override
  public void shutdown() {
    // Nothing right now.
  }

  @Override
  public String getMasterDomainModel() {
    JdomMasterDomainModelCreator creator = new JdomMasterDomainModelCreator();

    return creator.newModel(activityRepository, spaceControllerRepository, automationRepository);
  }

  @Override
  public void importMasterDomainModel(String model) {
    JdomMasterDomainModelImporter importer = new JdomMasterDomainModelImporter();

    importer.importModel(model, activityRepository, spaceControllerRepository, automationRepository,
        spaceEnvironment.getTimeProvider());
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
   * @param automationRepository
   *          the automationRepository to set
   */
  public void setAutomationRepository(AutomationRepository automationRepository) {
    this.automationRepository = automationRepository;
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }
}
