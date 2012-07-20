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

package interactivespaces.service.alert.notifier.mail.internal.osgi;

import interactivespaces.service.alert.AlertService;
import interactivespaces.service.alert.internal.BasicAlertService;
import interactivespaces.service.alert.notifier.mail.internal.BasicMailAlertNotifier;
import interactivespaces.service.mail.sender.MailSenderService;
import interactivespaces.system.InteractiveSpacesEnvironment;

/**
 * A stub for starting a {@link BasicAlertService} under OSGi.
 * 
 * @author Keith M. Hughes
 */
public class OsgiMailAlertNotifier extends BasicMailAlertNotifier {

	/**
	 * @param alertService
	 *            the alert service to use
	 */
	public void bindAlertService(AlertService alertService) {
		setAlertService(alertService);
	}

	/**
	 * @param alertService
	 *            the alert service which should no longer be used
	 */
	public void unbindAlertService(AlertService alertService) {
		setAlertService(null);
	}

	/**
	 * @param mailSenderService
	 *            the mail sender service to use
	 */
	public void bindMailSenderService(MailSenderService mailSenderService) {
		setMailSenderService(mailSenderService);
	}

	/**
	 * @param mailSenderService
	 *            the mail sender service which should no longer be used
	 */
	public void unbindMailSenderService(MailSenderService mailSenderService) {
		setMailSenderService(null);
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment to use
	 */
	public void bindSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		setSpaceEnvironment(spaceEnvironment);
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment which should no longer be used
	 */
	public void unbindSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		setSpaceEnvironment(null);
	}
}
