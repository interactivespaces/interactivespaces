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

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
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

import java.util.Collections;
import java.util.List;

/**
 * A {@link TwitterConnection} using Twitter4J.
 *
 * @author Keith M. Hughes
 */
public class Twitter4jTwitterConnection implements TwitterConnection {

  /**
   * The character used by the twitter query mechanism for specifying a hashtag.
   */
  public static final String TWITTER_SYMBOL_HASHTAG = "#";

  /**
   * The Interactive Spaces API key for the connection.
   */
  private String apiKey;

  /**
   * The Interactive Spaces API key secret for the connection.
   */
  private String apiKeySecret;

  /**
   * The user access token for connection.
   */
  private String userAccessToken;

  /**
   * The user access token secret for the connection.
   */
  private String userAccessTokenSecret;

  /**
   * The listeners for the connection.
   */
  private List<TwitterConnectionListener> listeners;

  /**
   * The twitter connection.
   */
  private AsyncTwitter connection;

  /**
   * The logger for this endpoint.
   */
  private Log log;

  /**
   * Construct a twitter connection.
   *
   * @param apiKey
   *          the API key for the connection
   * @param apiKeySecret
   *          the API secret for the connection
   * @param userAccessToken
   *          the user access token for the connection
   * @param userAccessTokenSecret
   *          the user access token secret for the connection
   * @param log
   *          the logger for this endpoint
   */
  public Twitter4jTwitterConnection(String apiKey, String apiKeySecret, String userAccessToken,
      String userAccessTokenSecret, Log log) {
    this.apiKey = apiKey;
    this.apiKeySecret = apiKeySecret;
    this.userAccessToken = userAccessToken;
    this.userAccessTokenSecret = userAccessTokenSecret;

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
        log.error("Exception during Twitter connection communication", e);
      }
    };

    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(true).setOAuthConsumerKey(apiKey).setOAuthConsumerSecret(apiKeySecret)
        .setOAuthAccessToken(userAccessToken).setOAuthAccessTokenSecret(userAccessTokenSecret).setUseSSL(true);

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
      throw new InteractiveSpacesException("Could not get screen name from Twitter connection", e);
    }
  }

  @Override
  public void updateStatus(String status) {
    connection.updateStatus(status);
  }

  @Override
  public void addHashTagSearch(String tag, String since) {
    Query query = new Query(TWITTER_SYMBOL_HASHTAG + tag);
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
   *          the result of the Twitter query
   */
  public void handleSearchResult(QueryResult queryResult) {
    String query = queryResult.getQuery();
    for (Status status : queryResult.getTweets()) {
      for (TwitterConnectionListener listener : listeners) {
        listener.onMessage(this, query, status.getUser().getScreenName(), status.getText());
      }
    }
  }
}
