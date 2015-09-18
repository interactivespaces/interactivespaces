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

package interactivespaces.bridge.message;

/**
 * An object that contains the source and the destination for a message bridge.
 *
 * @author Keith M. Hughes
 */
public class BridgeObject {

  /**
   * The source object for the bridge.
   */
  private Object src;

  /**
   * The destination object for the bridge.
   */
  private Object dst;

  /**
   * Construct a bridge object.
   *
   * @param src
   *          the source message
   * @param dst
   *          the destination message
   */
  public BridgeObject(Object src, Object dst) {
    this.src = src;
    this.dst = dst;
  }

  /**
   * Get the source message.
   *
   * @return the source message
   */
  public Object getSrc() {
    return src;
  }

  /**
   * Get the destination message.
   *
   * @return the destination message
   */
  public Object getDst() {
    return dst;
  }
}
