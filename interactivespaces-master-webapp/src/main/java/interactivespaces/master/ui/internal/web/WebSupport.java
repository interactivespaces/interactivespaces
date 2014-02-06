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

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.SpaceControllerMode;
import interactivespaces.domain.space.Space;
import interactivespaces.master.api.MasterApiUtilities;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.springframework.context.MessageSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Support for Spring WebFlow operations.
 *
 * <p>
 * For example, WebFlow needs properly serializable entities in its context.
 *
 *
 * @author Keith M. Hughes
 */
public final class WebSupport {

  /**
   * Format to be used for Activity selection names.
   *
   * Argument 0 is the name of the activity, argument 1 is the activity version.
   */
  public static final String FORMAT_APPLICATION_SELECTION_NAME = "{0} ({1})";

  /**
   * Private default constructor for utility class.
   */
  private WebSupport() {
  }

  /**
   * Get a selection list of activities.
   *
   * @param activities
   *          The activities.
   *
   * @return an ordered list of activities
   */
  public static Map<String, String> getActivitySelections(List<Activity> activities) {
    List<Activity> toBeSorted = Lists.newArrayList(activities);
    Collections.sort(toBeSorted, MasterApiUtilities.ACTIVITY_BY_NAME_AND_VERSION_COMPARATOR);

    Map<String, String> items = Maps.newLinkedHashMap();
    for (Activity activity : toBeSorted) {
      items.put(activity.getId(),
          String.format("%s - %s", activity.getName(), activity.getVersion()));
    }

    return items;
  }

  /**
   * Get a selection list of controllers.
   *
   * @param controllers
   *          The controllers.
   *
   * @return List of controllers ordered by name
   */
  public static Map<String, String> getControllerSelections(List<SpaceController> controllers) {
    List<SpaceController> toBeSorted = Lists.newArrayList(controllers);
    Collections.sort(toBeSorted, MasterApiUtilities.SPACE_CONTROLLER_BY_NAME_COMPARATOR);

    Map<String, String> items = Maps.newLinkedHashMap();
    for (SpaceController controller : toBeSorted) {
      items.put(controller.getId(), controller.getName());
    }

    return items;
  }

  /**
   * Get a selection list of controller modes.
   *
   * @param messageSource
   *          messages for translation
   * @param locale
   *          locale for translation
   *
   * @return list of controller modes
   */
  public static Map<String, String> getControllerModes(MessageSource messageSource, Locale locale) {
    Map<String, String> items = Maps.newLinkedHashMap();
    for (SpaceControllerMode mode : SpaceControllerMode.values()) {
      items.put(mode.name(), messageSource.getMessage(mode.getDescription(), null, locale));
    }
    return items;
  }

  /**
   * Get a map of live activity names keyed by IDs.
   *
   * @param activities
   *          the live activities
   *
   * @return selections ordered by name
   */
  public static Map<String, String> getLiveActivitySelections(List<LiveActivity> activities) {
    List<LiveActivity> toBeSorted = Lists.newArrayList(activities);
    Collections.sort(toBeSorted, MasterApiUtilities.LIVE_ACTIVITY_BY_NAME_COMPARATOR);

    Map<String, String> items = Maps.newLinkedHashMap();
    for (LiveActivity activity : toBeSorted) {
      items.put(activity.getId(), activity.getName());
    }

    return items;
  }

  /**
   * Get a selection list of activity groups.
   *
   * @param groups
   *          The activity groups.
   *
   * @return items ordered by name
   */
  public static Map<String, String> getLiveActivityGroupSelections(List<LiveActivityGroup> groups) {
    List<LiveActivityGroup> toBeSorted = Lists.newArrayList(groups);
    Collections.sort(toBeSorted, MasterApiUtilities.LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR);

    Map<String, String> items = Maps.newLinkedHashMap();
    for (LiveActivityGroup group : toBeSorted) {
      items.put(group.getId(), group.getName());
    }

    return items;
  }

  /**
   * Get a selection list of spaces.
   *
   * @param spaces
   *          the spaces
   *
   * @return items ordered by name
   */
  public static Map<String, String> getSpaceSelections(List<Space> spaces) {
    List<Space> toBeSorted = Lists.newArrayList(spaces);
    Collections.sort(toBeSorted, MasterApiUtilities.SPACE_BY_NAME_COMPARATOR);

    Map<String, String> items = Maps.newLinkedHashMap();
    for (Space space : toBeSorted) {
      items.put(space.getId(), space.getName());
    }

    return items;
  }

  /**
   * Get a map of activity group names keyed by IDs.
   *
   * @param languages
   *          the scripting languages available
   *
   * @return language map keyed by id
   */
  public static Map<String, String> getScriptingLanguageSelections(Set<String> languages) {
    // TODO(keith): Look into better way to decide on languages
    Map<String, String> map = new HashMap<String, String>();
    for (String name : languages) {
      map.put(name, name);
    }

    return map;
  }
}
