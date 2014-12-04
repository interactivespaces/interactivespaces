/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.master.ui.internal.web.spacecontroller;

import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.SpaceControllerConfiguration;
import interactivespaces.master.server.services.SpaceControllerRepository;
import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;
import interactivespaces.master.ui.internal.web.ConfigurationForm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
 * A form for editing space controller configurations.
 *
 * @author Keith M. Hughes
 */
@Controller
@RequestMapping("/spacecontroller/{id}/config/edit")
@SessionAttributes({ "spacecontroller", "id", "config" })
public class SpaceControllerConfigEditForm extends BaseSpaceMasterController {

  /**
   * The separator between lines of name/value pairs.
   */
  public static final String NAME_VALUE_LINE_SEPARATOR = "\n";

  /**
   * The separator betwwen the name and the value of a name/value pair.
   */
  public static final char NAME_VALUE_PAIR_SEPARATOR = '=';

  /**
   * The space controller repository.
   */
  private SpaceControllerRepository spaceControllerRepository;

  /**
   * Set fields that can be bound by the edit.
   *
   * @param dataBinder
   *          the data binder
   */
  @InitBinder
  public void setAllowedFields(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
  }

  /**
   * Set up the form.
   *
   * @param id
   *          ID of the space controller
   * @param model
   *          the model for the form
   *
   * @return location of the edit form
   */
  @RequestMapping(method = RequestMethod.GET)
  public String setupForm(@PathVariable("id") String id, Model model) {
    SpaceController spaceController = spaceControllerRepository.getSpaceControllerById(id);
    model.addAttribute("spacecontroller", spaceController);
    model.addAttribute("id", id);

    addGlobalModelItems(model);

    ConfigurationForm configurationForm = newConfigurationForm(spaceController);

    model.addAttribute("config", configurationForm);

    return "spacecontroller/SpaceControllerConfigurationEdit";
  }

  /**
   * Process a form submission.
   *
   * @param id
   *          ID of the space controller
   * @param configurationForm
   *          the configuration form
   * @param result
   *          the result of performing the data binding
   * @param status
   *          the session status
   *
   * @return location of where to send the browser next
   */
  @RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
  public String processSubmit(@PathVariable("id") String id,
      @ModelAttribute("config") ConfigurationForm configurationForm, BindingResult result, SessionStatus status) {
    configurationForm.validate(result, true, "space.config");
    if (result.hasErrors()) {
      return "spacecontroller/SpaceControllerConfigurationEdit";
    } else {
      SpaceController spaceController = spaceControllerRepository.getSpaceControllerById(id);

      if (saveConfigurationForm(configurationForm, spaceController)) {
        spaceControllerRepository.saveSpaceController(spaceController);
      }

      status.setComplete();

      return "redirect:/spacecontroller/" + id + "/view.html";
    }
  }

  /**
   * Create a configuration form with the values of the configuration parameters in it.
   *
   * @param spaceController
   *          the space controller that contains the configuration
   *
   * @return a filled out form object
   */
  private ConfigurationForm newConfigurationForm(SpaceController spaceController) {
    ConfigurationForm configurationForm = new ConfigurationForm();

    SpaceControllerConfiguration configuration = spaceController.getConfiguration();
    if (configuration != null) {
      List<ConfigurationParameter> configParameters = Lists.newArrayList(configuration.getParameters());

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
   * Create a configuration form with the values of the configuration parameters in it.
   *
   * @param form
   *          the form
   * @param spaceController
   *          the space controller which contains the configuration
   *
   * @return {@code true} if there were changes
   */
  private boolean saveConfigurationForm(ConfigurationForm form, SpaceController spaceController) {
    Map<String, String> map = getSubmittedMap(form);

    return saveConfiguration(spaceController, map);
  }

  /**
   * save the configuration.
   *
   * @param spaceController
   *          the space controller being reconfigured
   * @param map
   *          the map of new configurations
   *
   * @return {@code true} if there was a change in the configuration
   */
  private boolean saveConfiguration(SpaceController spaceController, Map<String, String> map) {
    SpaceControllerConfiguration configuration = spaceController.getConfiguration();
    if (configuration != null) {
      return mergeParameters(map, configuration);
    } else {
      // No configuration. If nothing in submission, nothing has changed.
      // Otherwise add everything.
      if (map.isEmpty()) {
        return false;
      }

      createNewConfiguration(spaceController, map);

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
   * @return {@code true} if there were any parameters changed in the configuration
   */
  private boolean mergeParameters(Map<String, String> map, SpaceControllerConfiguration configuration) {
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

        parameter = spaceControllerRepository.newSpaceControllerConfigurationParameter();
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
   * @param spaceController
   *          the space controller to be configured
   * @param map
   *          the new metadata
   */
  private void createNewConfiguration(SpaceController spaceController, Map<String, String> map) {
    SpaceControllerConfiguration configuration = spaceControllerRepository.newSpaceControllerConfiguration();
    spaceController.setConfiguration(configuration);

    for (Entry<String, String> entry : map.entrySet()) {
      ConfigurationParameter parameter = spaceControllerRepository.newSpaceControllerConfigurationParameter();
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

    String[] lines = form.getValues().split(NAME_VALUE_LINE_SEPARATOR);

    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty()) {
        continue;
      }

      int pos = line.indexOf(NAME_VALUE_PAIR_SEPARATOR);
      map.put(new String(line.substring(0, pos).trim()), new String(line.substring(pos + 1).trim()));
    }
    return map;
  }

  /**
   * Set the space controller repository.
   *
   * @param spaceControllerRepository
   *          the space controller repository
   */
  public void setSpaceControllerRepository(SpaceControllerRepository spaceControllerRepository) {
    this.spaceControllerRepository = spaceControllerRepository;
  }
}
