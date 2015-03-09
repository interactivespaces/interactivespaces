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

/**
 * Utilities for binding JSON parsed to maps with the Java interfaces.
 * The main idea is to work with an object of a given well-defined type,
 * while the state of the object (values of its properties) are stored in a map
 * that is a result of parsing a JSON object and/or may be serialized to JSON.
 * This JSON-orientedness implies some limitations and peculiarities:
 * <ul>
 *   <li>Supported types are most of the primitive types, String, List, Map, Object.
 *   <li>Maps can only have String keys.
 *   <li>Lists can have elements of any supported type.
 *   <li>Conforming to Java type system may require some transformations, e.g.,
 *   Integers/Longs and Strings "NaN", "Infinity", "-Infinity" to Doubles/Floats;
 *   Maps to dynamic objects, etc.
 * </ul>
 *
 * <p>
 * Implementation details:
 * <ul>
 *   <li>Two dynamic objects are considered equal if they implement the same interface,
 *   and their backing maps are equal.
 *   <li>Because of the implicit transformations that occur on method invocations,
 *   two dynamic objects may be not equal even if values of the properties returned
 *   from the corresponding methods are equal.<br>
 *   For example, if a dynamic object interface has a property of type Long:
 *   <pre>{@code
 *     public interface Example {
 *       Long getProperty();
 *     }
 *     }
 *   </pre>
 *   And one object is created with state {@code {"property": 1}} (value of type Integer),
 *   and another one with state {@code {"property": 1L}} (value of type Long),
 *   these two objects will not be equal even though getProperty() will return 1L
 *   for each of them.
 *
 *   <li>Primitive getters (except {@code boolean} ones) will throw a {@link NullPointerException}
 *   if there is no corresponding value in the backing map.
 *   This is similar to un-boxing null value.
 *   <li>Primitive boolean getters will return {@code false} if there is no value.
 *   <li>If a value in the map cannot be converted to the target type, a {@link ClassCastException}
 *   will be thrown. This also means that {@code null} value can be returned without exceptions
 *   from a getter with any (even unsupported) return type (for primitive types - see above).
 *   <li>Wildcard types in Maps and Lists are deliberately not supported.
 *   <li>Generally, if method's signature is not supported, an {@link UnsupportedOperationException}
 *   will be thrown.
 * </ul>
 *
 * <p>
 * {@link InterfaceMap} supports following methods in dynamic objects:
 * <ul>
 *   <li>{@link java.lang.Object#hashCode()} and {@link java.lang.Object#toString()}
 *   which are delegated to the backing map.
 *   <li>{@link java.lang.Object#equals(java.lang.Object)} that compares interfaces
 *   implemented by two dynamic objects and the corresponding backing maps.
 *   <li>{@link Boolean} getters - methods that return {@code Boolean} or {@code boolean}
 *   and whose names start with 'is' followed by capitalized property name,
 *   and which have no parameters.
 *   <li>general getters - methods whose return type is any of the supported types
 *   and whose name starts with 'get', and which have no parameters.
 *   <li>setters - methods whose return type is {@link Void} or {@code void},
 *   and which have exactly one parameter.
 * </ul>
 *
 * <p>
 * Use generic interfaces for dynamic objects judiciously. Consider following interfaces:
 * <pre>{@code
 * interface GenericProperty<T> {
 *   T getValue();
 * }
 * interface LongProperty extends GenericProperty<Long> {
 *   // Long getValue();
 * }
 * }</pre>
 * For compiler the method in the subinterface seems to exist, but not for the runtime.
 * Thus, the return type is erased and is seen as {@code Object}, so no conversions will occur,
 * which may lead to a ClassCastException. If, however, the method is explicitly overriden
 * (here - uncommented), everything will work fine.
 */
package interactivespaces.util.data.dynamic;
