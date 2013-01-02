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

import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;
import interactivespaces.service.speech.synthesis.SpeechSynthesisPlayer;
import interactivespaces.service.speech.synthesis.SpeechSynthesisService;

import java.util.Map;

/**
 * An example Speech synthesis activity.
 * 
 * @author Keith M. Hughes
 */
public class SpeechSynthesisExampleActivity extends BaseRoutableRosActivity {

	/**
	 * The speech player.
	 */
	private SpeechSynthesisPlayer speechPlayer;

	@Override
	public void onActivityStartup() {
		SpeechSynthesisService speechSynthesisService = getSpaceEnvironment()
				.getServiceRegistry().getRequiredService(
						SpeechSynthesisService.SERVICE_NAME);

		speechPlayer = speechSynthesisService.newPlayer();

		addManagedResource(speechPlayer);
	}

	@Override
	public void onActivityActivate() {
		speechPlayer.speak("Speech synthesis is activated", true);
	}

	@Override
	public void onActivityDeactivate() {
		speechPlayer.speak("Speech synthesis is deactivated", true);
	}

	@Override
	public void onNewInputJson(String channelName, Map<String, Object> message) {
		if (isActivated() && "speech".equals(channelName)) {
			String toSpeak = (String) message.get("message");
			if (toSpeak != null) {
				speechPlayer.speak(toSpeak, true);
			}
		}
	}
}
