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

package interactivespaces.service.script.internal;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.script.Script;
import interactivespaces.service.script.ScriptSource;

import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * A {@link Script} for compiled scripts.
 *
 * @author Keith M. Hughes
 */
public class CompiledScriptScript implements Script {

  /**
   * The engine for compiling the scripts.
   */
  private Compilable compileEngine;

  /**
   * Source for the script.
   */
  private ScriptSource scriptSource;

  /**
   * The compiled script.
   */
  private CompiledScript script;

  public CompiledScriptScript(Compilable compileEngine, ScriptSource scriptSource) {
    this.scriptSource = scriptSource;

    try {
      script = compileEngine.compile(scriptSource.getScriptContents());
    } catch (ScriptException e) {
      throw new InteractiveSpacesException("Could not compile script", e);
    }

  }

  @Override
  public Object eval(Map<String, Object> bindings) {
    Bindings myBindings = new SimpleBindings(bindings);
    try {
      if (scriptSource.isModified()) {
        script = compileEngine.compile(scriptSource.getScriptContents());
      }

      return script.eval(myBindings);
    } catch (ScriptException e) {
      throw new InteractiveSpacesException("Could not evaluate script", e);
    }
  }
}
