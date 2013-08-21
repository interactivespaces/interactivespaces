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

package interactivespaces.master.ui.internal.web.liveactivity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.master.server.services.ActivityRepository;
import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;
import interactivespaces.master.ui.internal.web.ConfigurationForm;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A form for editing live activities.
 *
 * @author Keith M. Hughes
 */
@Controller
@RequestMapping("/liveactivity/{id}/config/edit")
@SessionAttributes({ "liveactivity", "id", "config" })
public class LiveActivityConfigEditForm extends BaseSpaceMasterController {

  /**
   * The activity repository.
   */
  private ActivityRepository activityRepository;

  @InitBinder
  public void setAllowedFields(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
  }

  @RequestMapping(method = RequestMethod.GET)
  public String setupForm(@PathVariable("id") String id, Model model) {
    LiveActivity liveactivity = activityRepository.getLiveActivityById(id);
    model.addAttribute("liveactivity", liveactivity);
    model.addAttribute("id", id);

    addGlobalModelItems(model);

    ConfigurationForm configurationForm = newConfigurationForm(liveactivity);

    model.addAttribute("config", configurationForm);

    return "liveactivity/LiveActivityConfigurationEdit";
  }

  @RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
  public String processSubmit(@PathVariable("id") String id,
      @ModelAttribute("config") ConfigurationForm configurationForm, BindingResult result,
      SessionStatus status) {
    configurationForm.validate(result, true, "space.config");
    if (result.hasErrors()) {
      return "liveactivity/LiveActivityConfigurationEdit";
    } else {
      LiveActivity liveactivity = activityRepository.getLiveActivityById(id);

      if (saveConfigurationForm(configurationForm, liveactivity)) {
        activityRepository.saveLiveActivity(liveactivity);
      }

      status.setComplete();

      return "redirect:/liveactivity/" + id + "/view.html";
    }
  }

  /**
   * Create a configuration form with the values of the configuration parameters
   * in it.
   *
   * @param liveactivity
   *          the live activity which contains the configuration
   *
   * @return a filled out form object
   */
  private ConfigurationForm newConfigurationForm(LiveActivity liveactivity) {
    ConfigurationForm configurationForm = new ConfigurationForm();

    ActivityConfiguration configuration = liveactivity.getConfiguration();
    if (configuration != null) {
      List<ConfigurationParameter> configParameters =
          Lists.newArrayList(configuration.getParameters());

      // Want the parameters sorted by name.
      Collections.sort(configParameters, new Comparator<ConfigurationParameter>() {
        @Override
        public int compare(ConfigurationParameter o1, ConfigurationParameter o2) {
          return o1.getName().compareToIgnoreCase(o2.getName());
        }
      });

      StringBuilder builder = new StringBuilder();
      for (ConfigurationParameter parameter : configParameters) {
        builder.append(parameter.getName()).append("=").append(parameter.getValue()).append('\n');
      }

      configurationForm.setValues(builder.toString());
    } else {
      configurationForm.setValues("");
    }

    return configurationForm;
  }

  /**
   * Create a configuration form with the values of the configuration parameters
   * in it.
   *
   * @param liveactivity
   *          the live activity which contains the configuration
   *
   * @return {@code true} if there were changes
   */
  private boolean saveConfigurationForm(ConfigurationForm form, LiveActivity liveactivity) {
    Map<String, String> map = getSubmittedMap(form);

    return saveConfiguration(liveactivity, map);
  }

  /**
   * save the configuration.
   *
   * @param liveactivity
   *          the live activity being reconfigured
   * @param map
   *          the map of new configurations
   *
   * @return {@code true} if there was a change in the configuration
   */
  private boolean saveConfiguration(LiveActivity liveactivity, Map<String, String> map) {
    ActivityConfiguration configuration = liveactivity.getConfiguration();
    if (configuration != null) {
      return mergeParameters(map, configuration);
    } else {
      // No configuration. If nothing in submission, nothing has changed.
      // Otherwise add everything.
      if (map.isEmpty())
        return false;

      createNewConfiguration(liveactivity, map);

      return true;
    }
  }

  /**
   * Merge the values in the map with the configuration.
   *
   * @param map
   *          map of new name/value pairs
   * @param configuration
   *          the configuration which may be changed
   *
   * @return {@code true} if there were any parameters changed in the
   *         configuration
   */
  private boolean mergeParameters(Map<String, String> map, ActivityConfiguration configuration) {
    boolean changed = false;

    Map<String, ConfigurationParameter> existingMap = configuration.getParameterMap();

    // Delete all items removed
    for (Entry<String, ConfigurationParameter> entry : existingMap.entrySet()) {
      if (!map.containsKey(entry.getKey())) {
        changed = true;

        configuration.removeParameter(entry.getValue());
      }
    }

    // Now everything in the submitted map will be check. if the name exists
    // in the old configuration, we will try and change the value. if the
    // name
    // doesn't exist, add it.
    for (Entry<String, String> entry : map.entrySet()) {
      ConfigurationParameter parameter = existingMap.get(entry.getKey());
      if (parameter != null) {
        // Existed
        String oldValue = parameter.getValue();
        if (!oldValue.equals(entry.getValue())) {
          changed = true;
          parameter.setValue(entry.getValue());
        }
      } else {
        // Didn't exist
        changed = true;

        parameter = activityRepository.newConfigurationParameter();
        parameter.setName(entry.getKey());
        parameter.setValue(entry.getValue());

        configuration.addParameter(parameter);
      }

    }
    return changed;
  }

  /**
   * Create a new configuration for the activity.
   *
   * @param liveactivity
   *          the live activity to be configured
   * @param map
   *          the new metadata
   */
  private void createNewConfiguration(LiveActivity liveactivity, Map<String, String> map) {
    ActivityConfiguration configuration;
    configuration = activityRepository.newActivityConfiguration();
    liveactivity.setConfiguration(configuration);

    for (Entry<String, String> entry : map.entrySet()) {
      ConfigurationParameter parameter = activityRepository.newConfigurationParameter();
      parameter.setName(entry.getKey());
      parameter.setValue(entry.getValue());

      configuration.addParameter(parameter);
    }
  }

  /**
   * Get a map of the submitted parameters.
   *
   * @param form
   *          the form
   *
   * @return a map of the config names to values
   */
  protected Map<String, String> getSubmittedMap(ConfigurationForm form) {
    Map<String, String> map = Maps.newHashMap();

    String[] lines = form.getValues().split("\n");

    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty())
        continue;

      int pos = line.indexOf('=');
      map.put(new String(line.substring(0, pos).trim()), new String(line.substring(pos + 1).trim()));
    }
    return map;
  }

  /**
   * @param activityRepository
   *          the activityRepository to set
   */
  public void setActivityRepository(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }
}