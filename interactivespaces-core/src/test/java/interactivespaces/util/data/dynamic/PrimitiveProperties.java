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

/**
 * Dynamic object interface with primitive properties.
 * Primitive getters (except boolean getters) will throw a {@link NullPointerException}
 * if there's no corresponding value. A boolean getter will return {@code false}.
 *
 * @author Oleksandr Kelepko
 */
public interface PrimitiveProperties {
  void setPrimitiveBoolean(boolean d);

  boolean isPrimitiveBoolean();

  boolean getAnotherPrimitiveBoolean();

  void setPrimitiveInt(int d);

  int getPrimitiveInt();

  void setPrimitiveLong(long d);

  long getPrimitiveLong();

  void setPrimitiveDouble(double d);

  double getPrimitiveDouble();
}
