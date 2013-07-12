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

package interactivespaces.service.audio.player.jukebox;

import interactivespaces.configuration.Configuration;
import interactivespaces.service.audio.player.AudioTrackPlayerFactory;

import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.logging.Log;

/**
 * A base {@link JukeboxOperation} which provides some support for
 * implementations.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseJukeboxOperation implements JukeboxOperation {

  /**
   * Configuration for the operations.
   */
  protected Configuration configuration;

  /**
   * factory for track players.
   */
  protected AudioTrackPlayerFactory trackPlayerFactory;

  /**
   * An executor service.
   */
  protected ScheduledExecutorService executor;

  /**
   * Listener for the operation.
   */
  protected AudioJukeboxListener listener;

  /**
   * Log where all should be written.
   */
  protected Log log;

  public BaseJukeboxOperation(Configuration configuration,
      AudioTrackPlayerFactory trackPlayerFactory, ScheduledExecutorService executor,
      AudioJukeboxListener listener, Log log) {
    this.configuration = configuration;
    this.trackPlayerFactory = trackPlayerFactory;
    this.executor = executor;
    this.listener = listener;
    this.log = log;
  }
}