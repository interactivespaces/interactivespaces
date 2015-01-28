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

package interactivespaces.liveactivity.runtime;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.configuration.ActivityConfiguration;
import interactivespaces.configuration.Configuration;
import interactivespaces.liveactivity.runtime.activity.wrapper.ActivityWrapper;
import interactivespaces.liveactivity.runtime.activity.wrapper.ActivityWrapperFactory;
import interactivespaces.liveactivity.runtime.configuration.LiveActivityConfiguration;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.resource.NamedVersionedResourceCollection;
import interactivespaces.resource.Version;
import interactivespaces.resource.VersionRange;

import org.apache.commons.logging.Log;

/**
 * An {@link LiveActivityRunnerFactory} with versioned factories.
 *
 * <p>
 * Factories without a version are given a version of {@link #DEFAULT_FACTORY_VERSION}. The highest version available
 * will always be used that meets the activity type's requested version range. No version constraints will give the
 * highest version available.
 *
 * @author Keith M. Hughes
 */
public class StandardLiveActivityRunnerFactory implements LiveActivityRunnerFactory {

  /**
   * Default version for wrapper factories.tClass().
   */
  public static final Version DEFAULT_FACTORY_VERSION = new Version(0, 0, 0);

  /**
   * The separator between an activity type and a potential version range for the type.
   */
  public static final char VERSION_RANGE_SEPARATOR = ';';

  /**
   * Collection of wrapper factories.
   */
  private final NamedVersionedResourceCollection<ActivityWrapperFactory> activityWrapperFactories =
      NamedVersionedResourceCollection.newNamedVersionedResourceCollection();

  /**
   * The logger for the factory.
   */
  private Log log;

  /**
   * Construct a new factory.
   *
   * @param log
   *          the logger
   */
  public StandardLiveActivityRunnerFactory(Log log) {
    this.log = log;
  }

  @Override
  public BasicLiveActivityRunner newLiveActivityRunner(String activityType, InstalledLiveActivity liveActivity,
      InternalLiveActivityFilesystem activityFilesystem, LiveActivityConfiguration configuration,
      LiveActivityRunnerListener liveActivityRunnerListener, LiveActivityRuntime liveActivityRuntime) {

    String bareActivityType = activityType;
    VersionRange versionRange = null;
    int versionRangePos = bareActivityType.indexOf(VERSION_RANGE_SEPARATOR);
    if (versionRangePos != -1) {
      versionRange = VersionRange.parseVersionRange(bareActivityType.substring(versionRangePos + 1));
      bareActivityType = bareActivityType.substring(0, versionRangePos);
    }
    bareActivityType = bareActivityType.toLowerCase();

    ActivityWrapperFactory wrapperFactory = null;
    if (versionRange != null) {
      wrapperFactory = activityWrapperFactories.getResource(bareActivityType, versionRange);
    } else {
      wrapperFactory = activityWrapperFactories.getHighestResource(bareActivityType);
    }

    if (wrapperFactory != null) {
      ActivityWrapper wrapper =
          wrapperFactory.newActivityWrapper(liveActivity, activityFilesystem, configuration, liveActivityRuntime);

      BasicLiveActivityRunner activeLiveActivity =
          new BasicLiveActivityRunner(liveActivity, wrapper, activityFilesystem, configuration,
              liveActivityRunnerListener, liveActivityRuntime);

      return activeLiveActivity;
    } else {
      String message =
          String
              .format("Unsupported activity type %s for activity %s", activityType.toString(), liveActivity.getUuid());
      liveActivityRuntime.getSpaceEnvironment().getLog().warn(message);

      throw new SimpleInteractiveSpacesException(message);
    }
  }

  @Override
  public BasicLiveActivityRunner newLiveActivityRunner(InstalledLiveActivity liveActivity,
      InternalLiveActivityFilesystem activityFilesystem, LiveActivityConfiguration configuration,
      LiveActivityRunnerListener liveActivityRunnerListener, LiveActivityRuntime liveActivityRuntime) {
    String type = getConfiguredType(configuration);

    return newLiveActivityRunner(type, liveActivity, activityFilesystem, configuration, liveActivityRunnerListener,
        liveActivityRuntime);
  }

  @Override
  public String getConfiguredType(Configuration configuration) {
    return configuration.getRequiredPropertyString(ActivityConfiguration.CONFIGURATION_ACTIVITY_TYPE);
  }

  @Override
  public void registerActivityWrapperFactory(ActivityWrapperFactory factory) {
    Version version = getFactoryVersion(factory);

    if (activityWrapperFactories.addResource(factory.getActivityType().toLowerCase(), version, factory) == null) {
      log.info(String.format("Registered activity wrapper factory version %s for activity type %s", version,
          factory.getActivityType()));
    } else {
      log.warn(String.format(
          "The %s version %s activity wrapper factory was already registered, the previous version has been replaced.",
          factory.getActivityType(), version));
    }
  }

  @Override
  public void unregisterActivityWrapperFactory(ActivityWrapperFactory factory) {
    Version version = getFactoryVersion(factory);

    String activityType = factory.getActivityType().toLowerCase();
    if (activityWrapperFactories.getResource(activityType, new VersionRange(version)) == factory) {
      activityWrapperFactories.removeResource(activityType, version);
    }
  }

  /**
   * Get the version of the factory.
   *
   * @param factory
   *          the factory
   *
   * @return the version to use
   */
  private Version getFactoryVersion(ActivityWrapperFactory factory) {
    // No version gets a version of 0.0.0.
    Version version = factory.getVersion();
    if (version == null) {
      version = DEFAULT_FACTORY_VERSION;
    }
    return version;
  }
}
