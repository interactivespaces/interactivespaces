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

package interactivespaces.util.io;

import interactivespaces.InteractiveSpacesException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 * Read a file and process each line individually.
 *
 * @author Keith M. Hughes
 */
public class FileLineReader {
	/**
	 * Read through the contents of the file and process each line from the file
	 * using the supplied {@link LineReaderHandler}.
	 * 
	 * @param source The file to read.
	 * @param handler The handler which will process each line.
	 */
	public void process(File source, LineReaderHandler handler) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(source));

			String line;
			while ((line = reader.readLine()) != null) {
				handler.processLine(line);
			}
		} catch (Exception e) {
			throw new InteractiveSpacesException("Unable to read file " + source, e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// Don't care. Closing.
				}
		}
	}
}
