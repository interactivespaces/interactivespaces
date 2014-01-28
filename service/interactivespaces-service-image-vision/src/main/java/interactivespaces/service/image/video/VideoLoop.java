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
   * A set of processors for the video frames.
   */
  private final List<VideoFrameProcessor<T>> processors = Lists.newCopyOnWriteArrayList();

  /**
   * Add a new frame processor to the loop.
   *
   * @param processor
   *          the new processor
   */
  public void addProcessor(VideoFrameProcessor<T> processor) {
    processors.add(processor);
  }

  /**
   * Remove a frame processor from the loop.
   *
   * <p>
   * Does nothing if the processor was never added.
   *
   * @param processor
   *          the processor to remove
   */
  public void removeProcessor(VideoFrameProcessor<T> processor) {
    processors.remove(processor);
  }

  /**
   * Notify all processors about a new video frame.
   *
   * @param frame
   *          the new frame
   */
  protected void notifyListenersNewVideoFrame(T frame) {
    for (VideoFrameProcessor<T> processor : processors) {
      processor.onNewVideoFrame(frame);
    }
  }
}
