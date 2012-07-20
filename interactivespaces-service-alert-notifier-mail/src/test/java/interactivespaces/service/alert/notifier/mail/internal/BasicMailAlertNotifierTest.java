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

package interactivespaces.service.alert.notifier.mail.internal;

import static org.junit.Assert.assertEquals;
import interactivespaces.configuration.Configuration;
import interactivespaces.service.alert.AlertNotifier;
import interactivespaces.service.alert.AlertService;
import interactivespaces.service.mail.common.MailMessage;
import interactivespaces.service.mail.sender.MailSenderService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

/**
 * Unit tests for the {@link BasicMailAlertNotifier}.
 * 
 * @author Keith M. Hughes
 */
public class BasicMailAlertNotifierTest {
	private BasicMailAlertNotifier notifier;

	private InteractiveSpacesEnvironment spaceEnvironment;
	private MailSenderService mailSender;
	private AlertService alertService;
	private Configuration configuration;

	@Before
	public void setup() {
		configuration = Mockito.mock(Configuration.class);
		spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);
		Mockito.when(spaceEnvironment.getSystemConfiguration()).thenReturn(
				configuration);

		mailSender = Mockito.mock(MailSenderService.class);
		alertService = Mockito.mock(AlertService.class);

		notifier = new BasicMailAlertNotifier();
		notifier.setSpaceEnvironment(spaceEnvironment);
		notifier.setAlertService(alertService);
		notifier.setMailSenderService(mailSender);
	}

	/**
	 * Make sure it gets properly registered with the alert service.
	 */
	@Test
	public void testAlertServiceRegistration() {
		Mockito.verify(alertService, Mockito.times(1)).registerAlertNotifier(
				notifier);
		Mockito.verify(alertService, Mockito.never()).unregisterAlertNotifier(
				Mockito.any(AlertNotifier.class));
	}

	/**
	 * Make sure it gets properly unregistered with the alert service.
	 */
	@Test
	public void testAlertServiceUnRegistration() {
		notifier.setAlertService(null);
		Mockito.verify(alertService, Mockito.times(1)).registerAlertNotifier(
				notifier);
		Mockito.verify(alertService, Mockito.times(1)).unregisterAlertNotifier(
				notifier);
	}

	/**
	 * Email with default values
	 */
	@Test
	public void testSendingMailDefaults() {
		String fromAddress = "snidley.whiplash@gmail.com";
		Mockito.when(
				configuration
						.getPropertyString(BasicMailAlertNotifier.CONFIGURATION_INTERACTIVESPACES_SERVICE_ALERT_NOTIFIER_MAIL_FROM))
				.thenReturn(fromAddress);
		String toAddress = "dudley.doright@gmail.com";
		Mockito.when(
				configuration
						.getPropertyString(BasicMailAlertNotifier.CONFIGURATION_INTERACTIVESPACES_SERVICE_ALERT_NOTIFIER_MAIL_TO))
				.thenReturn(toAddress);
		String subject = "But Nell...";
		Mockito.when(
				configuration
						.getPropertyString(BasicMailAlertNotifier.CONFIGURATION_INTERACTIVESPACES_SERVICE_ALERT_NOTIFIER_MAIL_SUBJECT))
				.thenReturn(subject);

		String alertType = "kiwi";
		String id = "orange";
		String message = "mango";

		ArgumentCaptor<MailMessage> argument = ArgumentCaptor
				.forClass(MailMessage.class);

		notifier.notify(alertType, id, message);

		Mockito.verify(mailSender).sendMailMessage(argument.capture());

		assertEquals(fromAddress, argument.getValue().getFromAddress());
		assertEquals(toAddress, argument.getValue().getToAdresses().get(0));
		assertEquals(subject, argument.getValue().getSubject());
		assertEquals(message, argument.getValue().getBody());
	}

	/**
	 * Email with values specific to alert types.
	 * 
	 * <p>
	 * Also have multiple to addresses
	 */
	@Test
	public void testSendingMailByAlertType() {
		String alertType = "kiwi";
		String id = "orange";
		String message = "mango";

		String fromAddress = "elmer.fudd@gmail.com";
		Mockito.when(
				configuration
						.getPropertyString(BasicMailAlertNotifier.CONFIGURATION_INTERACTIVESPACES_SERVICE_ALERT_NOTIFIER_MAIL_FROM
								+ "." + alertType)).thenReturn(fromAddress);
		String toAddress1 = "bugs.bunny@gmail.com";
		String toAddress2 = "road.runner@gmail.com";
		Mockito.when(
				configuration
						.getPropertyString(BasicMailAlertNotifier.CONFIGURATION_INTERACTIVESPACES_SERVICE_ALERT_NOTIFIER_MAIL_TO
								+ "." + alertType)).thenReturn(
				"  " + toAddress1 + "     " + toAddress2);
		String subject = "Wascally wabbit...";
		Mockito.when(
				configuration
						.getPropertyString(BasicMailAlertNotifier.CONFIGURATION_INTERACTIVESPACES_SERVICE_ALERT_NOTIFIER_MAIL_SUBJECT
								+ "." + alertType)).thenReturn(subject);

		ArgumentCaptor<MailMessage> argument = ArgumentCaptor
				.forClass(MailMessage.class);

		notifier.notify(alertType, id, message);

		Mockito.verify(mailSender).sendMailMessage(argument.capture());

		assertEquals(fromAddress, argument.getValue().getFromAddress());
		assertEquals(Lists.newArrayList(toAddress1, toAddress2), argument
				.getValue().getToAdresses());
		assertEquals(subject, argument.getValue().getSubject());
		assertEquals(message, argument.getValue().getBody());
	}
}
