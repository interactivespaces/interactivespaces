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

package interactivespaces.workbench.util;

import interactivespaces.InteractiveSpacesException;

import java.io.IOException;

/**
 * A runner for native commands.
 * 
 * @author Keith M. Hughes
 */
public class NativeCommandRunner {
	
	/**
	 * The status the process exited with.
	 */
	private int exitStatus;

	/**
	 * Execute the given command.
	 * 
	 * @param command
	 *            the command to execute
	 */
	public void execute(String command) {
		try {
			Process process = Runtime.getRuntime().exec(command);
			exitStatus = process.waitFor();
		} catch (Exception e) {
			throw new InteractiveSpacesException(String.format(
					"Could not execute command %s", command), e);
		}
	}
	
	/**
	 * Did the command exit successfully?
	 * 
	 * @return {@code true} if it was successful.
	 */
	public boolean isSuccess() {
		return exitStatus == 0;
	}
}
