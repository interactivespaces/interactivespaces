/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.util.sampling;

import interactivespaces.util.resource.ManagedResource;
import interactivespaces.util.sampling.SampledDataSequence.SampledDataFrame;

import org.apache.commons.logging.Log;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Send a sampled data sequence out as a series of frames.
 *
 * @author Keith M. Hughes
 */
public class SampledDataSequencePlayer implements ManagedResource {

  /**
   * The data to be sent.
   */
  private final SampledDataSequence sequence;

  /**
   * The object which will actually transmit the samples.
   */
  private final SampledDataSequenceSampleSender sender;

  /**
   * The executor service for sending the data.
   */
  private final ScheduledExecutorService executorService;

  /**
   * The future for the sending task.
   */
  private Future<?> runningFuture;

  /**
   * The logger to use.
   */
  private final Log log;

  /**
   * Construct a sampled data sequence player.
   *
   * @param sequence
   *          the sequence to play
   * @param sender
   *          the player of the data
   * @param executorService
   *          the executor service to get a thread for the player
   * @param log
   *          the logger to use
   */
  public SampledDataSequencePlayer(SampledDataSequence sequence, SampledDataSequenceSampleSender sender,
      ScheduledExecutorService executorService, Log log) {
    this.sequence = sequence;
    this.sender = sender;
    this.executorService = executorService;
    this.log = log;
  }

  @Override
  public void startup() {
    // Nothing to do during startup as this should be started manually.
  }

  @Override
  public void shutdown() {
    stopPlayback();
  }

  /**
   * Play the frames in sequence.
   *
   * @throws InterruptedException
   *           the player has been interrupted
   */
  private void play() throws InterruptedException {

    List<SampledDataFrame> frames = sequence.getFrames();
    int maxFrames = frames.size() - 1;

    while (!Thread.interrupted()) {
      log.info("Starting playback of sampled data");
      SampledDataFrame currentFrame = frames.get(0);
      int[] samples = currentFrame.getSamples();
      sender.sendSampledData(currentFrame.getSource(), Arrays.copyOf(samples, samples.length));
      SampledDataFrame previousFrame;
      for (int frame = 1; !Thread.interrupted() && frame < maxFrames; frame++) {
        previousFrame = currentFrame;
        currentFrame = frames.get(frame);

        long delay = currentFrame.getTimestamp() - previousFrame.getTimestamp();
        if (delay > 0) {
          Thread.sleep(delay);
        }

        samples = currentFrame.getSamples();
        sender.sendSampledData(currentFrame.getSource(), Arrays.copyOf(samples, samples.length));
      }
      log.info("Ending playback of sampled data");
    }
  }

  /**
   * Start playing the data.
   *
   * <p>
   * Can be called more than once, only the first time will start sending.
   */
  public synchronized void startPlayback() {
    if (runningFuture == null) {
      runningFuture = executorService.submit(new Runnable() {
        @Override
        public void run() {
          try {
            play();
          } catch (InterruptedException e) {
            log.info("Playback of sample data frames interrupted");
          } catch (Exception e) {
            log.error("Error while sending sampled data frame", e);
          }
        }
      });
    }
  }

  /**
   * Stop sending data.
   *
   * <p>
   * Can be called multiple times.
   */
  public synchronized void stopPlayback() {
    if (runningFuture != null) {
      runningFuture.cancel(true);
      runningFuture = null;
    }
  }

  /**
   * The data sender.
   *
   * @author Keith M. Hughes
   */
  public interface SampledDataSequenceSampleSender {

    /**
     * Send data from a source.
     *
     * @param source
     *          name of the source
     * @param samples
     *          the samples for the source in the current frame
     */
    void sendSampledData(String source, int[] samples);
  }
}
