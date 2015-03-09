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

/**
 * This interface contains primitive wrapper and {@link String} properties.
 *
 * @author Oleksandr Kelepko
 */
interface JavaBeanProperties extends GenericProperty<Integer> {

  Integer getAnotherInt();

  Long getLong();

  void setLong(Long l);

  Long getAnotherLong();

  Double getDouble();

  void setDouble(Double d);

  Double getAnotherDouble();

  Float getFloat();

  void setFloat(Float d);

  Float getAnotherFloat();

  Boolean getBoolean();

  void setBoolean(Boolean b);

  Boolean getAnotherBoolean();

  Boolean isTrue();

  String getString();

  void setString(String s);

  CharSequence getAnotherString();

  List<Integer> getIntList();

  void setIntList(List<Integer> list);

  List<Integer> getAnotherIntList();

  List<Long> getLongList();

  void setLongList(List<Long> list);

  List<Long> getAnotherLongList();

  List<Double> getDoubleList();

  void setDoubleList(List<Double> list);

  List<Double> getAnotherDoubleList();

  List<Float> getFloatList();

  void setFloatList(List<Float> list);

  List<Float> getAnotherFloatList();

  List<Boolean> getBooleanList();

  void setBooleanList(List<Boolean> list);

  List<Boolean> getAnotherBooleanList();

  List<String> getStringList();

  void setStringList(List<String> list);

  List<String> getAnotherStringList();
}
