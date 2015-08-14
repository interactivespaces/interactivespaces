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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.resource.TypedId;

/**
 * The base implementation of the {@link SpaceControllerRepository}. It provides common implementation.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseSpaceControllerRepository implements SpaceControllerRepository {

  @Override
  public SpaceController getSpaceControllerByTypedId(String typedIdString) throws InteractiveSpacesException {
    TypedId typedId = TypedId.newTypedID(TYPED_ID_TYPE_COMPONENT_SEPARATOR, TYPED_ID_TYPE_DEFAULT, typedIdString);
    switch (typedId.getType()) {
      case TYPED_ID_TYPE_ID:
        return getSpaceControllerById(typedId.getId());
      case TYPED_ID_TYPE_UUID:
        return getSpaceControllerByUuid(typedId.getId());
      default:
        throw SimpleInteractiveSpacesException.newFormattedException(
            "Unknown typed ID type %s while getting space controller", typedId.getType());
    }
  }
}
