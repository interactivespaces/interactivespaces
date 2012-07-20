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

package interactivespaces.service.script;

/**
 * An {@link ScriptSource} which is just a string.
 * 
 * @author Keith M. Hughes
 */
public class StringScriptSource implements ScriptSource {

	/**
	 * The contents of the script.
	 */
	private String contents;

	/**
	 * {@code true} if the contents have been modified, false otherwise.
	 */
	private boolean modified;

	/**
	 * 
	 * @param contents
	 *            the initial contents of the source
	 */
	public StringScriptSource(String contents) {
		this.contents = contents;
		this.modified = true;
	}

	@Override
	public String getScriptContents() {
		modified = false;

		return contents;
	}

	@Override
	public boolean isModified() {
		return modified;
	}

	public void changeContents(String contents) {
		modified = true;
		this.contents = contents;
	}
}
