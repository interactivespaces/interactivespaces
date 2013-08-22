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

package interactivespaces.common;

/**
 * Enum of all the different upload channels -- used to route to the appropriate on-upload handler.
 * @author Trevor Pering
 */
public enum ResourceRepositoryUploadChannel {
  DATA_BUNDLE_UPLOAD;

  /**
   * Get the channel ID for this channel.
   *
   * @return
   *     channel id of the resource upload channel
   */
  public String getChannelId() {
    return toString();
  }
}
