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
 * handle the reading of a collection of columns from a {@link ColumnSource}.
 *
 * @author Keith M. Hughes
 */
public interface ColumnSourceHandler {
	
	/**
	 * process a line for the CSV file.
	 * 
	 * @param lineReader The line reader which has the current line set.
	 */
	void processColumns(ColumnSource lineReader);

}
