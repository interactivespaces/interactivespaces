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

import static java.lang.reflect.Proxy.getInvocationHandler;
import static java.lang.reflect.Proxy.isProxyClass;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Dynamic object factory. Given an interface and a map, creates an object with that interface
 * with state (JavaBean properties) stored in the map.
 * <p/>
 * In current implementation calls to {@code hashCode()} and {@code toString()} are delegated to the backing map.
 * Two dynamic objects are equal if they are of the same type (implement the same interface)
 * and have the same state (backing maps are equal).
 * <p/>
 * Primitive getters will throw an exception if the corresponding value is {@code null}.
 * One exception is primitive {@code boolean} getter, which in this case will return {@code false}.
 *
 * @author Oleksandr Kelepko
 */
public final class InterfaceMap {
  /**
   * Prevent instantiation of the utility class.
   */
  private InterfaceMap() {
  }

  /**
   * Reflectively creates an instance of a given interface, that is backed by a given map.
   * The interface may contain JavaBean properties (getters and/or setters) of the following types:
   * <ul>
   * <li>{@link java.lang.Integer} and {@code int}</li>
   * <li>{@link java.lang.Long} and {@code long}</li>
   * <li>{@link java.lang.Float} and {@code float}</li>
   * <li>{@link java.lang.Double} and {@code double}</li>
   * <li>{@link java.lang.Boolean} and {@code boolean}</li>
   * <li>{@link java.lang.String}</li>
   * <li>another JavaBean interface that meets these requirements</li>
   * <li>{@link java.util.List} of elements of any of the supported types (no wildcards)</li>
   * <li>{@link java.util.Map} with {link String} keys and values of any of the supported types (no wildcards)</li>
   * </ul>
   *
   * <p>
   * List getters return an immutable (possibly transformed) snapshot of a list in the backing map.
   *
   * @param interfaceClass
   *          type of the object to create
   * @param backingMap
   *          backing map, the source of values
   * @param <T>
   *          type of the object to create
   *
   * @return instance of the {@code interfaceClass}, backed by {@code backingMap}
   *
   * @throws java.lang.IllegalArgumentException
   *           if {@code interfaceClass} is not an interface
   * @throws java.lang.NullPointerException
   *           if any argument is {@code null}
   */
  @SuppressWarnings("unchecked")
  public static <T> T createInstance(Class<T> interfaceClass, Map<String, Object> backingMap) {
    return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[] { interfaceClass },
        new BackingMapInvocationHandler(backingMap, interfaceClass));
  }

  /**
   * Create a new instance of the given class with an empty backing map.
   *
   * @param interfaceClass
   *          class of object to create
   * @param <T>
   *          type of object
   *
   * @return new dynamic object instance with empty backing map
   */
  public static <T> T createInstance(Class<T> interfaceClass) {
    Map<String, Object> backingMap = Maps.newConcurrentMap();
    return createInstance(interfaceClass, backingMap);
  }

  /**
   * Is this a dynamic object?
   *
   * @param object
   *          dynamic object to query
   *
   * @return {@code true} if this is, in fact, a dynamic object
   */
  public static boolean isDynamicObject(Object object) {
    if (object == null) {
      return false;
    }
    Class<?> c = object.getClass();
    if (isProxyClass(c)) {
      InvocationHandler handler = getInvocationHandler(object);
      return handler instanceof BackingMapInvocationHandler;
    }
    return false;
  }

  /**
   * What's the type of the given dynamic object?
   *
   * @param dynamicObject
   *          dynamic object to query
   *
   * @return interface class represented by the dynamic object
   */
  public static Class<?> getClass(Object dynamicObject) {
    Preconditions.checkArgument(isDynamicObject(dynamicObject), "Object must be DynamicObject");
    return dynamicObject.getClass().getInterfaces()[0];
  }

  /**
   * Get the backing map for the given dynamic object.
   *
   * @param dynamicObject
   *          object to get map from
   *
   * @return backing map for object
   */
  public static Map<String, Object> getBackingMap(Object dynamicObject) {
    InvocationHandler handler = getInvocationHandler(dynamicObject);
    return ((BackingMapInvocationHandler) handler).getBackingMap();
  }
}
