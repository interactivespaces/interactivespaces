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

package interactivespaces.resource;

/**
 * Typed IDs have a type of the ID and the actual content of the ID.
 *
 * @author Keith M. Hughes
 */
public class TypedId {

  /**
   * Parse the given Typed ID string.
   *
   * <p>
   * Typed IDs are of the form {@code type:id}, where {@code type} is the type of the ID, e.g. ID or UUID, and
   * {@code id} is the ID of the proper type.
   *
   *
   * @param separator
   *          the separator between the type and the ID
   * @param defaultType
   *          the default type if no separator
   * @param typedIdString
   *          the string to be parsed
   *
   * @return the typed ID
   */
  public static TypedId newTypedID(String separator, String defaultType, String typedIdString) {
    String type = defaultType;
    String id = typedIdString;
    int separatorPos = typedIdString.indexOf(separator);
    if (separatorPos != -1) {
      type = typedIdString.substring(0, separatorPos);
      id = typedIdString.substring(separatorPos + 1);
    }

    return new TypedId(type, id);
  }

  /**
   * The type of the ID.
   */
  private final String type;

  /**
   * The ID content of the ID.
   */
  private final String id;

  /**
   * Construct a new typed id.
   *
   * @param type
   *          the type of the ID
   * @param id
   *          the ID content of the ID
   */
  public TypedId(String type, String id) {
    this.type = type;
    this.id = id;
  }

  /**
   * Get the type of the ID.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Get the ID content of the ID.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }
}
