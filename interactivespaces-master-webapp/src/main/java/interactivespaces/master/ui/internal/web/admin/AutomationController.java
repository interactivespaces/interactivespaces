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
import interactivespaces.master.api.master.MasterApiAutomationManager;
import interactivespaces.master.api.master.MasterApiUtilities;
import interactivespaces.master.api.messages.MasterApiMessageSupport;
import interactivespaces.master.server.services.AutomationRepository;
import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A Spring MVC controller for automation activities.
 *
 * @author Keith M. Hughes
 */
@Controller
public class AutomationController extends BaseSpaceMasterController {

  /**
   * The repository for automation artifacts.
   */
  private AutomationRepository automationRepository;

  /**
   * The UI manager for automation.
   */
  private MasterApiAutomationManager masterApiAutomationManager;

  /**
   * Display a list of all named scripts.
   *
   * @return Model and view for named script list display.
   */
  @RequestMapping("/admin/namedscript/all.html")
  public ModelAndView listNamedScripts() {
    List<NamedScript> scripts = Lists.newArrayList(automationRepository.getAllNamedScripts());
    Collections.sort(scripts, MasterApiUtilities.NAMED_SCRIPT_BY_NAME_COMPARATOR);

    ModelAndView mav = getModelAndView();
    mav.setViewName("admin/NamedScriptViewAll");
    mav.addObject("scripts", scripts);

    return mav;
  }

  @RequestMapping(value = "/admin/namedscript/{id}/view.html", method = RequestMethod.GET)
  public ModelAndView viewScript(@PathVariable String id) {
    ModelAndView mav = getModelAndView();

    NamedScript controller = automationRepository.getNamedScriptById(id);
    if (controller != null) {
      mav.setViewName("admin/NamedScriptView");

      mav.addObject("script", controller);
    } else {
      mav.setViewName("admin/NamedScriptNonexistent");
    }

    return mav;
  }

  @RequestMapping(value = "/admin/namedscript/{id}/delete.html", method = RequestMethod.GET)
  public ModelAndView deleteController(@PathVariable String id) {
    ModelAndView mav = getModelAndView();
    Map<String, Object> response = masterApiAutomationManager.deleteNamedScript(id);
    if (MasterApiMessageSupport.isSuccessResponse(response)) {
      mav.clear();
      mav.setViewName("redirect:/admin/namedscript/all.html");
    } else {
      mav.setViewName("admin/NamedScriptNonexistent");
    }

    return mav;
  }

  @RequestMapping(value = "/admin/namedscript/all.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> listAllControllersJson() {
    List<Map<String, Object>> data = Lists.newArrayList();

    for (NamedScript script : automationRepository.getAllNamedScripts()) {
      Map<String, Object> scriptData = Maps.newHashMap();

      scriptData.put("id", script.getId());
      scriptData.put("name", script.getName());
      scriptData.put("description", script.getDescription());

      data.add(scriptData);
    }

    return MasterApiMessageSupport.getSuccessResponse(data);
  }

  @RequestMapping(value = "/admin/namedscript/{id}/run.json", method = RequestMethod.GET)
  public @ResponseBody
  Map<String, ? extends Object> runNamedScript(@PathVariable String id) {
    return masterApiAutomationManager.runScript(id);
  }

  /**
   * @param automationRepository
   *          the automationRepository to set
   */
  public void setAutomationRepository(AutomationRepository automationRepository) {
    this.automationRepository = automationRepository;
  }

  /**
   * @param masterApiAutomationManager
   *          the masterApiAutomationManager to set
   */
  public void setMasterApiAutomationManager(MasterApiAutomationManager masterApiAutomationManager) {
    this.masterApiAutomationManager = masterApiAutomationManager;
  }
}
