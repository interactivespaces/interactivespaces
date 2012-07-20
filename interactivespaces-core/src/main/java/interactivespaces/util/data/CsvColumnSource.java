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

package interactivespaces.util.data;

/**
 * A {@link ColumnSource} for CSV data.
 * 
 * @author Keith M. Hughes
 */
public class CsvColumnSource implements ColumnSource {
	
	/**
	 * The current line being processed.
	 */
	private String line;

	/**
	 * Current position in the line.
	 */
	private int pos = 0;

	/**
	 * Buffer for building up the data.
	 */
	private StringBuilder builder = new StringBuilder();

	/**
	 * Set the line to be processed.
	 * 
	 * @param line
	 *            The line to be processed.
	 */
	public void setLine(String line) {
		this.line = line;
		pos = 0;
	}

	@Override
	public String getColumn() {
		builder.setLength(0);

		char c = line.charAt(pos);
		boolean quoted = c == '"';
		if (quoted)
			pos++;
		for (; pos < line.length(); pos++) {
			c = line.charAt(pos);
			if (c == ',' && !quoted) {
				break;
			}

			if (c == '"') {
				if (quoted) {
					// We're quoted, either a double quote, or we are
					// ending. Check.
					int npos = pos + 1;
					if (npos < line.length()) {
						c = line.charAt(npos);
						if (c == '"') {
							builder.append(c);
							pos = npos;
							continue;
						} else {
							// Not a quoted quote, so must be at end of
							// quote

							// skip over quote
							pos++;

							break;
						}
					} else {
						// No next character, so is final quote
						break;
					}
				}
			}

			builder.append(c);
		}

		// Either there are more characters in line and we reached an end
		// character, so
		// need to move on, or this doesn't matter as we are at the end of
		// the string.
		pos++;

		return new String(builder.toString());
	}

}
