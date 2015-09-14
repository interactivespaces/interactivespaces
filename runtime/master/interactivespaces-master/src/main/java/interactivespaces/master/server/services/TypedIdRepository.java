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

package interactivespaces.master.server.services;

/**
 * A repository that makes use of typed IDs for some of its retrieval.
 *
 * @author Keith M. Hughes
 */
public interface TypedIdRepository {

  /**
   * The Typed ID type for a standard ID.
   */
  String TYPED_ID_TYPE_ID = "id";

  /**
   * The Typed ID type for a UUID.
   */
  String TYPED_ID_TYPE_UUID = "uuid";

  /**
   * The Typed ID type default when none is specified.
   */
  String TYPED_ID_TYPE_DEFAULT = TYPED_ID_TYPE_ID;

  /**
   * The Typed ID separator between the type and the ID.
   */
  String TYPED_ID_TYPE_COMPONENT_SEPARATOR = ":";
}
