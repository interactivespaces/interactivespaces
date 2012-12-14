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

package interactivespaces.service.speech.synthesis.internal.festival;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.speech.synthesis.SpeechSynthesisService;
import interactivespaces.service.speech.synthesis.internal.festival.client.Request;
import interactivespaces.service.speech.synthesis.internal.festival.client.RequestListener;
import interactivespaces.service.speech.synthesis.internal.festival.client.Session;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * A speech synthesis service based on Festival.
 * 
 * @author Keith M. Hughes
 */
public class FestivalSpeechSynthesisService implements SpeechSynthesisService {
	
	/**
	 * Host the festival server is running on.
	 */
	private String host;

	/**
	 * Port the festival host is listening on.
	 */
	private int port;

	private RequestListener requestHandler;

	private Session session;

	/**
	 * 
	 * @param host
	 *            Host the festival server is running on.
	 * @param port
	 *            Port the festival host is listening on.
	 */
	public FestivalSpeechSynthesisService(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public void startup() {

		session = null;

		try {
			session = new Session(host, port);
		} catch (UnknownHostException e) {
			throw new InteractiveSpacesException(
					"Festival client has unknown host", e);
		} catch (IOException e) {
			throw new InteractiveSpacesException(
					"Festival client can't connect", e);
		}

		session.initialise();

		requestHandler = new RequestHandler();
	}

	/* (non-Javadoc)
	 * @see interactivespaces.service.speech.synthesis.internal.festival.SpeechSynthesisService#shutdown()
	 */
	@Override
	public void shutdown() {
		session.terminate(true);
	}

	/* (non-Javadoc)
	 * @see interactivespaces.service.speech.synthesis.internal.festival.SpeechSynthesisService#sendRequest(java.lang.String, boolean)
	 */
	@Override
	public void speak(String request, boolean sync) {
		String textCommand = String.format("(SayText \"%s\")", request);

		Request r = session.request(textCommand, requestHandler);
		if (sync)
			while (!r.isFinished())
				r.waitForUpdate();

	}

	/**
	 * Festival client request handler.
	 * 
	 * @author Keith M. Hughes
	 * @since Dec 8, 2011
	 */
	private static class RequestHandler implements RequestListener {
		public void requestRunning(Request request) {
			System.out.format("Request %s running\n", request.command);
		}

		public void requestResult(Request request, Object result) {
			System.out.format("Request %s, result %s\n", request.command,
					result.toString());
		}

		public void requestError(Request request, String message) {
			System.out.format("Request %s, Error %s\n", request.command,
					message);
		}

		public void requestFinished(Request request) {
			System.out.format("Request %s finished\n", request.command);
		}
	}

};
