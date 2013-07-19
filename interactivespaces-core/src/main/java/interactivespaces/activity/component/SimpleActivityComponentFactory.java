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

package interactivespaces.activity.component;

import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;

import java.util.Map;

/**
 * A simple implementation of the {@link ActivityComponentFactory}.
 *
 * @author Keith M. Hughes
 */
public class SimpleActivityComponentFactory implements ActivityComponentFactory {

  /**
   * The mapping of component names to the class implementing the component.
   */
  private Map<String, Class<? extends ActivityComponent>> componentClassMap = Maps.newHashMap();

  @Override
  public void register(String componentName, Class<? extends ActivityComponent> componentClass) {
    componentClassMap.put(componentName, componentClass);
  }

  @Override
  public <T extends ActivityComponent> T newComponent(String componentName) {
    Class<? extends ActivityComponent> componentClass = componentClassMap.get(componentName);

    if (componentClass != null) {
      try {
        @SuppressWarnings("unchecked")
        T component = (T) componentClass.newInstance();

        return component;
      } catch (Exception e) {
        throw new InteractiveSpacesException(String.format("Cannot create activity component %s",
            componentName), e);
      }
    } else {
      throw new InteractiveSpacesException(String.format("Unknown activity component %s",
          componentName));
    }
  }
}
