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

package interactivespaces.controller.client.node.internal.osgi;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.configuration.ActivityConfiguration;
import interactivespaces.configuration.Configuration;
import interactivespaces.controller.activity.configuration.SimpleActivityConfiguration;
import interactivespaces.controller.activity.wrapper.ActivityWrapper;
import interactivespaces.controller.activity.wrapper.ActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.bridge.topic.TopicBridgeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.interactivespaces.InteractiveSpacesNativeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.osnative.NativeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.web.WebActivityWrapperFactory;
import interactivespaces.controller.client.node.ActiveControllerActivity;
import interactivespaces.controller.client.node.ActiveControllerActivityFactory;
import interactivespaces.controller.client.node.StandardSpaceController;
import interactivespaces.controller.domain.InstalledLiveActivity;

import java.util.Map;

import org.osgi.framework.BundleContext;

import com.google.common.collect.Maps;

/**
 * An {@link ActiveControllerActivityFactory} which works within an OSGi
 * container.
 * 
 * @author Keith M. Hughes
  */
public class OsgiActiveControllerActivityFactory implements
		ActiveControllerActivityFactory {

	/**
	 * OSGi bundle context the factory lives in.
	 */
	private BundleContext bundleContext;

	/**
	 * Mapping of activity types to wrapper factories for that type.
	 */
	private Map<String, ActivityWrapperFactory> activityWrapperFactories = Maps
			.newConcurrentMap();

	public OsgiActiveControllerActivityFactory(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		
		registerActivityWrapperFactory(new NativeActivityWrapperFactory());
		registerActivityWrapperFactory(new WebActivityWrapperFactory());
		registerActivityWrapperFactory(new TopicBridgeActivityWrapperFactory());

		registerActivityWrapperFactory(new InteractiveSpacesNativeActivityWrapperFactory(
				bundleContext));
	}

	@Override
	public ActiveControllerActivity createActiveActivity(
			String activityType, InstalledLiveActivity liapp,
			ActivityFilesystem activityFilesystem,
			SimpleActivityConfiguration configuration,
			StandardSpaceController controller) {

		ActivityWrapperFactory wrapperFactory = activityWrapperFactories
				.get(activityType.toLowerCase());
		if (wrapperFactory != null) {
			ActivityWrapper wrapper = wrapperFactory
					.createActivityWrapper(liapp, activityFilesystem,
							configuration, controller);

			ActiveControllerActivity app = new ActiveControllerActivity(
					liapp, wrapper, activityFilesystem, configuration,
					controller);

			return app;
		} else {
			throw new InteractiveSpacesException(String.format(
					"Unsupported activity type %s",
					activityType.toString()));
		}
	}

	@Override
	public ActiveControllerActivity createActiveActivity(
			InstalledLiveActivity liapp,
			ActivityFilesystem activityFilesystem,
			SimpleActivityConfiguration configuration,
			StandardSpaceController controller) {
		String type = getConfiguredType(configuration);

		return createActiveActivity(type, liapp, activityFilesystem,
				configuration, controller);
	}

	@Override
	public String getConfiguredType(Configuration configuration) {
		return configuration
				.getRequiredPropertyString(ActivityConfiguration.CONFIGURATION_ACTIVITY_TYPE);
	}

	@Override
	public void registerActivityWrapperFactory(
			ActivityWrapperFactory factory) {
		if (activityWrapperFactories.put(factory.getActivityType()
				.toLowerCase(), factory) != null) {
			throw new InteractiveSpacesException(String.format(
					"The %s %s is already registered",
					factory.getActivityType(),
					ActivityWrapperFactory.class.getName()));
		}
	}

	@Override
	public void unregisterActivityWrapperFactory(
			ActivityWrapperFactory factory) {
		String activityType = factory.getActivityType().toLowerCase();
		if (activityWrapperFactories.get(activityType) == factory) {
			activityWrapperFactories.remove(activityType);
		}
	}
}
