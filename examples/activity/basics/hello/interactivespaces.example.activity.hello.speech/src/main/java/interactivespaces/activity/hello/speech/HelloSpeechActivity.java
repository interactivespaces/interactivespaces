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

package interactivespaces.activity.hello.speech;

import interactivespaces.activity.impl.BaseActivity;
import interactivespaces.service.speech.synthesis.SpeechSynthesisPlayer;
import interactivespaces.service.speech.synthesis.SpeechSynthesisService;

import java.util.Map;

/**
 * A simple Hello Speech synthesis activity.
 * 
 * <p>
 * This will speak during various activity lifecycle events.
 * 
 * @author Keith M. Hughes
 */
public class HelloSpeechActivity extends BaseActivity {

	/**
	 * The speech player.
	 */
	private SpeechSynthesisPlayer speechPlayer;

	@Override
	public void onActivitySetup() {
		SpeechSynthesisService speechSynthesisService = getSpaceEnvironment()
				.getServiceRegistry().getRequiredService(
						SpeechSynthesisService.SERVICE_NAME);

		speechPlayer = speechSynthesisService.newPlayer();

		addManagedResource(speechPlayer);
	}

	@Override
	public void onActivityStartup() {
		speechPlayer.speak("The hello speech example is started up", true);
	}

	@Override
	public void onActivityActivate() {
		speechPlayer.speak("The hello speech example is activated", true);
	}

	@Override
	public void onActivityDeactivate() {
		speechPlayer.speak("The hello speech example is deactivated", true);
	}
}
