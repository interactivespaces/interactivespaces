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

package interactivespaces.activity.speech;

import interactivespaces.activity.ActivityState;
import interactivespaces.activity.binary.NativeActivityRunner;
import interactivespaces.activity.impl.ros.BaseRosActivity;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.speech.synthesis.SpeechSynthesisService;
import interactivespaces.service.speech.synthesis.internal.festival.FestivalSpeechSynthesisService;
import interactivespaces.util.InteractiveSpacesUtilities;

import java.util.Map;

import org.ros.message.MessageListener;
import org.ros.message.interactivespaces_msgs.SpeechRequest;
import org.ros.node.Node;
import org.ros.node.topic.Subscriber;

import com.google.common.collect.Maps;

/**
 * The Speech activity.
 * 
 * @author Keith M. Hughes
 */
public class SpeechSynthesisActivity extends BaseRosActivity {

	public static final String CONFIGURATION_SPEECH_CONTROL_ROS_TOPIC_NAME = "speech.ros.topic.name";

	/**
	 * Control of the speech server.
	 */
	private NativeActivityRunner speechServer;

	private SpeechSynthesisService speechSynthesisService;

	@Override
	public void onActivityStartup() {
		Configuration configuration = getConfiguration();

		setupRosTopics(configuration);
		startSpeechService(configuration);
	}

	@Override
	public void onActivityCleanup() {
		if (speechServer != null) {
			speechServer.shutdown();
			speechServer = null;
		}

		if (speechSynthesisService != null) {
			speechSynthesisService.shutdown();
			speechSynthesisService = null;
		}
	}

	@Override
	public void checkActivityState() {
		if (speechServer != null) {
			if (!speechServer.isRunning()) {
				setActivityStatus(ActivityState.CRASHED,
						"Speech server no longer running");
			}
		}
	}

	@Override
	public void onActivityActivate() {
		speechSynthesisService.speak("Speech synthesis is activated", true);
	}

	@Override
	public void onActivityDeactivate() {
		speechSynthesisService.speak("Speech synthesis is deactivated", true);
	}

	/**
	 * @param configuration
	 * 
	 */
	protected void setupRosTopics(Configuration configuration) {
		Node node = getMainNode();
		Subscriber<SpeechRequest> subscriber = node
				.newSubscriber(
						configuration
								.getRequiredPropertyString(CONFIGURATION_SPEECH_CONTROL_ROS_TOPIC_NAME),
						"interactivespaces_msgs/SpeechRequest");
		subscriber.addMessageListener(new MessageListener<SpeechRequest>() {
			@Override
			public void onNewMessage(SpeechRequest request) {
				handleNewSpeechCommand(request);
			}
		});
	}

	/**
	 * Start up whatever speech service is being used, if necessary.
	 */
	private void startSpeechService(Configuration configuration) {
		speechServer = getController().getNativeActivityRunnerFactory()
				.newPlatformNativeActivityRunner(getLog());
		Map<String, Object> appConfig = Maps.newHashMap();
		appConfig
				.put(NativeActivityRunner.ACTIVITYNAME,
						configuration
								.getRequiredPropertyString("speech.server.executable.festival.linux"));

		String commandFlags = configuration
				.getRequiredPropertyString("speech.server.flags.festival.linux");

		appConfig.put(NativeActivityRunner.FLAGS, commandFlags);
		speechServer.configure(appConfig);
		speechServer.startup();

		InteractiveSpacesUtilities.delay(5000);

		speechSynthesisService = new FestivalSpeechSynthesisService("localhost", 1314);
		speechSynthesisService.startup();
	}

	/**
	 * Received new speech request.
	 * 
	 * @param request
	 *            The new request
	 */
	private void handleNewSpeechCommand(SpeechRequest request) {
		speechSynthesisService.speak(request.text, true);
	}
}
