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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Dynamic object factory. Given an interface and a map, creates an object with that interface with state (JavaBean
 * properties) stored in the map.
 * <p/>
 * In current implementation calls to hashCode() and toString() are delegated to the backing map. Two dynamic objects
 * are equal if they are of the same type (implement the same interface) and have the same state (backing maps are
 * equal).
 * <p/>
 * Primitive getters will throw an exception if the corresponding value is null. One exception is primitive boolean
 * getter, which in this case will return false.
 *
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
   * Reflectively creates an instance of a given interface, that is backed by a given map. Interface must contain
   * JavaBean properties of the following types:
   * <ul>
   * <li>{@link java.lang.Integer} and {@code int}</li>
   * <li>{@link java.lang.Long} and {@code long}</li>
   * <li>{@link java.lang.Double} and {@code double}</li>
   * <li>{@link java.lang.Number}</li>
   * <li>{@link java.lang.Boolean} and {@code boolean}</li>
   * <li>{@link java.lang.String}</li>
   * <li>{@link java.lang.CharSequence}</li>
   * <li>another JavaBean interface that meets these requirements</li>
   * <li>{@link java.util.List} of elements of any of the types above (no wildcards)</li>
   * </ul>
   * <p/>
   * Lists represent live view of a list in the backing map. Element removal is supported, adding elements is not.
   *
   * @param interfaceClass
   *          type of the object to create
   * @param backingMap
   *          backing map, the source of values
   * @param <T>
   *          type of the object to create
   *
   * @return instance of the {@code interfaceClass}, backed by {@code backingMap}
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
    return ((BackingMapInvocationHandler) handler).backingMap;
  }

  /**
   * Invocation handler that stores state of the object (JavaBean properties) in the backing map. Calls to hashCode()
   * and toString() are delegated to the map.
   */
  static final class BackingMapInvocationHandler implements InvocationHandler {

    /**
     * Empty array for masking null in {@link BackingMapInvocationHandler#invoke}.
     */
    private static final Object[] NO_ARGS = {};

    /**
     * Simple types.
     */
    private static final Collection<Class<?>> SIMPLE_TYPES = ImmutableSet.<Class<?>>builder().add(Object.class)
        .add(Integer.class).add(Integer.TYPE).add(Long.class).add(Long.TYPE).add(Double.class).add(Double.TYPE)
        .add(Number.class).add(Boolean.class).add(Boolean.TYPE).add(String.class).add(CharSequence.class).build();

    /**
     * Converts numbers and some strings into doubles.
     */
    private static final Function<Object, Double> TO_DOUBLE = new ToDouble();

    /**
     * The state of the object. Keys are JavaBean property names, values are objects of the following types:
     * <ul>
     * <li>{@link java.lang.Integer}</li>
     * <li>{@link java.lang.Long}</li>
     * <li>{@link java.lang.Double}</li>
     * <li>{@link java.lang.Boolean}</li>
     * <li>{@link java.lang.String}</li>
     * <li>{@link java.collection.Map} whose keys are Javabean properties whose values are found in this list listed
     * types.</li>
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
    private BackingMapInvocationHandler(Map<String, Object> backingMap, Class<?> type) {
      this.backingMap = Preconditions.checkNotNull(backingMap);
      this.type = type.getName();
    }

    @Override
    public Object invoke(Object target, Method method, Object[] args) throws Throwable {
      if (args == null) {
        args = NO_ARGS;
      }

      String methodName = method.getName();

      if (method.getDeclaringClass() == Object.class) {
        if (args.length == 0) {
          if (methodName.equals("hashCode")) {
            return backingMap.hashCode();
          }
          if (methodName.equals("toString")) {
            return backingMap.toString();
          }
        }
        if (args.length == 1 && methodName.equals("equals") && method.getParameterTypes()[0] == Object.class) {
          if (args[0] != null && isProxyClass(args[0].getClass())) {
            InvocationHandler handler = getInvocationHandler(args[0]);
            return (handler instanceof BackingMapInvocationHandler)
                && type.equals(((BackingMapInvocationHandler) handler).type)
                && backingMap.equals(((BackingMapInvocationHandler) handler).backingMap);
          }
          return false;
        }
      }

      // Getters and setters
      if (isGetter(method, args)) {
        return invokeGetter(method);
      }
      if (isSetter(method, args)) {
        invokeSetter(method, args[0]);
        return null;
      }

      // Unsupported method
      String msg = String.format("Method %s is neither a getter nor a setter.", method);
      throw new UnsupportedOperationException(msg);
    }

    /**
     * Emulates invoking a getter.
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

      if (result == null) {
        if (returnType == Boolean.TYPE) {
          return false;
        }
        if (returnType.isPrimitive()) {
          String message = String.format("Cannot convert null to %s, required by %s.", returnType, method);
          throw new IllegalStateException(message);
        }
        return null;
      }

      if (SIMPLE_TYPES.contains(returnType)) {
        // 'simple' type - return as-is
        return checkType(returnType, method, result);
      }

      if (returnType == List.class && result instanceof List) {
        return getList(method, (List) result);
      }

      // Do this AFTER dealing with List.class and BEFORE trying to wrap a Map into a DynamicObject.
      if (returnType.isInstance(result)) {
        return result;
      }

      if (returnType.isInterface() && result instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> asMap = (Map) result;
        return createInstance(returnType, asMap);
      }

      String msg =
          String.format("Method %s returns an object, but the backing map does not contain an object: %s", method,
              result);
      throw new IllegalStateException(msg);
    }

    /**
     * Emulates invoking a setter.
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
        }
      } else {
        backingMap.put(property, arg);
      }
    }

    /**
     * Transforms a given list into a proper return value of a given method.
     *
     * @param method
     *          method whose return value type to which the list will be converted
     * @param list
     *          list that represents the method's return value
     *
     * @return list that can be returned from the method
     */
    private static List<?> getList(Method method, List list) {
      // Check that element types are right.
      ParameterizedType genericReturnType = (ParameterizedType) method.getGenericReturnType();
      Type[] typeArguments = genericReturnType.getActualTypeArguments();
      // This will fail if there are wildcards (e.g. List<? extends Number>).
      Class<?> typeArgument = (Class<?>) typeArguments[0];

      if (!SIMPLE_TYPES.contains(typeArgument) && !typeArgument.isInterface()) {
        String msg = String.format("Return type %s not supported: %s", typeArgument.getName(), method);
        throw new IllegalStateException(msg);
      }

      if (elementsMatch(list, typeArgument, false)) {
        // Is this a good idea?
        return list;
      }

      // Elements don't match precisely - will try some transformations.

      if (typeArgument == Long.class) {
        // Not attempting to round floats or doubles.
        checkListElements(method, list, Long.class, Integer.class);
        return Lists.transform(list, new ToLong());
      }

      if (typeArgument == Double.class) {
        // Any Number and some Strings ("NaN", "-Infinity", "Infinity") can be converted to a Double.
        checkListElements(method, list, Number.class, String.class);
        return Lists.transform(list, TO_DOUBLE);
      }

      if (typeArgument.isInterface()) {
        @SuppressWarnings("unchecked")
        Function<Object, ?> function = new ToDynamicObject(typeArgument);
        List<Object> newList = Lists.newArrayListWithExpectedSize(list.size());
        for (Object x : list) {
          newList.add(function.apply(x));
        }
        return Collections.unmodifiableList(newList);
        // The code above *should* be functionally equivalent to:
        // return Lists.transform(list, function);
        // ...but for some reason the Lists.transform version does not work!
      }

      // Run again, but throw this time.
      elementsMatch(list, typeArgument, true);

      // Should never reach here, but just in case...
      String msg = String.format("Can't convert to List<%s>: %s", typeArgument, list);
      throw new IllegalStateException(msg);
    }

    /**
     * Checks whether a list contains elements only of a given type (or null).
     *
     * @param list
     *          elements whose type will be checked
     * @param typeArgument
     *          expected type of the elements in the list
     * @param shouldThrow
     *          {@code true} if the code should throw an exception, otherwise return {@code false}
     *
     * @return {@code true} if all elements of the list match the given type
     */
    private static boolean elementsMatch(List<?> list, Class<?> typeArgument, boolean shouldThrow) {
      int index = 0;
      for (Object element : list) {
        if (element != null && !typeArgument.isInstance(element)) {
          if (shouldThrow) {
            String msg =
                String.format("Can't convert list argument #%d to %s from incompatible type %s", index, typeArgument,
                    element.getClass());
            throw new IllegalStateException(msg);
          }
          return false;
        }

        index++;
      }
      return true;
    }

    /**
     * Ensure a list contains elements of a given type.
     *
     * @param method
     *          method whose return value is the list
     * @param list
     *          list that is the method's return value
     * @param expectedTypes
     *          expected types of the elements in the list
     */
    private static void checkListElements(Method method, List<?> list, Class<?>... expectedTypes) {
      outer: for (Object element : list) {
        if (element == null) {
          continue;
        }
        for (Class<?> type : expectedTypes) {
          if (type.isInstance(element)) {
            continue outer;
          }
        }
        String message =
            String.format("The backing list contains %s of %s, cannot convert to %s, required by %s.", element, element
                .getClass().getName(), Arrays.asList(expectedTypes), method);
        throw new IllegalStateException(message);
      }
    }

    /**
     * Ensures that result is assignable to the return type of the method.
     *
     * @param maybePrimitiveReturnType
     *          return type of the method
     * @param method
     *          method
     * @param result
     *          the value that will be returned as the result of method invocation
     *
     * @return result that is assignable to the return type of the method
     */
    private static Object checkType(Class<?> maybePrimitiveReturnType, Method method, Object result) {
      Class<?> returnType = Primitives.wrap(maybePrimitiveReturnType);
      if (returnType.isInstance(result)) {
        return result;
      }
      if (returnType == Long.class && result instanceof Integer) {
        return ((Integer) result).longValue();
      }
      if (returnType == Double.class) {
        Object r = TO_DOUBLE.apply(result);
        if (r != null) {
          return r;
        }
      }

      // type mismatch
      String msg =
          String.format("While invoking '%s': method returns %s but the backing map contains '%s' of %s", method,
              returnType, result, result.getClass());
      throw new IllegalStateException(msg);
    }

    /**
     * Performs a sanity check that a method is a setter, i.e. has a single parameter.
     *
     * @param method
     *          setter method
     * @param params
     *          list of the method's parameters
     *
     * @return {@code true} if the given method looks like a setter
     * @throws java.lang.UnsupportedOperationException
     *           if the method has a setter-like name, but has number of parameters other than 1 or the parameter is of
     *           a primitive type.
     */
    private static boolean isSetter(Method method, Object[] params) {
      String name = method.getName();
      if (!name.startsWith("set")) {
        return false;
      }
      Class<?> returnType = method.getReturnType();
      if (returnType != void.class && returnType != Void.class) {
        String msg = String.format("Setter method must return void: %s.", method);
        throw new UnsupportedOperationException(msg);
      }
      if (params.length != 1) {
        String msg = String.format("Setter method %s must have exactly 1 parameter: %s", method, Arrays.asList(params));
        throw new UnsupportedOperationException(msg);
      }
      return true;
    }

    /**
     * Performs a sanity check that a method is a getter, e.g. has no parameters.
     *
     * @param method
     *          getter method
     * @param params
     *          list of the method's parameters
     *
     * @return {@code true} if the given method seems like a getter
     * @throws java.lang.UnsupportedOperationException
     *           if the method has a getter-like name, but has parameters or returns a primitive value (except for the
     *           boolean primitive type)
     */
    private static boolean isGetter(Method method, Object[] params) {
      String name = method.getName();
      Class<?> returnType = method.getReturnType();
      boolean getterName =
          name.startsWith("get")
              || (name.startsWith("is") && returnType == Boolean.class || returnType == Boolean.TYPE);
      if (!getterName) {
        return false;
      }
      if (params.length != 0) {
        String msg = String.format("Getter method %s must be parameterless: %s", method, Arrays.asList(params));
        throw new UnsupportedOperationException(msg);
      }
      return true;
    }

    /**
     * Retrieves a JavaBean property name from a method name.
     *
     * @param methodName
     *          method name
     *
     * @return property name
     * @throws UnsupportedOperationException
     *           if {@code methodName} does not represent a JavaBean property
     */
    private static String getPropertyName(String methodName) {
      StringBuilder sb = new StringBuilder(methodName);
      // remove 'get' or 'set'
      if (methodName.startsWith("get") || methodName.startsWith("set")) {
        sb.delete(0, "get".length());
      } else if (methodName.startsWith("is")) {
        sb.delete(0, "is".length());
      }
      if (sb.length() == 0) {
        throw new UnsupportedOperationException("Not a JavaBean property: " + methodName);
      }
      sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
      return sb.toString();
    }
  }

  /**
   * Converts {@link java.lang.Number Numbers} and some {@link java.lang.String Strings} into {@link java.lang.Double}.
   */
  private static class ToDouble implements Function<Object, Double> {
    /**
     * Special double values as strings.
     */
    private final Map<String, Double> specialDoubleValues = ImmutableMap.<String, Double>builder()
        .put("NaN", Double.NaN).put("-Infinity", Double.NEGATIVE_INFINITY).put("Infinity", Double.POSITIVE_INFINITY)
        .build();

    @Override
    public Double apply(Object o) {
      if (o instanceof Double) {
        return (Double) o;
      }
      if (o instanceof Number) {
        return ((Number) o).doubleValue();
      }
      if (o instanceof String) {
        Double d = specialDoubleValues.get(o);
        if (d != null) {
          return d;
        }
      }
      String msg = String.format("Cannot convert %s of %s to Double", o, o.getClass());
      throw new IllegalStateException(msg);
    }
  }

  /**
   * Converts {@link java.lang.Long Long}s and {@link java.lang.Integer Integer}s into {@link java.lang.Long}.
   */
  private static class ToLong implements Function<Object, Long> {
    @Override
    public Long apply(Object o) {
      return o instanceof Integer ? ((Integer) o).longValue() : (Long) o;
    }
  }

  /**
   * Converts {@link java.util.Map Map}s into dynamic objects.
   */
  private static class ToDynamicObject<T> implements Function<Object, T> {
    /**
     * The type of the dynamic object to return.
     */
    private final Class<T> type;

    /**
     * Constructor.
     *
     * @param type
     *          type of the object to return
     */
    ToDynamicObject(Class<T> type) {
      this.type = type;
    }

    @Override
    public T apply(Object o) {
      if (o == null || type.isInstance(o)) {
        return type.cast(o);
      }
      if (o instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> asMap = (Map<String, Object>) o;
        return createInstance(type, asMap);
      }
      String msg = String.format("Can't convert to %s: %s [%s]", type.getName(), o, o.getClass().getName());
      throw new IllegalStateException(msg);
    }
  }
}
