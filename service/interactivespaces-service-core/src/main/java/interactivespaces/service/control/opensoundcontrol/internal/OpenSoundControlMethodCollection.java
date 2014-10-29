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

package interactivespaces.service.control.opensoundcontrol.internal;

import interactivespaces.service.control.opensoundcontrol.OpenSoundControlMethod;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlServerPacket;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.util.List;

/**
 * A collection of OSC methods.
 *
 * <p>
 * This class is not thread safe. The callers need to protect it.
 *
 * @author Keith M. Hughes
 */
public class OpenSoundControlMethodCollection {

  /**
   * The methods.
   */
  private List<OpenSoundControlMethod> methods = Lists.newArrayList();

  /**
   * Add a new method to the collection.
   *
   * @param method
   *          the method to add
   */
  public void addMethod(OpenSoundControlMethod method) {
    methods.add(method);
  }

  /**
   * Remove a method from the collection.
   *
   * <p>
   * Does nothing if the method is not in the collection.
   *
   * @param method
   *          the method to remove
   */
  public void removeMethod(OpenSoundControlMethod method) {
    methods.remove(method);
  }

  /**
   * Handle a packet.
   *
   * @param packet
   *          the packet to handle
   * @param log
   *          a logger to use
   */
  public void handlePacket(OpenSoundControlServerPacket packet, Log log) {
    for (OpenSoundControlMethod method : methods) {
      try {
        method.invoke(packet);
      } catch (Throwable e) {
        log.error("An Open Sound Control method has failed", e);
      }
    }
  }
}
