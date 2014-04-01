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

package interactivespaces.util.uuid;

import interactivespaces.util.resource.ManagedResource;

/**
 * A generator for UUIDs.
 *
 * <p>
 * Should only be one per VM.
 *
 * @author Keith M. Hughes
 */
public interface UuidGenerator extends ManagedResource {

  /**
   * The UUID namespace for space controllers.
   *
   * <p>
   * Comes from generating the name based UUID from
   * {@code http://interactive-spaces.org/objects/spacecontroller}.
   */
  String NAMESPACE_SPACE_CONTROLLER = "d06715fb-3529-31a7-9d50-f8bc98a98dc";

  /**
   * Create a UUID. This UUID will be a random one.
   *
   * @return the new UUID
   */
  String newUuid();

  /**
   * Create a new name based UUID.
   *
   * @param name
   *        the name for the UUID
   *
   * @return
   */
  String newNameUuid(String namespace, String name);
}
