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

package interactivespaces.service.comm.twitter.internal.twitter4j;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.comm.twitter.TwitterConnection;
import interactivespaces.service.comm.twitter.TwitterConnectionListener;

import java.util.Collections;
import java.util.List;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.conf.ConfigurationBuilder;

import com.google.common.collect.Lists;

/**
 * A {@link TwitterConnection} for XMPP using the Smack library.
 * 
 * @author Keith M. Hughes
 */
public class Twitter4jTwitterConnection implements TwitterConnection {

	/**
	 * Consumer key for the connection.
	 */
	private String consumerKey;

	/**
	 * Consumer secret for the connection.
	 */
	private String consumerSecret;

	/**
	 * Access token for connection.
	 */
	private String accessToken;

	/**
	 * Access token secret for the connection.
	 */
	private String accessTokenSecret;

	/**
	 * The listeners for the connection.
	 */
	private List<TwitterConnectionListener> listeners;

	/**
	 * The twitter connection.
	 */
	private AsyncTwitter connection;

	/**
	 * Construct a twitter connection.
	 * 
	 * @param consumerKey
	 *            the consumer key for the connection
	 * @param consumerSecret
	 *            the consumer secret for the connection
	 * @param accessToken
	 *            the access token for the connection
	 * @param accessTokenSecret
	 *            the access token secret for the connection
	 */
	public Twitter4jTwitterConnection(String consumerKey,
			String consumerSecret, String accessToken, String accessTokenSecret) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;

		listeners = Lists.newArrayList();
		listeners = Collections.synchronizedList(listeners);
	}

	@Override
	public void startup() {
		TwitterListener listener = new TwitterAdapter() {
			@Override
			public void searched(QueryResult queryResult) {
				handleSearchResult(queryResult);
			}

			@Override
			public void onException(TwitterException e, TwitterMethod method) {
				e.printStackTrace();
			}
		};

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
				.setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(accessToken)
				.setOAuthAccessTokenSecret(accessTokenSecret);

		// The factory instance is re-useable and thread safe.
		AsyncTwitterFactory factory = new AsyncTwitterFactory(cb.build());
		connection = factory.getInstance();
		connection.addListener(listener);
	}

	@Override
	public void shutdown() {
		if (connection != null) {
			connection.shutdown();
			connection = null;
		}
	}

	@Override
	public boolean isConnected() {
		return connection != null;
	}

	@Override
	public String getUser() {
		try {
			return connection.getScreenName();
		} catch (Exception e) {
			throw new InteractiveSpacesException(
					"Could not get screen name from Twitter connection", e);
		}
	}

	@Override
	public void updateStatus(String status) {
		connection.updateStatus(status);
	}

	@Override
	public void addHashTagSearch(String tag, String since) {
		Query query = new Query("#" + tag);
		query.setSince(since);
		connection.search(query);
	}

	@Override
	public void addListener(TwitterConnectionListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(TwitterConnectionListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Handle a Twitter search result.
	 * 
	 * @param queryResult
	 */
	public void handleSearchResult(QueryResult queryResult) {
		String query = queryResult.getQuery();
		for (Status status : queryResult.getTweets()) {
			for (TwitterConnectionListener listener : listeners) {
				listener.onMessage(this, query, status.getUser()
						.getScreenName(), status.getText());
			}
		}
	}
}
