/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.util;

/**
 * A collection of utilities for working with bytes
 * 
 * @author Keith M. Hughes
 */
public class ByteUtils {

	/**
	 * Get the hex string for the bytes.
	 * 
	 * <p>
	 * Only the lower 8 bits of each int is used for the byte.
	 * 
	 * <p>
	 * The bytes are assumed to be most significant byte first
	 * 
	 * @param ia
	 * 		the array of "bytes"
	 * 
	 * @return hex string for the bytes.
	 */
	public static String toHexString(int[] ia) {
		StringBuilder builder = new StringBuilder();

		for (int i : ia) {
			String istr = Integer.toHexString(i & 0xff);
			if (istr.length() < 2) {
				builder.append('0');
			}
			builder.append(istr);
		}

		return builder.toString();
	}

	/**
	 * Get the hex string for the bytes.
	 * 
	 * <p>
	 * The bytes are assumed to be most significant byte first
	 * 
	 * @param ba
	 * 		the array of bytes
	 * 
	 * @return hex string for the bytes.
	 */
	public static String toHexString(byte[] ba) {
		StringBuilder builder = new StringBuilder();

		for (byte b : ba) {
			String bstr = Integer.toHexString((int)b & 0xff);
			if (bstr.length() < 2) {
				builder.append('0');
			}
			builder.append(bstr);
		}

		return builder.toString();
	}
}
