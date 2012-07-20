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

package interactivespaces.domain.support;

import java.util.regex.Pattern;

/**
 * A validator for activity identifying names
 *
 * @author Keith M. Hughes
 */
public class ActivityIdentifyingNameValidator implements Validator {
	
	/**
	 * Pattern for the identifying name.
	 */
	public static final Pattern PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*");
	
	@Override
	public boolean validate(String candidate) {
		return PATTERN.matcher(candidate).matches();
	}
	
	@Override
	public String getDescription() {
		return "An identifying name must be of the form a.b.c.c\n" + 
				"where each section starts with a letter and\n" + 
				"continues with letters, digits, or underscores.";
	}
}
