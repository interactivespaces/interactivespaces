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

package interactivespaces.service.speech.synthesis.internal.osgi;

import interactivespaces.osgi.service.InteractiveSpacesServiceOsgiBundleActivator;
import interactivespaces.service.speech.synthesis.SpeechSynthesisService;
import interactivespaces.service.speech.synthesis.internal.freetts.FreeTtsSpeechSynthesisService;

/**
 * OSGi bundle activator for the speech service.
 *
 * @author Keith M. Hughes
 */
public class SpeechSynthesisServiceOsgiBundleActivator extends
    InteractiveSpacesServiceOsgiBundleActivator {

  @Override
  protected void allRequiredServicesAvailable() {
    SpeechSynthesisService speechSynthesisService = new FreeTtsSpeechSynthesisService();

    registerNewInteractiveSpacesService(speechSynthesisService);
  }
}
