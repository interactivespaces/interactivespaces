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

package interactivespaces.master.ui.internal.web;

/**
 * A value with a label.
 *
 * @author Keith M. Hughes
 */
public class LabeledValue implements Comparable<LabeledValue> {
	
	/**
	 * The label.
	 */
	private String label;
	
	/**
	 * The value.
	 */
	private String value;

	public LabeledValue(String label, String value) {
		this.label = label;
		this.value = value;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	@Override
	public int compareTo(LabeledValue o) {
		return getLabel().compareToIgnoreCase(o.getLabel());
	}
	
	
}
