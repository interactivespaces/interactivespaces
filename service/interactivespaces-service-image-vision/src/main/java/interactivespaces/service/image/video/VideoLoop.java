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

package interactivespaces.service.image.video;

import interactivespaces.util.concurrency.CancellableLoop;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * A loop for capturing video.
 *
 * @param <T>
 *          the type of video frame
 *
 * @author Keith M. Hughes
 */
public abstract class VideoLoop<T> extends CancellableLoop {

  /**
   * A list of listeners.
   */
  private final List<VideoFrameListener<T>> listeners = Lists.newCopyOnWriteArrayList();

  /**
   * Add a new frame listener to the loop.
   *
   * @param listener
   *          the new listener
   */
  public void addListener(VideoFrameListener<T> listener) {
    listeners.add(listener);
  }

  /**
   * Remove a frame listener from the loop.
   *
   * <p>
   * Does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  public void removeListener(VideoFrameListener<T> listener) {
    listeners.remove(listener);
  }

  /**
   * Notify all listeners about a new video frame.
   *
   * @param frame
   *          the new frame
   */
  protected void notifyListenersNewVideoFrame(T frame) {
    for (VideoFrameListener<T> listener : listeners) {
      listener.onNewVideoFrame(frame);
    }
  }

  /**
   * A listener for frames captured during with a {@link VideoLoop}.
   *
   * @param <T>
   *          the type of video frame
   *
   * @author Keith M. Hughes
   */
  public interface VideoFrameListener<T> {

    /**
     * A new frame has come in.
     *
     * @param frame
     *          the new frame
     */
    void onNewVideoFrame(T frame);
  }
}
