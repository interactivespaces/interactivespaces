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

package interactivespaces.service.script.internal.osgi;

import interactivespaces.osgi.service.InteractiveSpacesServiceOsgiBundleActivator;
import interactivespaces.service.script.ScriptService;
import interactivespaces.service.script.internal.JavaxScriptScriptService;

/**
 * An OSGI bundle activator for the script service.
 *
 * @author Keith M. Hughes
 */
public class OsgiScriptServiceActivator extends InteractiveSpacesServiceOsgiBundleActivator {

  @Override
  protected void allRequiredServicesAvailable() {
    JavaxScriptScriptService scriptService = new JavaxScriptScriptService();

    registerNewInteractiveSpacesService(scriptService);

    registerOsgiService(ScriptService.class.getName(), scriptService);
  }
}
