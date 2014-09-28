package interactivespaces.controller.standalone.startup;

import interactivespaces.configuration.Configuration;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.activity.configuration.LiveActivityConfiguration;
import interactivespaces.controller.activity.wrapper.ActivityWrapper;
import interactivespaces.controller.activity.wrapper.ActivityWrapperFactory;
import interactivespaces.controller.client.node.ActiveControllerActivity;
import interactivespaces.controller.client.node.ActiveControllerActivityFactory;
import interactivespaces.controller.client.node.InternalActivityFilesystem;
import interactivespaces.controller.domain.InstalledLiveActivity;

/**
 * Activity factory used for standalone activities.
 */
public class StandaloneActiveControllerActivityFactory implements ActiveControllerActivityFactory {
  @Override
  public ActiveControllerActivity createActiveLiveActivity(String activityType,
      InstalledLiveActivity liapp, InternalActivityFilesystem activityFilesystem,
      LiveActivityConfiguration configuration, SpaceController controller) {
    return null;
  }

  @Override
  public ActiveControllerActivity newActiveActivity(InstalledLiveActivity installedActivity,
      InternalActivityFilesystem activityFilesystem, LiveActivityConfiguration configuration,
      SpaceController controller) {
    ActivityWrapper activityWrapper = null;
    return new ActiveControllerActivity(installedActivity, activityWrapper,
        activityFilesystem, configuration, controller);
  }

  @Override
  public String getConfiguredType(Configuration configuration) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void registerActivityWrapperFactory(ActivityWrapperFactory factory) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void unregisterActivityWrapperFactory(ActivityWrapperFactory factory) {
    throw new UnsupportedOperationException("Not implemented");
  }
}
