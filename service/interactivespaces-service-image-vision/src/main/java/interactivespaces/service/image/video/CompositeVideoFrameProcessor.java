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

import interactivespaces.InteractiveSpacesException;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.util.List;

/**
 * A video frame listener that gives a sequence of video frame listeners to be
 * handled one at a time.
 *
 * @param <T>
 *          the type for the video frame
 *
 * @author Keith M. Hughes
 */
public class CompositeVideoFrameProcessor<T> implements VideoFrameProcessor<T> {

  /**
   * The components of the composite listener.
   */
  private final List<VideoFrameProcessor<T>> components = Lists.newCopyOnWriteArrayList();

  /**
   * Logger for the listener.
   */
  private final Log log;

  /**
   * Construct a new composite listener.
   *
   * @param log
   *          the logger to use
   */
  public CompositeVideoFrameProcessor(Log log) {
    this.log = log;
  }

  /**
   * Add a new component to the listener.
   *
   * @param component
   *          the component to add
   */
  public void addComponent(VideoFrameProcessor<T> component) {
    components.add(component);
  }

  /**
   * Remove a component from the listener.
   *
   * <p>
   * Does nothing if the component was never added.
   *
   * @param component
   *          the component to remove
   */
  public void removeComponent(VideoFrameProcessor<T> component) {
    components.remove(component);
  }

  @Override
  public void startup() {
    List<VideoFrameProcessor<T>> componentsStarted = Lists.newArrayList();
    try {
      for (VideoFrameProcessor<T> component : components) {
        component.startup();
        componentsStarted.add(component);
      }
    } catch (Exception e) {
      log.error("Could not start composite video listener");

      shutdownComponents(componentsStarted);

      throw new InteractiveSpacesException("ECould not start composite video listener", e);
    }
  }

  @Override
  public void shutdown() {
    shutdownComponents(components);
  }

  @Override
  public T onNewVideoFrame(T frame) {
    // If an exception happens, let it go so that the caller handles it since no
    // need to process any other frames.
    T curFrame = frame;
    for (VideoFrameProcessor<T> component : components) {
      curFrame = component.onNewVideoFrame(curFrame);
    }

    return curFrame;
  }

  /**
   * Shutdown the list of components given.
   *
   * @param componentsToShutdown
   *          the components to shut down
   */
  private void shutdownComponents(List<VideoFrameProcessor<T>> componentsToShutdown) {
    for (VideoFrameProcessor<T> component : componentsToShutdown) {
      try {
        component.shutdown();
      } catch (Exception e) {
        log.error("Error while shutting down composite video listener", e);
      }
    }
  }
}
