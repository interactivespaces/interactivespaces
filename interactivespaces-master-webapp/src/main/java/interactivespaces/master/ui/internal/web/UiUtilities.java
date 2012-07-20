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
import interactivespaces.domain.space.Space;
import interactivespaces.domain.system.NamedScript;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.master.server.ui.UiLiveActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

/**
 * useful utilities for the user interface.
 * 
 * @author Keith M. Hughes
 */
public class UiUtilities {

	/**
	 * The option used when nothing should be selected in a multiple selection
	 * box.
	 */
	public static final String MULTIPLE_SELECT_NONE = "--none--";

	/**
	 * A comparator for controllers which orders by name.
	 */
	public static final SpaceControllerByNameComparator SPACE_CONTROLLER_BY_NAME_COMPARATOR = new SpaceControllerByNameComparator();

	/**
	 * A comparator for activities which orders by name.
	 */
	public static final ActivityByNameComparator ACTIVITY_BY_NAME_COMPARATOR = new ActivityByNameComparator();

	/**
	 * A comparator for installed activities which orders by name.
	 */
	public static final LiveActivityByNameComparator LIVE_ACTIVITY_BY_NAME_COMPARATOR = new LiveActivityByNameComparator();

	/**
	 * A comparator for live activity groups which orders by name.
	 */
	public static final LiveActivityGroupByNameComparator LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR = new LiveActivityGroupByNameComparator();

	/**
	 * A comparator for spaces which orders by name.
	 */
	public static final SpaceByNameComparator SPACE_BY_NAME_COMPARATOR = new SpaceByNameComparator();

	/**
	 * A comparator for active activities which orders by name.
	 */
	public static final UiLiveActivityByNameComparator UI_LIVE_ACTIVITY_BY_NAME_COMPARATOR = new UiLiveActivityByNameComparator();

	/**
	 * A comparator for space live activity groups which orders by name.
	 */
	public static final UiSpaceLiveActivityGroupByNameComparator UI_SPACE_LIVE_ACTIVITY_GROUP_BY_NAME_COMPARATOR = new UiSpaceLiveActivityGroupByNameComparator();

	/**
	 * A comparator for active controllers which orders by name.
	 */
	public static final ActiveControllerByNameComparator ACTIVE_CONTROLLER_BY_NAME_COMPARATOR = new ActiveControllerByNameComparator();

	/**
	 * A comparator for named scripts which orders by name.
	 */
	public static final NamedScriptByNameComparator NAMED_SCRIPT_BY_NAME_COMPARATOR = new NamedScriptByNameComparator();

	/**
	 * Take the given map and turn it into a sorted set of labeled values.
	 * 
	 * <p>
	 * The map key will be the label and the value will be the value.
	 * 
	 * @param metadata
	 *            the data to be rendered
	 * 
	 * @return a sorted list of labeled values.
	 */
	public static List<LabeledValue> getMetadataView(
			Map<String, Object> metadata) {
		List<LabeledValue> values = Lists.newArrayList();

		for (Entry<String, Object> entry : metadata.entrySet()) {
			values.add(new LabeledValue(entry.getKey(), entry.getValue()
					.toString()));
		}

		Collections.sort(values);

		return values;
	}

	/**
	 * A comparator for installed activity which orders by name.
	 * 
	 * @author Keith M. Hughes
	 */
	private static class SpaceControllerByNameComparator implements
			Comparator<SpaceController> {
		@Override
		public int compare(SpaceController o1, SpaceController o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}

	/**
	 * A comparator for activities which orders by name.
	 * 
	 * @author Keith M. Hughes
	 */
	private static class ActivityByNameComparator implements
			Comparator<Activity> {
		@Override
		public int compare(Activity o1, Activity o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}

	/**
	 * A comparator for live activities which orders by name.
	 * 
	 * @author Keith M. Hughes
	 */
	private static class LiveActivityByNameComparator implements
			Comparator<LiveActivity> {
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
	private static class LiveActivityGroupByNameComparator implements
			Comparator<LiveActivityGroup> {
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
	 * A comparator for active activities which orders by name.
	 * 
	 * @author Keith M. Hughes
	 */
	private static class UiLiveActivityByNameComparator implements
			Comparator<UiLiveActivity> {
		@Override
		public int compare(UiLiveActivity o1, UiLiveActivity o2) {
			return o1.getActivity().getName()
					.compareToIgnoreCase(o2.getActivity().getName());
		}
	}

	/**
	 * A comparator for UI live activity groups which orders by name.
	 * 
	 * @author Keith M. Hughes
	 */
	private static class UiSpaceLiveActivityGroupByNameComparator implements
			Comparator<UiSpaceLiveActivityGroup> {
		@Override
		public int compare(UiSpaceLiveActivityGroup o1,
				UiSpaceLiveActivityGroup o2) {
			return o1.getLiveActivityGroup().getName()
					.compareToIgnoreCase(o2.getLiveActivityGroup().getName());
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
			return o1.getController().getName()
					.compareToIgnoreCase(o2.getController().getName());
		}
	}

	/**
	 * A comparator for named scripts which orders by name.
	 * 
	 * @author Keith M. Hughes
	 */
	private static class NamedScriptByNameComparator implements
			Comparator<NamedScript> {
		@Override
		public int compare(NamedScript o1, NamedScript o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}
}
