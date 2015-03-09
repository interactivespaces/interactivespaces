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

package interactivespaces.util.data.dynamic;

import interactivespaces.util.data.json.JsonMapper;

import java.util.List;
import java.util.Map;

/**
 * Methods with signatures that are not supported by the {@link InterfaceMap}.
 *
 * @author Oleksandr Kelepko
 */
public interface InvalidProperties {
  /**
   * Method name without a property name.
   */
  Integer get();

  /**
   * Unsupported type of elements.
   */
  List<JsonMapper> getListOfNonInterfaceType();

  void getNativeVoid();

  Void getVoid();

  Integer size();

  String isNotAJavaBeanProperty();

  String setterIsInvalid(String param);

  void setTooManyParameters(String param, String param2);

  void setNoParameters();

  String getTooManyParameters(String param);

  Map<String, ? extends Integer> getMapWithWildcards();

  Map<Integer, Integer> getMapWithNonStringKeys();

  Map getRawMap();

  List<? extends Integer> getListWithWildcards();

  List getRawList();
}
