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
package interactivespaces.util.ui.swing;

import interactivespaces.util.resource.ManagedResource;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Wrap a {@link JFrame} so that it becomes a {@link ManagedResource}.
 *
 * <p>
 * The frame will have its default closed operation set to
 * {@link WindowConstants.DISPOSE_ON_CLOSE}. Shutting down the managed resource
 * will post a window close event to the frame.
 *
 * @author Keith M. Hughes
 */
public class JFrameManagedResource implements ManagedResource {

  /**
   * The wrapped jframe.
   */
  private final JFrame frame;

  /**
   * Construct the managed resource.
   *
   * @param frame
   *          the frame to be managed
   */
  public JFrameManagedResource(JFrame frame) {
    this.frame = frame;
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  }

  @Override
  public void startup() {
    // Nothing to do
  }

  @Override
  public void shutdown() {
    WindowEvent wev = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
  }
}
