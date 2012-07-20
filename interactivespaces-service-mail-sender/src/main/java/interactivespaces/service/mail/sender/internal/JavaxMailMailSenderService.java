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

package interactivespaces.service.mail.sender.internal;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.mail.common.MailMessage;
import interactivespaces.service.mail.sender.MailSenderService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * A {@link MailMessage} which uses Javax mail.
 * 
 * @author Keith M. Hughes
 */
public class JavaxMailMailSenderService implements MailSenderService {

	/**
	 * The default SMTP port.
	 */
	private static final String SMTP_PORT_DEFAULT = "25";

	/**
	 * Configuration property for SMTP host interactive spaces should use
	 */
	public static final String CONFIGURATION_MAIL_SMTP_HOST = "interactivespaces.mail.smtp.host";

	/**
	 * Configuration property for SMTP host port interactive spaces should use
	 */
	public static final String CONFIGURATION_MAIL_SMTP_PORT = "interactivespaces.mail.smtp.port";

	/**
	 * Space environment being run under.
	 */
	private InteractiveSpacesEnvironment spaceEnvironment;

	/**
	 * The Javamail mailerSession for sending messages.
	 */
	private Session mailerSession;

	@Override
	public void startup() {
		spaceEnvironment.getLog().info("Mail sending service starting up");

		Properties props = System.getProperties();

		// Setup mail server
		String smtpHost = spaceEnvironment.getSystemConfiguration()
				.getPropertyString(CONFIGURATION_MAIL_SMTP_HOST);
		if (smtpHost != null) {
			props.put("mail.smtp.host", smtpHost);
			props.put(
					"mail.smtp.port",
					spaceEnvironment.getSystemConfiguration()
							.getPropertyString(CONFIGURATION_MAIL_SMTP_PORT,
									SMTP_PORT_DEFAULT));

			mailerSession = Session.getDefaultInstance(props, null);
		} else {
			spaceEnvironment.getLog().warn(
					"Mail service not configured. No smptp host given");
		}

		spaceEnvironment.getServiceRegistry().registerService(SERVICE_NAME,
				this);
	}

	@Override
	public void sendMailMessage(MailMessage message) {
		if (mailerSession == null) {
			throw new InteractiveSpacesException("Mail service not configured");
		}
		
		// Define message
		MimeMessage msg = new MimeMessage(mailerSession);
		try {
			msg.setFrom(new InternetAddress(message.getFromAddress()));

			for (String address : message.getToAdresses()) {
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
						address));
			}

			for (String address : message.getCcAdresses()) {
				msg.addRecipient(Message.RecipientType.CC, new InternetAddress(
						address));
			}

			for (String address : message.getBccAdresses()) {
				msg.addRecipient(Message.RecipientType.BCC,
						new InternetAddress(address));
			}

			msg.setSubject(message.getSubject());
			msg.setText(message.getBody());

			// Send message
			Transport transport = mailerSession.getTransport("smtp");
			transport.connect();
			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();

			spaceEnvironment.getLog().info("Sent mail successfully");
		} catch (Throwable e) {
			throw new InteractiveSpacesException("Could not send mail", e);
		}
	}

	@Override
	public void shutdown() {
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void setSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		this.spaceEnvironment = spaceEnvironment;
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void bindSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		setSpaceEnvironment(spaceEnvironment);
	}

	/**
	 * @param spaceEnvironment
	 *            the spaceEnvironment to set
	 */
	public void unbindSpaceEnvironment(
			InteractiveSpacesEnvironment spaceEnvironment) {
		setSpaceEnvironment(null);
	}
}