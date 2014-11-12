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

import interactivespaces.service.control.opensoundcontrol.OpenSoundControlIncomingMessage;
import interactivespaces.service.control.opensoundcontrol.OpenSoundControlMethod;

import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;

import java.util.Map;

/**
 * Dispatch messages to an appropriate Open Sound Control method.
 *
 * <p>
 * This class is threadsafe.
 *
 * @param <M>
 *      the type of incoming messages
 *
 * @author Keith M. Hughes
 */
public class OpenSoundControlMethodDispatcher<M extends OpenSoundControlIncomingMessage> {

  /**
   * The collections of methods.
   */
  private Map<String, OpenSoundControlMethodCollection<M>> collections = Maps.newHashMap();

  /**
   * The methods for handling unhandled messages.
   */
  private OpenSoundControlMethodCollection<M> unknownMessageMethods = new OpenSoundControlMethodCollection<M>();

  /**
   * The logger to use.
   */
  private Log log;

  /**
   * Construct a new dispatcher.
   *
   * @param log
   *          the logger to use
   */
  public OpenSoundControlMethodDispatcher(Log log) {
    this.log = log;
  }

  /**
   * Add in a new method into the dispatcher.
   *
   * @param oscAddress
   *          the address the method will respond to
   * @param method
   *          the method to be added
   */
  public synchronized void addMethod(String oscAddress, OpenSoundControlMethod<M> method) {
    OpenSoundControlMethodCollection<M> collection = collections.get(oscAddress);
    if (collection == null) {
      collection = new OpenSoundControlMethodCollection<M>();
      collections.put(oscAddress, collection);
    }

    collection.addMethod(method);
  }

  /**
   * Remove a method from the dispatcher.
   *
   * @param oscAddress
   *          the address the method will respond to
   * @param method
   *          the method to be removed
   */
  public synchronized void removeMethod(String oscAddress, OpenSoundControlMethod<M> method) {
    OpenSoundControlMethodCollection<M> collection = collections.get(oscAddress);
    if (collection != null) {
      collection.removeMethod(method);
    }
  }

  /**
   * Register a method for unknown OSC messages.
   *
   * <p>
   * Unknown OSC messages for this dispatcher are defined to be ones that did not have an explicit address registered
   * for handling them.
   *
   * @param method
   *          the method
   */
  public synchronized void addUnknownMessageMethod(OpenSoundControlMethod<M> method) {
    unknownMessageMethods.addMethod(method);
  }

  /**
   * Remove a method for unknown messages.
   *
   * <p>
   * Does nothing if the method has not been registered.
   *
   * @param method
   *          the method for the addressed messages
   */
  public synchronized void removeUnknownMessageMethod(OpenSoundControlMethod<M> method) {
    unknownMessageMethods.removeMethod(method);
  }

  /**
   * Handle an incoming OSC message.
   *
   * @param message
   *          the message to be handled
   */
  public synchronized void handleIncomingMessage(M message) {
    OpenSoundControlMethodCollection<M> collection = collections.get(message.getAddress());
    if (collection != null) {
      collection.handleMessage(message, log);
    } else {
      log.warn(String.format("Got unhandled OSC message with address %s", message.getAddress()));

      unknownMessageMethods.handleMessage(message, log);
    }
  }
}
