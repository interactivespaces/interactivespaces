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

import java.util.Map;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.script.Script;
import interactivespaces.service.script.ScriptSource;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * A {@link Script} which uses a {@link ScriptEngine}. This means it is interpreted every time.
 *
 * @author Keith M. Hughes
 */
public class ScriptEngineScript implements Script {
	
	/**
	 * The engine which will interpret the script.
	 */
	private ScriptEngine engine;
	
	/**
	 * The source of the script.
	 */
	private ScriptSource source;

	public ScriptEngineScript(ScriptEngine engine, ScriptSource source) {
		this.engine = engine;
		this.source = source;
	}

	@Override
	public Object eval(Map<String, Object> bindings) {
		Bindings myBindings = new SimpleBindings(bindings);
		try {
			return engine.eval(source.getScriptContents(), myBindings);
		} catch (ScriptException e) {
			throw new InteractiveSpacesException("Could not evaluate script", e);
		}
	}
	
	
}
