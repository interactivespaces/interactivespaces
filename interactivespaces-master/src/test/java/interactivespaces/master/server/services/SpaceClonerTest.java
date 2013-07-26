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

package interactivespaces.master.server.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import interactivespaces.domain.basic.Activity;
import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.GroupLiveActivity;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.basic.pojo.SimpleActivity;
import interactivespaces.domain.basic.pojo.SimpleActivityConfiguration;
import interactivespaces.domain.basic.pojo.SimpleConfigurationParameter;
import interactivespaces.domain.basic.pojo.SimpleLiveActivity;
import interactivespaces.domain.basic.pojo.SimpleLiveActivityGroup;
import interactivespaces.domain.basic.pojo.SimpleSpaceController;
import interactivespaces.domain.space.Space;
import interactivespaces.domain.space.pojo.SimpleSpace;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Tests for a {@link SpaceCloner}.
 *
 * @author Keith M. Hughes
 */
public class SpaceClonerTest {
  private static final String DESCRIPTION = "dumb description";

  private static final String ACTIVITY_3_NAME = "activity 3";

  private static final String ACTIVITY_2_NAME = "activity 2";

  private static final String ACTIVITY_1_NAME = "activity 1";

  private static final String CLONE_NAME_PREFIX = "I think I'm a clone now";

  private SpaceCloner cloner;

  @Mock
  private ActivityRepository activityRepository;

  private SpaceController controller1 = new SimpleSpaceController();

  private SpaceController controller2 = new SimpleSpaceController();

  private SpaceController controller3 = new SimpleSpaceController();

  private Activity activity1;
  private Activity activity2;
  private Activity activity3;

  Map<String, Object> metadata = Maps.newHashMap();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    Mockito.when(activityRepository.newActivityConfiguration()).thenAnswer(
        new Answer<ActivityConfiguration>() {
          public ActivityConfiguration answer(InvocationOnMock invocation) {
            return new SimpleActivityConfiguration();
          }
        });

    Mockito.when(activityRepository.newConfigurationParameter()).thenAnswer(
        new Answer<ConfigurationParameter>() {
          public ConfigurationParameter answer(InvocationOnMock invocation) {
            return new SimpleConfigurationParameter();
          }
        });

    Mockito.when(activityRepository.newLiveActivity()).thenAnswer(new Answer<LiveActivity>() {
      public LiveActivity answer(InvocationOnMock invocation) {
        return new SimpleLiveActivity();
      }
    });

    Mockito.when(activityRepository.newLiveActivityGroup()).thenAnswer(
        new Answer<LiveActivityGroup>() {
          public LiveActivityGroup answer(InvocationOnMock invocation) {
            return new SimpleLiveActivityGroup();
          }
        });

    Mockito.when(activityRepository.newSpace()).thenAnswer(new Answer<Space>() {
      public Space answer(InvocationOnMock invocation) {
        return new SimpleSpace();
      }
    });

    cloner = new SpaceCloner(activityRepository);

    activity1 = new SimpleActivity();
    activity1.setName(ACTIVITY_1_NAME);
    activity2 = new SimpleActivity();
    activity2.setName(ACTIVITY_2_NAME);
    activity3 = new SimpleActivity();
    activity3.setName(ACTIVITY_3_NAME);

    cloner.setNamePrefix(CLONE_NAME_PREFIX);

