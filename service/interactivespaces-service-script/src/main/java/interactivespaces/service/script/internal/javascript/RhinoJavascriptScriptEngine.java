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

import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * A {@link ScriptEngine} for Rhino Javascript.
 *
 * @author Keith M. Hughes
 */
public class RhinoJavascriptScriptEngine implements ScriptEngine {

  @Override
  public Bindings createBindings() {
    return new SimpleBindings();
  }

  @Override
  public Object eval(String script) throws ScriptException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object eval(Reader reader) throws ScriptException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object eval(String script, ScriptContext context) throws ScriptException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object eval(Reader reader, ScriptContext context) throws ScriptException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object eval(String script, Bindings n) throws ScriptException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object eval(Reader reader, Bindings n) throws ScriptException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object get(String key) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Bindings getBindings(int scope) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ScriptContext getContext() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ScriptEngineFactory getFactory() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void put(String key, Object value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setBindings(Bindings bindings, int scope) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setContext(ScriptContext context) {
    // TODO Auto-generated method stub

  }

}
