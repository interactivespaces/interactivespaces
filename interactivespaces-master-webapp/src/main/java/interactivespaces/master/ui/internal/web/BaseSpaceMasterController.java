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

package interactivespaces.master.ui.internal.web;

import com.google.common.collect.Maps;

import interactivespaces.expression.ExpressionFactory;
import interactivespaces.master.server.services.MasterConfigurations;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.core.collection.MutableAttributeMap;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base support for Spring MVC controllers for the space master.
 * 
 * @author Keith M. Hughes
 */
public class BaseSpaceMasterController {

  /**
   * Attribute name for the current date and time.
   */
  public static final String ATTRIBUTE_NAME_NOW = "now";

  /**
   * Attribute name for the name of interactivespaces.
   */
  public static final String ATTRIBUTE_NAME_INTERACTIVESPACES_NAME = "interactivespacesName";

  /**
   * The message source for messages.
   */
  protected MessageSource messageSource;

  /**
   * Locale for message lookup.
   * 
   * <p>
   * For now local is fixed to US. This will change.
   */
  protected Locale locale = Locale.US;

  protected String[] NO_MESSAGE_ARGS = new String[0];

  /**
   * A factory for expressions.
   */
  protected ExpressionFactory expressionFactory;

  /**
   * The spaces environment.
   */
  protected InteractiveSpacesEnvironment spacesEnvironment;

  /**
   * Get a Spring {@link ModelAndView} prepopulated with useful objects.
   * 
   * @return
   */
  public ModelAndView getModelAndView() {
    ModelAndView mav = new ModelAndView();

    addGlobalModelItems(mav.getModel());

    return mav;
  }

  /**
   * Add items to the model.
   * 
   * @param the
   *          model to be modified
   */
  public void addGlobalModelItems(Map<String, Object> model) {
    model.put(ATTRIBUTE_NAME_NOW, new Date());

    model.put(
        ATTRIBUTE_NAME_INTERACTIVESPACES_NAME,
        spacesEnvironment.getSystemConfiguration().getPropertyString(
            MasterConfigurations.CONFIGURATION_NAME_SPACE_NAME,
            MasterConfigurations.CONFIGURATION_VALUE_DEFAULT_SPACE_NAME));
  }

  /**
   * Add all needed items to a Spring Model.
   * 
   * @param model
   *          the model to add to
   */
  public void addGlobalModelItems(Model model) {
    Map<String, Object> items = Maps.newHashMap();
    addGlobalModelItems(items);
    model.addAllAttributes(items);
  }

  /**
   * Add all needed items to a Spring WebFlow View Scope.
   * 
   * @param viewScope
   *          the viewScope to add to
   */
  public void addGlobalModelItems(MutableAttributeMap viewScope) {
    Map<String, Object> items = Maps.newHashMap();
    addGlobalModelItems(items);
    for (Entry<String, Object> entry : items.entrySet()) {
      viewScope.put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * @param messageSource
   *          the messageSource to set
   */
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  /**
   * @param spacesEnvironment
   *          the spacesEnvironment to set
   */
  public void setSpacesEnvironment(InteractiveSpacesEnvironment spacesEnvironment) {
    this.spacesEnvironment = spacesEnvironment;
  }

  /**
   * @param expressionFactory
   *          the expressionFactory to set
   */
  public void setExpressionFactory(ExpressionFactory expressionFactory) {
    this.expressionFactory = expressionFactory;
  }
}
