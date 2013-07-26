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

package interactivespaces.util.concurrency;

import interactivespaces.InteractiveSpacesException;

/**
 * A loop which controls a {@link Updateable}.
 *
 * @author Keith M. Hughes
 */
public class UpdateableLoop extends CancellableLoop {

  /**
   * The object being updated.
   */
  private Updateable updateable;

  /**
   * Time in milliseconds to delay per loop.
   */
  private long delayTime;

  public UpdateableLoop(Updateable updateable) {
    this.updateable = updateable;
  }

  @Override
  protected void loop() throws InterruptedException {
    updateable.update();

    Thread.sleep(delayTime);
  }

  /**
   * Set a delay used between iterations.
   *
   * <p>
   * Will only be used if the delay is positive.
   *
   *
   * @param delayTime
   *          Number of milliseconds for the delay.
   *
   * @throws InteractiveSpacesException
   *           If loop is running.
   */
  public void setDelayTime(long delayTime) {
    if (!isRunning()) {
      this.delayTime = delayTime;
    } else {
      throw new InteractiveSpacesException("Can't change the delay time on a running loop");
    }
  }

  /**
   * Set the delay time using frames per second.
   *
   * @param fps
   *          The loop rate in frames per second.
   */
  public void setFrameRate(double fps) {
    setDelayTime((long) (1000.0 / fps));
  }
}
