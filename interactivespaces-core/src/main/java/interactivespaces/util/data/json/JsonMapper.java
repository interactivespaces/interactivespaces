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

package interactivespaces.util.data.json;

import interactivespaces.InteractiveSpacesException;

import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * A mapper for JSON.
 * 
 * <p>
 * This object is threadsafe so can be made static.
 * 
 * @author Keith M. Hughes
 */
public class JsonMapper {

	/**
	 * The JSON mapper.
	 */
	private static final ObjectMapper MAPPER;

	static {
		MAPPER = new ObjectMapper();
		MAPPER.getJsonFactory().enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
	}

	/**
	 * Parse an object string.
	 * 
	 * @param object
	 *            the JSON string to parse
	 * 
	 * @return the map, if it parsed corrected
	 * 
	 * @throws InteractiveSpacesException
	 *             the string did not parse properly
	 */
	public Map<String, Object> parseObject(String object) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) MAPPER.readValue(
					object, Map.class);
			return map;
		} catch (Exception e) {
			throw new InteractiveSpacesException("Could not parse JSON string",
					e);
		}
	}

	/**
	 * Take a map and write it as a string.
	 * 
	 * <p>
	 * Non 7-but ASCII characters will be escaped.
	 * 
	 * @param data
	 * 			the object to serialize as JSON
	 * 
	 * @return the string
	 */
	public String toString(Object data) {
		try {
			return MAPPER.writeValueAsString(data);
		} catch (Exception e) {
			throw new InteractiveSpacesException(
					"Could not serialize JSON object as string", e);
		}
	}
}
