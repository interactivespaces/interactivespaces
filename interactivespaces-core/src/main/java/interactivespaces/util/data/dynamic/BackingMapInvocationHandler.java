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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Invocation handler that stores state of the object (JavaBean properties) in the backing map. Calls to hashCode()
 * and toString() are delegated to the map.
 *
 * @author Oleksandr Kelepko
 */
final class BackingMapInvocationHandler implements InvocationHandler {

  /**
   * Empty array for masking null in {@link BackingMapInvocationHandler#invoke}.
   */
  private static final Object[] NO_ARGS = {};

  /**
   * Prefix of boolean getter methods' names.
   */
  private static final String METHOD_PREFIX_IS = "is";

  /**
   * Prefix of general getter methods' names.
   */
  private static final String METHOD_PREFIX_GET = "get";

  /**
   * Prefix of setter methods' names.
   */
  private static final String METHOD_PREFIX_SET = "set";

  /**
   * Name of the {@link Object#equals(Object)} method.
   */
  private static final String OBJECT_EQUALS_METHOD_NAME = "equals";

  /**
   * Name of the {@link Object#hashCode()} method.
   */
  private static final String OBJECT_HASHCODE_METHOD_NAME = "hashCode";

  /**
   * Name of the {@link Object#toString()} method.
   */
  private static final String OBJECT_TOSTRING_METHOD_NAME = "toString";

  /**
   * The state of the object. Keys are JavaBean property names, values are objects of the following:
   * <ul>
   * <li>a value of one of the {@link Conversions#SIMPLE_TYPE_CONVERSIONS}
   * <li>{@link Map} whose keys are Strings and values are found in this list of types.
   * <li>{@link java.util.List} of any of this list of types</li>
   * </ul>
   */
  private final Map<String, Object> backingMap;

  /**
   * Type of this dynamic object, is used in equals().
   */
  private final String type;

  /**
   * Constructor.
   *
   * @param backingMap
   *          the map that will reflect the state of the object
   * @param type
   *          type this BackingMapInvocationHandler represents (= the interface it implements)
   */
  BackingMapInvocationHandler(Map<String, Object> backingMap, Class<?> type) {
    this.backingMap = Preconditions.checkNotNull(backingMap);
    this.type = type.getName();
  }

  /**
   * Get the state of this object.
   *
   * @return map holding the values of this object's properties
   */
  public Map<String, Object> getBackingMap() {
    return backingMap;
  }

  @Override
  public Object invoke(Object target, Method method, Object[] args) throws Throwable {
    if (args == null) {
      args = NO_ARGS;
    }

    String methodName = method.getName();

    if (method.getDeclaringClass() == Object.class) {
      if (args.length == 0) {
        if (methodName.equals(OBJECT_HASHCODE_METHOD_NAME)) {
          return backingMap.hashCode();
        }
        if (methodName.equals(OBJECT_TOSTRING_METHOD_NAME)) {
          return backingMap.toString();
        }
      }
      if (args.length == 1 && methodName.equals(OBJECT_EQUALS_METHOD_NAME)
          && method.getParameterTypes()[0] == Object.class) {
        if (args[0] != null && isProxyClass(args[0].getClass())) {
          InvocationHandler handler = getInvocationHandler(args[0]);
          return (handler instanceof BackingMapInvocationHandler)
              && type.equals(((BackingMapInvocationHandler) handler).type)
              && backingMap.equals(((BackingMapInvocationHandler) handler).backingMap);
        }
        return false;
      }
      // NOTE: clone() is not supported (yet?). Other methods (except finalize()) are final.
    }

    if (isGetter(method, args)) {
      return invokeGetter(method);
    }
    if (isSetter(method, args)) {
      invokeSetter(method, args[0]);
      return null;
    }

    throw new UnsupportedOperationException(
        String.format("Method is neither a getter nor a setter: %s.", method));
  }

