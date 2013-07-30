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

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * A {@link ScriptEngineFactory} for Rhino Javascript.
 *
 * @author Keith M. Hughes
 */
public class RhinoJavascriptScriptEngineFactory implements ScriptEngineFactory {
  private static List<String> EXTENSIONS;
  private static List<String> NAMES;

  static {
    EXTENSIONS = new ArrayList<String>();
    EXTENSIONS.add("js");

    NAMES = new ArrayList<String>();
    NAMES.add("javascript");
    NAMES.add("rhino");
  }

  @Override
  public String getEngineName() {
    return "javascript";
  }

  @Override
  public String getEngineVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getExtensions() {
    // TODO(keith): Make unmodifiable
    return EXTENSIONS;
  }

  @Override
  public String getLanguageName() {
    return "javascript";
  }

  @Override
  public String getLanguageVersion() {
    return "2.5.1-R2";
  }

  @Override
  public String getMethodCallSyntax(String obj, String m, String... args) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getMimeTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getNames() {
    // TODO(keith): Make unmodifiable
    return NAMES;
  }

  @Override
  public String getOutputStatement(String toDisplay) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getParameter(String key) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getProgram(String... statements) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ScriptEngine getScriptEngine() {
    return new RhinoJavascriptScriptEngine();
  }

}