    metadata.put("foo", "bar");
    metadata.put("bletch", "spam");
  }

  /**
   * Copy a live activity. There is no controller map.
   */
  @Test
  public void testConfiguration() {
    ActivityConfiguration srcConfiguration = new SimpleActivityConfiguration();
    srcConfiguration.setDescription(DESCRIPTION);
    srcConfiguration.addParameter(new SimpleConfigurationParameter("foo", "bar"));
    srcConfiguration.addParameter(new SimpleConfigurationParameter("bletch", "spam"));

    ActivityConfiguration clonedConfiguration = cloner.cloneConfiguration(srcConfiguration);

    compareConfigurations(srcConfiguration, clonedConfiguration);
  }

  /**
   * Copy a live activity. There is no controller map.
   */
  @Test
  public void testLiveActivityCopyNoControllerMap() {
    ActivityConfiguration srcConfiguration = new SimpleActivityConfiguration();
    srcConfiguration.setDescription(DESCRIPTION);
    srcConfiguration.addParameter(new SimpleConfigurationParameter("foo", "bar"));
    srcConfiguration.addParameter(new SimpleConfigurationParameter("bletch", "spam"));

    LiveActivity srcLiveActivity = newLiveActivity(activity1, controller1);
    srcLiveActivity.setConfiguration(srcConfiguration);

    LiveActivity clonedLiveActivity = cloner.cloneLiveActivity(srcLiveActivity);

    compareLiveActivity(srcLiveActivity, clonedLiveActivity, controller1);
  }

  /**
   * Copy a live activity. make sure get same one twice.
   */
  @Test
  public void testLiveActivityCopyNoControllerMapCheckCache() {
    LiveActivity srcLiveActivity = newLiveActivity(activity1, controller1);

    LiveActivity clonedLiveActivity1 = cloner.cloneLiveActivity(srcLiveActivity);
    LiveActivity clonedLiveActivity2 = cloner.cloneLiveActivity(srcLiveActivity);

    assertEquals(clonedLiveActivity1, clonedLiveActivity2);
  }

  /**
   * Copy a live activity. There is a controller map, but not with the
   * controller the live activity has.
   */
  @Test
  public void testLiveActivityCopyControllerMapMissing() {
    Map<SpaceController, SpaceController> controllerMap = Maps.newHashMap();
    controllerMap.put(controller2, controller3);
    cloner.setControllerMap(controllerMap);

    LiveActivity srcLiveActivity = newLiveActivity(activity1, controller1);
    srcLiveActivity.setDescription(DESCRIPTION);
    srcLiveActivity.setMetadata(metadata);

    LiveActivity clonedLiveActivity = cloner.cloneLiveActivity(srcLiveActivity);
    compareLiveActivity(srcLiveActivity, clonedLiveActivity, controller1);
  }

  /**
   * Copy a live activity. There is a controller map which contains the live
   * activity's controller
   */
  @Test
  public void testLiveActivityCopyControllerMap() {
    Map<SpaceController, SpaceController> controllerMap = Maps.newHashMap();
    controllerMap.put(controller1, controller3);
    cloner.setControllerMap(controllerMap);

    LiveActivity srcLiveActivity = newLiveActivity(activity1, controller1);
    srcLiveActivity.setDescription(DESCRIPTION);
    srcLiveActivity.setMetadata(metadata);

    LiveActivity clonedLiveActivity = cloner.cloneLiveActivity(srcLiveActivity);
    compareLiveActivity(srcLiveActivity, clonedLiveActivity, controller3);
  }

  /**
   * Copy a live activity. There is a controller map which contains the live
   * activity's controller
   */
  @Test
  public void testLiveActivityGroupCopy() {
    LiveActivityGroup srcLiveActivityGroup =
        newLiveActivityGroup(Lists.newArrayList(newLiveActivity(activity1, controller1),
            newLiveActivity(activity2, controller2)));

    LiveActivityGroup clonedLiveActivityGroup = cloner.cloneLiveActivityGroup(srcLiveActivityGroup);
    compareLiveActivityGroup(srcLiveActivityGroup, clonedLiveActivityGroup);
  }

  /**
   * Copy a live activity. Make sure cache works
   */
  @Test
  public void testLiveActivityGroupCopyCache() {
    LiveActivityGroup srcLiveActivityGroup = new SimpleLiveActivityGroup();

    LiveActivityGroup clonedLiveActivityGroup1 =
        cloner.cloneLiveActivityGroup(srcLiveActivityGroup);
    LiveActivityGroup clonedLiveActivityGroup2 =
        cloner.cloneLiveActivityGroup(srcLiveActivityGroup);
    assertEquals(clonedLiveActivityGroup1, clonedLiveActivityGroup2);
  }

  /**
   * Copy a space. Make sure cache works
   */
  @Test
  public void testSpaceCopy() {
    LiveActivityGroup srcLiveActivityGroup1 =
        newLiveActivityGroup(Lists.newArrayList(newLiveActivity(activity1, controller1),
            newLiveActivity(activity2, controller2)));

    LiveActivityGroup srcLiveActivityGroup2 =
        newLiveActivityGroup(Lists.newArrayList(newLiveActivity(activity3, controller3)));

    Space srcSpace = new SimpleSpace();
    srcSpace.setDescription(DESCRIPTION);
    srcSpace.setMetadata(metadata);

    srcSpace.addActivityGroup(srcLiveActivityGroup1);

    Space srcSubspace = new SimpleSpace();
    srcSubspace.setDescription(DESCRIPTION);
    srcSubspace.setMetadata(metadata);
    srcSubspace.addActivityGroup(srcLiveActivityGroup2);

    srcSpace.addSpace(srcSubspace);

    Space clonedSpace = cloner.cloneSpace(srcSpace);

    compareSpaces(srcSpace, clonedSpace);
  }

  /**
   * Copy a space. Make sure cache works
   */
  @Test
  public void testSpaceCopyCache() {
    Space srcSpace = new SimpleSpace();

    Space clonedSpace1 = cloner.cloneSpace(srcSpace);
    Space clonedSpace2 = cloner.cloneSpace(srcSpace);
    assertEquals(clonedSpace1, clonedSpace2);
  }

  /**
   * @return
   */
  protected LiveActivity newLiveActivity(Activity activity, SpaceController controller) {
    LiveActivity liveActivity = new SimpleLiveActivity();
    liveActivity.setActivity(activity);
    liveActivity.setController(controller);
    liveActivity.setDescription(DESCRIPTION);
    liveActivity.setMetadata(metadata);

    return liveActivity;
  }

  /**
   * Create a new Live Activity Group
   *
   * @param liveActivities
   *          the live activities to go in the group
   *
   * @return the new group
   */
  private LiveActivityGroup newLiveActivityGroup(List<LiveActivity> liveActivities) {
    LiveActivityGroup srcLiveActivityGroup = new SimpleLiveActivityGroup();
    srcLiveActivityGroup.setDescription(DESCRIPTION);
    srcLiveActivityGroup.setMetadata(metadata);
    for (LiveActivity srcLiveActivity : liveActivities) {
      srcLiveActivityGroup.addActivity(srcLiveActivity);
    }
    return srcLiveActivityGroup;
  }

  /**
   * @param srcConfiguration
   *          the original
   * @param clonedConfiguration
   *          the clone
   */
  private void compareConfigurations(ActivityConfiguration srcConfiguration,
      ActivityConfiguration clonedConfiguration) {
    if (srcConfiguration == null) {
      if (clonedConfiguration != null) {
        fail();
      }

      return;
    } else {
      if (clonedConfiguration == null) {
        fail();
      }
    }

    assertEquals(srcConfiguration.getDescription(), clonedConfiguration.getDescription());

    Map<String, ConfigurationParameter> srcConfigurationMap = srcConfiguration.getParameterMap();
    Map<String, ConfigurationParameter> clonedConfigurationMap =
        clonedConfiguration.getParameterMap();
    assertEquals(srcConfigurationMap.size(), clonedConfigurationMap.size());
    for (Entry<String, ConfigurationParameter> entry : srcConfigurationMap.entrySet()) {
      ConfigurationParameter clonedParameter = clonedConfigurationMap.get(entry.getKey());
      assertNotNull(clonedParameter);
      assertEquals(entry.getValue().getName(), clonedParameter.getName());
      assertEquals(entry.getValue().getValue(), clonedParameter.getValue());
    }
  }

  /**
   * Compare two live activities to see if they were cloned properly
   *
   * @param srcLiveActivity
   *          the original
   * @param clonedLiveActivity
   *          the clone
   * @param controller
   *          the controller that the live activity's controller should have
   *          mapped to
   */
  private void compareLiveActivity(LiveActivity srcLiveActivity, LiveActivity clonedLiveActivity,
      SpaceController controller) {
    assertEquals(srcLiveActivity.getActivity(), clonedLiveActivity.getActivity());
    assertEquals(controller, clonedLiveActivity.getController());
    assertEquals(CLONE_NAME_PREFIX + " " + srcLiveActivity.getActivity().getName(),
        clonedLiveActivity.getName());
    assertEquals(DESCRIPTION, clonedLiveActivity.getDescription());
    assertEquals(metadata, clonedLiveActivity.getMetadata());

    compareConfigurations(srcLiveActivity.getConfiguration(), clonedLiveActivity.getConfiguration());
  }

  /**
   * Compare the live activities in two live activity groups to make sure they
   * are the same.
   *
   * @param srcLiveActivityGroup
   *          the original
   * @param clonedLiveActivityGroup
   *          the clone
   */
  protected void compareGroupLiveActivities(LiveActivityGroup srcLiveActivityGroup,
      LiveActivityGroup clonedLiveActivityGroup) {
    List<? extends GroupLiveActivity> srcGroupLiveActivities = srcLiveActivityGroup.getActivities();
    List<? extends GroupLiveActivity> clonedGroupLiveActivities =
        clonedLiveActivityGroup.getActivities();
    assertEquals(srcGroupLiveActivities.size(), clonedGroupLiveActivities.size());
    for (int i = 0; i < srcGroupLiveActivities.size(); ++i) {
      GroupLiveActivity srcGroupLiveActivity = srcGroupLiveActivities.get(i);
      GroupLiveActivity clonedGroupLiveActivity = clonedGroupLiveActivities.get(i);
      assertEquals(srcGroupLiveActivity.getDependency(), clonedGroupLiveActivity.getDependency());
      compareLiveActivity(srcGroupLiveActivity.getActivity(),
          clonedGroupLiveActivity.getActivity(), srcGroupLiveActivity.getActivity().getController());
    }
  }

  /**
   * @param srcLiveActivityGroup
   *          the original
   * @param clonedLiveActivityGroup
   *          the clone
   */
  private void compareLiveActivityGroup(LiveActivityGroup srcLiveActivityGroup,
      LiveActivityGroup clonedLiveActivityGroup) {
    assertEquals(DESCRIPTION, clonedLiveActivityGroup.getDescription());
    assertEquals(metadata, clonedLiveActivityGroup.getMetadata());

    compareGroupLiveActivities(srcLiveActivityGroup, clonedLiveActivityGroup);
  }

  /**
   * @param srcSpace
   *          the original
   * @param clonedSpace
   *          the clone
   */
  private void compareSpaces(Space srcSpace, Space clonedSpace) {
    assertEquals(srcSpace.getDescription(), clonedSpace.getDescription());
    assertEquals(srcSpace.getMetadata(), clonedSpace.getMetadata());

    List<? extends LiveActivityGroup> srcLiveActivityGroups = srcSpace.getActivityGroups();
    List<? extends LiveActivityGroup> clonedLiveActivityGroups = clonedSpace.getActivityGroups();
    assertEquals(srcLiveActivityGroups.size(), clonedLiveActivityGroups.size());
    for (int i = 0; i < srcLiveActivityGroups.size(); ++i) {
      compareLiveActivityGroup(srcLiveActivityGroups.get(i), clonedLiveActivityGroups.get(i));
    }

    List<? extends Space> srcSubspaces = srcSpace.getSpaces();
    List<? extends Space> clonedSubspaces = clonedSpace.getSpaces();
    assertEquals(srcSubspaces.size(), clonedSubspaces.size());
    for (int i = 0; i < srcSubspaces.size(); ++i) {
      compareSpaces(srcSubspaces.get(i), clonedSubspaces.get(i));
    }
  }
}
