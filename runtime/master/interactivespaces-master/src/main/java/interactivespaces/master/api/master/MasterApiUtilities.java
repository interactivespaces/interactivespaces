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

package interactivespaces.master.api.master;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.space.Space;
import interactivespaces.domain.system.NamedScript;
import interactivespaces.master.server.services.ActiveSpaceController;

import java.util.Comparator;

/**
 * Utilities for the master side for working with the master.
 *
 * @author Keith M. Hughes
 */
public class MasterApiUtilities {

  /**
   * A comparator for controllers which orders by name.
   */
  public static final SpaceControllerByNameComparator SPACE_CONTROLLER_BY_NAME_COMPARATOR =
      new SpaceControllerByNameComparator();

  /**
   * A comparator for activities which orders by name.
   */
  public static final ActivityByNameAndVersionComparator ACTIVITY_BY_NAME_AND_VERSION_COMPARATOR =
      new ActivityByNameAndVersionComparator();

  /**
   * A comparator for installed activities which orders by name.
   */
  public static final LiveActivityByNameComparator LIVE_ACTIVITY_BY_NAME_COMPARATOR =
      new LiveActivityByNameComparator();

  /**
   * A comparator for live activity groups which orders by name.
   */
  public static final LiveActivityGroupByNameComparator LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR =
      new LiveActivityGroupByNameComparator();

  /**
   * A comparator for spaces which orders by name.
   */
  public static final SpaceByNameComparator SPACE_BY_NAME_COMPARATOR = new SpaceByNameComparator();

  /**
   * A comparator for active controllers which orders by name.
   */
  public static final ActiveControllerByNameComparator ACTIVE_CONTROLLER_BY_NAME_COMPARATOR =
      new ActiveControllerByNameComparator();

  /**
   * A comparator for named scripts which orders by name.
   */
  public static final NamedScriptByNameComparator NAMED_SCRIPT_BY_NAME_COMPARATOR =
      new NamedScriptByNameComparator();

  /**
   * A comparator for installed activity which orders by name.
   *
   * @author Keith M. Hughes
   */
  private static class SpaceControllerByNameComparator implements Comparator<SpaceController> {
    @Override
    public int compare(SpaceController o1, SpaceController o2) {
      return o1.getName().compareToIgnoreCase(o2.getName());
    }
  }

  /**
   * A comparator for activities which orders by name first then subsorts by
   * version.
   *
   * @author Keith M. Hughes
   */
  private static class ActivityByNameAndVersionComparator implements Comparator<Activity> {
    @Override
    public int compare(Activity o1, Activity o2) {
      int compare = o1.getName().compareToIgnoreCase(o2.getName());

      if (compare == 0) {
        compare = o1.getVersion().compareTo(o2.getVersion());
      }

      return compare;
    }
  }

  /**
   * A comparator for live activities which orders by name.
   *
   * @author Keith M. Hughes
   */
  private static class LiveActivityByNameComparator implements Comparator<LiveActivity> {
    @Override
    public int compare(LiveActivity o1, LiveActivity o2) {
      return o1.getName().compareToIgnoreCase(o2.getName());
    }
  }

  /**
   * A comparator for live activity groups which orders by name.
   *
   * @author Keith M. Hughes
   */
  private static class LiveActivityGroupByNameComparator implements Comparator<LiveActivityGroup> {
    @Override
    public int compare(LiveActivityGroup o1, LiveActivityGroup o2) {
      return o1.getName().compareToIgnoreCase(o2.getName());
    }
  }

  /**
   * A comparator for spaces which orders by name.
   *
   * @author Keith M. Hughes
   */
  private static class SpaceByNameComparator implements Comparator<Space> {
    @Override
    public int compare(Space o1, Space o2) {
      return o1.getName().compareToIgnoreCase(o2.getName());
    }
  }

  /**
   * A comparator for active controllers which orders by name.
   *
   * @author Keith M. Hughes
   */
  private static class ActiveControllerByNameComparator implements
      Comparator<ActiveSpaceController> {
    @Override
    public int compare(ActiveSpaceController o1, ActiveSpaceController o2) {
      return o1.getSpaceController().getName().compareToIgnoreCase(o2.getSpaceController().getName());
    }
  }

  /**
   * A comparator for named scripts which orders by name.
   *
   * @author Keith M. Hughes
   */
  private static class NamedScriptByNameComparator implements Comparator<NamedScript> {
    @Override
    public int compare(NamedScript o1, NamedScript o2) {
      return o1.getName().compareToIgnoreCase(o2.getName());
    }
  }
}