  /**
   * Emulate invoking a getter.
   *
   * @param method
   *          method that was called
   *
   * @return result of calling the getter
   */
  private Object invokeGetter(Method method) {
    String property = getPropertyName(method.getName());

    Object result = backingMap.get(property);
    Class<?> returnType = method.getReturnType();
    Type genericReturnType = method.getGenericReturnType();

    return Conversions.convert(genericReturnType, returnType, result);
  }

  /**
   * Emulate invoking a setter.
   *
   * @param method
   *          method that was called
   * @param arg
   *          value to set
   */
  private void invokeSetter(Method method, Object arg) {
    String property = getPropertyName(method.getName());
    if (arg == null) {
      // NOTE: backingMap may not support null values,
      // so here we remove the entry instead of putting null
      backingMap.remove(property);
    } else if (isProxyClass(arg.getClass())) {
      InvocationHandler handler = getInvocationHandler(arg);
      if (handler instanceof BackingMapInvocationHandler) {
        BackingMapInvocationHandler dynamicObject = (BackingMapInvocationHandler) handler;
        backingMap.put(property, dynamicObject.backingMap);
      } else {
        backingMap.put(property, arg);
      }
    } else {
      backingMap.put(property, arg);
    }
  }

  /**
   * Perform a sanity check that a method is a setter, i.e. has a single parameter.
   *
   * @param method
   *          setter method
   * @param params
   *          list of the method's parameters
   *
   * @return {@code true} if the given method looks like a setter
   *
   * @throws UnsupportedOperationException
   *           if the method has a setter-like name, but has number of parameters other than 1
   */
  private static boolean isSetter(Method method, Object[] params) {
    if (!method.getName().startsWith(METHOD_PREFIX_SET)) {
      return false;
    }

    Class<?> returnType = method.getReturnType();
    if (returnType != void.class && returnType != Void.class) {
      throw new UnsupportedOperationException(
          String.format("Setter method must return void: %s.", method));
    }

    if (params.length != 1) {
      throw new UnsupportedOperationException(
          String.format("Setter method must have exactly 1 parameter: %s", method));
    }

    return true;
  }

  /**
   * Perform a sanity check that a method is a getter, e.g. has no parameters.
   *
   * @param method
   *          getter method
   * @param params
   *          list of the method's parameters
   *
   * @return {@code true} if the given method seems like a getter
   *
   * @throws UnsupportedOperationException
   *           if the method has a getter-like name, but has parameters
   */
  private static boolean isGetter(Method method, Object[] params) {
    String name = method.getName();
    Class<?> returnType = method.getReturnType();

    boolean isGetterName = name.startsWith(METHOD_PREFIX_GET)
        || (name.startsWith(METHOD_PREFIX_IS) && (returnType == Boolean.class || returnType == Boolean.TYPE));
    if (!isGetterName) {
      return false;
    }

    if (returnType == void.class || returnType == Void.class) {
      throw new UnsupportedOperationException(
          String.format("Getter method must have a return type (not void or Void): %s", method));
    }

    if (params.length != 0) {
      throw new UnsupportedOperationException(
          String.format("Getter method must be parameterless: %s", method));
    }

    return true;
  }

  /**
   * Retrieve a JavaBean property name from a method name.
   *
   * @param methodName
   *          method name
   *
   * @return property name
   *
   * @throws UnsupportedOperationException
   *           if {@code methodName} does not represent a JavaBean property
   */
  private static String getPropertyName(String methodName) {
    StringBuilder sb = new StringBuilder(methodName);
    // Strip 'is'/'get' or 'set'.
    if (methodName.startsWith(METHOD_PREFIX_GET) || methodName.startsWith(METHOD_PREFIX_SET)) {
      sb.delete(0, METHOD_PREFIX_GET.length());
    } else if (methodName.startsWith(METHOD_PREFIX_IS)) {
      sb.delete(0, METHOD_PREFIX_IS.length());
    }
    if (sb.length() == 0) {
      throw new UnsupportedOperationException("Not a getter or setter: " + methodName);
    }
    sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
    return sb.toString();
  }
}
