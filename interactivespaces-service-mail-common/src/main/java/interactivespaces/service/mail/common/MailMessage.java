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

package interactivespaces.service.mail.common;

import java.util.List;

/**
 * A mail message.
 * 
 * @author Keith M. Hughes
 */
public interface MailMessage {

	/**
	 * Get all the To addresses.
	 * 
	 * @return a potentially empty list of addresses
	 */
	List<String> getToAdresses();

	/**
	 * Get all the CC addresses.
	 * 
	 * @return a potentially empty list of addresses
	 */
	List<String> getCcAdresses();

	/**
	 * Get all the BCC addresses.
	 * 
	 * @return a potentially empty list of addresses
	 */
	List<String> getBccAdresses();

	/**
	 * Get the from address of the mail.
	 * 
	 * @return the from address
	 */
	String getFromAddress();

	/**
	 * Get the subject of the mail.
	 * 
	 * @return the subject
	 */
	String getSubject();

	/**
	 * Get the body of the message.
	 * 
	 * @return the complete body of the message.
	 */
	String getBody();
}
