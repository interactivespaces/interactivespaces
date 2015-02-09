/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime.standalone;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * A collection of {@link StandaloneLiveActivityInformation}.
 *
 * @author Keith M. Hughes
 */
public class StandaloneLiveActivityInformationCollection {

  /**
   * The live activity information in the collection, mapped by UUID.
   */
  private Map<String, StandaloneLiveActivityInformation> liveActivityInformation = Maps.newHashMap();

  /**
   * Add in new information to the collection.
   *
   * @param info
   *          the new information
   */
  public void addInformation(StandaloneLiveActivityInformation info) {
    liveActivityInformation.put(info.getUuid(), info);
  }

  /**
   * Get all UUIDs for all activities.
   *
   * @return all UUIDs for all activities
   */
  public List<String> getAllActivityUuids() {
    return Lists.newArrayList(liveActivityInformation.keySet());
  }

  /**
   * Get all info for all activities.
   *
   * @return all UUIDs for all activities
   */
  public List<StandaloneLiveActivityInformation> getAllActivityInformation() {
    return Lists.newArrayList(liveActivityInformation.values());
  }

  /**
   * Get the live activity information for the specified UUID.
   *
   * @param uuid
   *          the UUID for the live activity
   *
   * @return the live activity information
   *
   * @throws InteractiveSpacesException
   *           there was no information for that UUID
   */
  public StandaloneLiveActivityInformation getLiveActivityInformation(String uuid) throws InteractiveSpacesException {
    StandaloneLiveActivityInformation info = liveActivityInformation.get(uuid);
    if (info == null) {
      SimpleInteractiveSpacesException.throwFormattedException("Cannot retrieve live activity with UUID %s", uuid);
    }

    return info;
  }

}
