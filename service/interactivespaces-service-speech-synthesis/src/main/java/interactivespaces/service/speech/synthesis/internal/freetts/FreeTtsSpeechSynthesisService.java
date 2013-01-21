/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.service.speech.synthesis.internal.freetts;

import interactivespaces.service.speech.synthesis.SpeechSynthesisPlayer;
import interactivespaces.service.speech.synthesis.SpeechSynthesisService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

import com.sun.speech.freetts.VoiceManager;

/**
 * A speech synthesis service based on FreeTTS.
 *
 * @author Keith M. Hughes
 */
public class FreeTtsSpeechSynthesisService  implements SpeechSynthesisService{

	/**
	 * The voice manager for getting voices.
	 */
	private VoiceManager voiceManager;
	
	/**
	 * Spave environment for the service.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;
	
	@Override
	public void startup() {
		System.setProperty("freetts.voices","com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
		voiceManager = VoiceManager.getInstance();
	}

	@Override
	public void shutdown() {
		// Nothing to do
	}

	@Override
	public SpeechSynthesisPlayer newPlayer(Log log) {
		return new FreeTtsSpeechSynthesisPlayer(voiceManager, spaceEnvironment.getExecutorService(), log);
	}

	@Override
	public SpeechSynthesisPlayer newPlayer() {
		return newPlayer(spaceEnvironment.getLog());
	}

	@Override
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}
}
