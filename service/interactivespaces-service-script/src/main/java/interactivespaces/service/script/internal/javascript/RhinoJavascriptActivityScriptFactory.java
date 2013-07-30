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

package interactivespaces.service.script.internal.javascript;

import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.script.ActivityScriptWrapper;
import interactivespaces.service.script.ScriptSource;
import interactivespaces.service.script.internal.ActivityScriptFactory;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 *
 *
 * @author Keith M. Hughes
 */
public class RhinoJavascriptActivityScriptFactory implements ActivityScriptFactory {

  /**
   * The global scope we'll store the standard JavaScript objects and some of
   * our own global definitions in.
   */
  private Scriptable globalScope;

  @Override
  public void initialize() {
    try {
      // We use two contexts: a global one and a local one.
      // This means the expensive initStandardObjects()
      // call is done only once in the global context.
      //
      // A local context is necessary to make sure that
      // side-effects don't impact the run of a script
      // (e.g. global variables created, etc).
      Context cx = Context.enter();
      // cx.setCompileFunctionsWithDynamicScope(false);

      // This is an expensive operation and we only want
      // to do it once.
      globalScope = cx.initStandardObjects(null);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      Context.exit();
    }
  }

  @Override
  public ActivityScriptWrapper getActivity(String objectName, ScriptSource scriptSource,
      ActivityFilesystem activityFilesystem, Configuration configuration) {
    return new RhinoActivityScriptWrapper(globalScope, scriptSource, activityFilesystem,
        configuration);
  }

}
