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

package interactivespaces.configuration;

import interactivespaces.InteractiveSpacesException;

/**
 * A wrapper for configurations which makes them unmodifiable.
 *
 * @author Keith M. Hughes
 */
public class ReadOnlyConfiguration implements Configuration {
	/**
	 * The configuration which is wrapped.
	 */
	private Configuration wrapped;

	public ReadOnlyConfiguration(Configuration wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public boolean containsProperty(String property) {
		return wrapped.containsProperty(property);
	}

	@Override
	public String getPropertyString(String property) {
		return wrapped.getPropertyString(property);
	}

	@Override
	public String getPropertyString(String property, String defaultValue) {
		return wrapped.getPropertyString(property, defaultValue);
	}

	@Override
	public String getRequiredPropertyString(String property) {
		return wrapped.getRequiredPropertyString(property);
	}

	@Override
	public Integer getPropertyInteger(String property, Integer defaultValue) {
		return wrapped.getPropertyInteger(property, defaultValue);
	}

	@Override
	public Long getPropertyLong(String property, Long defaultValue) {
		return wrapped.getPropertyLong(property, defaultValue);
	}

	@Override
	public Double getPropertyDouble(String property, Double defaultValue) {
		return wrapped.getPropertyDouble(property, defaultValue);
	}

	@Override
	public Boolean getPropertyBoolean(String property, Boolean defaultValue) {
		return wrapped.getPropertyBoolean(property, defaultValue);
	}

	@Override
	public String evaluate(String expression) {
		return wrapped.evaluate(expression);
	}

	@Override
	public void setValue(String property, String value) {
		throw new InteractiveSpacesException("Cannot modify configuration");
	}

	@Override
	public void setParent(Configuration parent) {
		throw new InteractiveSpacesException("Cannot modify configuration");
	}

	@Override
	public Configuration getParent() {
		return wrapped.getParent();
	}

	@Override
	public String findValueLocally(String property) {
		return wrapped.findValueLocally(property);
	}

	@Override
	public boolean containsPropertyLocally(String property) {
		return wrapped.containsPropertyLocally(property);
	}

	@Override
	public void clear() {
		throw new InteractiveSpacesException("Cannot modify configuration");
	}
}
