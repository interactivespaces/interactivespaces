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

import java.util.List;
import java.util.Map;

/**
 * This interface is meant to contain (directly and through inheritance) all kinds of properties a dynamic object
 * supports.
 *
 * @author Oleksandr Kelepko
 */
interface PossibleProperties extends JavaBeanProperties, InvalidProperties, PrimitiveProperties {
  List<JavaBeanProperties> getListOfW();

  Map<String, Object> getMap();


  JavaBeanProperties getInner();

  void setInner(JavaBeanProperties inner);

  JavaBeanProperties getAnotherInner();


  Map<String, JavaBeanProperties> getMapOfInner();

  void setMapOfInner(Map<String, JavaBeanProperties> map);

  Map<String, JavaBeanProperties> getAnotherMapOfInner();


  Map<String, List<Integer>> getMapOfInts();

  void setMapOfInts(Map<String, List<Integer>> map);

  Map<String, List<Integer>> getAnotherMapOfInts();

  List<List<Integer>> getIntLists();

  void setIntLists(List<List<Integer>> list);

  List<List<Integer>> getAnotherIntLists();
}
