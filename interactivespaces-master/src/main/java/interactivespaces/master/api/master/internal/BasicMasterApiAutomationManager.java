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

package interactivespaces.master.api.master.internal;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.domain.support.AutomationUtils;
import interactivespaces.domain.system.NamedScript;
import interactivespaces.domain.system.pojo.SimpleNamedScript;
import interactivespaces.master.api.master.MasterApiAutomationManager;
import interactivespaces.master.api.master.MasterApiMessageSupport;
import interactivespaces.master.server.services.AutomationManager;
import interactivespaces.master.server.services.AutomationRepository;
import interactivespaces.service.scheduler.SchedulerService;

import java.util.Map;
import java.util.Set;

/**
 * A basic implementation of the {@link MasterApiAutomationManager}.
 *
 * @author Keith M. Hughes
 */
public class BasicMasterApiAutomationManager extends BaseMasterApiManager implements MasterApiAutomationManager {

  /**
   * The automation repository.
   */
  private AutomationRepository automationRepository;

  /**
   * The automation manager.
   */
  private AutomationManager automationManager;

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
      throw new SimpleInteractiveSpacesException(String.format("Named script with id %s does not exist", id));
    }
  }

  /**
   * Start a script if it is scheduled.
   *
   * @param script
   *          the script to schedule
   */
  private void potentiallyScheduleScript(NamedScript script) {
    if (script.getScheduled()) {
      SchedulerService schedulerService =
          spaceEnvironment.getServiceRegistry().getService(SchedulerService.SERVICE_NAME);
      if (schedulerService != null) {
        spaceEnvironment.getLog().info(String.format("Scheduling script %s", script.getName()));

        String schedule = script.getSchedule();
        if (schedule.startsWith(SCHEDULE_TYPE_ONCE)) {
          // TODO(keith): Support once running
        } else if (schedule.startsWith(SCHEDULE_TYPE_REPEAT)) {
          // TODO(keith): Allow specification or autogeneration of job and group
          // names.
          schedulerService.scheduleScriptWithCron("foo", "bar", "goober",
              "0 " + schedule.substring(SCHEDULE_TYPE_REPEAT.length()));
        } else {
          spaceEnvironment.getLog().error(
              String.format("Script %s has an illegal schedule: %s", script.getName(), schedule));
        }
      } else {
        spaceEnvironment.getLog().warn(
            String.format("No scheduling service for scheduling script %s", script.getName()));
      }
    }
  }

  @Override
  public Map<String, Object> deleteNamedScript(String id) {
    NamedScript script = automationRepository.getNamedScriptById(id);
    if (script != null) {
      automationRepository.deleteNamedScript(script);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchNamedScriptResult();
    }
  }

  @Override
  public Map<String, Object> runScript(String id) {
    spaceEnvironment.getLog().info(String.format("Running script with id %s", id));
    NamedScript script = automationRepository.getNamedScriptById(id);
    if (script != null) {
      // TODO(keith): Run in another thread?
      automationManager.runScript(script);

      return MasterApiMessageSupport.getSimpleSuccessResponse();
    } else {
      return getNoSuchNamedScriptResult();
    }
  }

  /**
   * Get a Master API response for no such named script.
   *
   * @return the Master API response
   */
  private Map<String, Object> getNoSuchNamedScriptResult() {
    return MasterApiMessageSupport
        .getFailureResponse(MasterApiAutomationManager.MESSAGE_SPACE_DOMAIN_NAMEDSCRIPT_UNKNOWN);
  }

  /**
   * @param automationRepository
   *          the automationRepository to set
   */
  public void setAutomationRepository(AutomationRepository automationRepository) {
    this.automationRepository = automationRepository;
  }

  /**
   * @param automationManager
   *          the automationManager to set
   */
  public void setAutomationManager(AutomationManager automationManager) {
    this.automationManager = automationManager;
  }
}
