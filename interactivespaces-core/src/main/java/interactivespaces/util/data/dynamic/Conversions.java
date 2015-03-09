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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Utility class for working with implicit conversions.
 *
 * @author Oleksandr Kelepko
 */
final class Conversions {
  /**
   * Prevent instantiation of a utility class.
   */
  private Conversions() {
  }

  /**
   * Simple (primitives + {@link Object} + {@link String}) type conversions.
   */
  private static final Map<Class<?>, Conversion<?>> SIMPLE_TYPE_CONVERSIONS;

  /**
   * List conversion.
   */
  private static final ListConversion LIST_CONVERSION = new ListConversion();

  /**
   * Map conversion.
   */
  private static final MapConversion MAP_CONVERSION = new MapConversion();

  static {
    Conversion<Boolean> toBoolean = new IdentityConversion<Boolean>(Boolean.class);
    Conversion<Integer> toInt = new IdentityConversion<Integer>(Integer.class);
    Conversion<Long> toLong = new IntegerOrLongToLong();
    Conversion<Float> toFloat = new NumberOrStringToFloat();
    Conversion<Double> toDouble = new NumberOrStringToDouble();

    SIMPLE_TYPE_CONVERSIONS = ImmutableMap.<Class<?>, Conversion<?>>builder()
        .put(Boolean.class, toBoolean)
        .put(Boolean.TYPE, toBoolean)
        .put(Integer.class, toInt)
        .put(Integer.TYPE, toInt)
        .put(Long.class, toLong)
        .put(Long.TYPE, toLong)
        .put(Float.class, toFloat)
        .put(Float.TYPE, toFloat)
        .put(Double.class, toDouble)
        .put(Double.TYPE, toDouble)
        .put(String.class, new IdentityConversion<String>(String.class))
        .put(Object.class, new IdentityConversion<Object>(Object.class))
        .build();
  }

  /**
   * Convert a given object to a given type. If conversion is impossible, throws an exception.
   *
   * @param expectedGenericType
   *     expected generic type of the result
   * @param expectedType
   *     expected type of the result
   * @param object
   *     object to convert
   * @param <T>
   *     type of the result
   *
   * @return object of the given type
   */
  public static <T> T convert(Type expectedGenericType, Class<T> expectedType, Object object) {
    Object result = null;
    if (object == null) {
      if (!expectedType.isPrimitive()) {
        return null;
      }
      if (expectedType == Boolean.TYPE) {
        result = false;
      }
      // No default value for other primitive types.
    } else if (SIMPLE_TYPE_CONVERSIONS.containsKey(expectedType)) {
      result = SIMPLE_TYPE_CONVERSIONS.get(expectedType).apply(object);
    } else if (expectedType == List.class && object instanceof List) {
      result = LIST_CONVERSION.convert(expectedGenericType, object);
    } else if (expectedType == Map.class && object instanceof Map) {
      result = MAP_CONVERSION.convert(expectedGenericType, object);
    } else if (expectedType.isInstance(object)) {
      // Arbitrary object set in setter.
      result = object;
    } else if (expectedType.isInterface() && object instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> asMap = (Map) object;
      result = InterfaceMap.createInstance(expectedType, asMap);
    }

    return result != null
        ? Primitives.wrap(expectedType).cast(result)
        : cannotConvert(object, expectedType);
  }

  /**
   * Throw a proper exception saying that conversion failed.
   *
   * @param input
   *     object that cannot be converted to the target type
   * @param targetType
   *     target raw type
   * @param <T>
   *     target type
   *
   * @return nothing
   */
  private static <T> T cannotConvert(Object input, Class<T> targetType) {
    if (input == null) {
      throw new NullPointerException(
          String.format("Cannot convert null to primitive type %s.", targetType.getName()));
    } else {
      throw new ClassCastException(String.format("Cannot convert to %s from %s: %s.",
          targetType.getName(), input.getClass().getName(), input));
    }
  }

  /**
   * Represents a function that converts values to a proper generic type.
   * For example, if the target type is {@code Map&lt;String, MyInterface&gt;},
   * the original map may contain maps that need to be transformed into an {@link InterfaceMap}.
   * If the target type is {@code Map&lt;String, List&lt;MyInterface&gt;&gt;},
   * then the elements of lists may need to be transformed.
   *
   * @param <T>
   *     target type of the conversion
   */
  private static final class RecursiveConversion<T> implements Function<Object, T> {
    /**
     * Creates a conversion that converts values to a given type.
     *
     * @param expectedGenericType
     *     target generic type
     * @param expectedType
     *     target raw type
     * @param <T>
     *     target type
     *
     * @return conversion
     */
    public static <T> Function<Object, T> create(Type expectedGenericType, Class<T> expectedType) {
      if (SIMPLE_TYPE_CONVERSIONS.containsKey(expectedType) || expectedType.isInterface()) {
        return new RecursiveConversion<T>(expectedGenericType, expectedType);
      } else {
        throw new UnsupportedOperationException("Unsupported dynamic property type: " + expectedGenericType);
      }
    }

    /**
     * Target generic type.
     */
    private final Type expectedGenericType;

    /**
     * Target raw type.
     */
    private final Class<T> expectedType;

    /**
     * Construct a new recursive conversion.
     *
     * @param expectedGenericType
     *     target generic type
     * @param expectedType
     *     target raw type
     */
    private RecursiveConversion(Type expectedGenericType, Class<T> expectedType) {
      this.expectedGenericType = expectedGenericType;
      this.expectedType = expectedType;
    }

    @Override
    public T apply(Object input) {
      return convert(expectedGenericType, expectedType, input);
    }
  }

  /**
   * Represents an implicit conversion of a value from an original type to a target type.
   * If the value is {@code null} or if the original type is the same as the target type,
   * the original value will be returned without transformations.
   */
  private abstract static class Conversion<T> implements Function<Object, T> {
    /**
     * Target type.
     */
    private final Class<T> targetType;

    /**
     * Create a new conversion.
     *
     * @param targetType
     *     target type
     */
    Conversion(Class<T> targetType) {
      this.targetType = targetType;
    }

    @Override
    public final T apply(Object input) {
      if (input == null || targetType.isInstance(input)) {
        return targetType.cast(input);
      }
      T result = tryConvert(input);
      if (result != null) {
        return result;
      }
      return cannotConvert(input, targetType);
    }

    /**
     * Possibly transform a given value to return a value of a target type.
     *
     * @param input
     *     value whose type differs from the target type
     *
     * @return value of the target type, or {@code null} if the value cannot be converted
     */
    protected abstract T tryConvert(Object input);
  }

  /**
   * A no-op conversion. If a given object is of a target type, it is returned as-is.
   * Otherwise, an exception is thrown.
   *
   * @param <T>
   *     target type
   */
  private static final class IdentityConversion<T> extends Conversion<T> {
    /**
     * Create new identity conversion.
     *
     * @param targetType
     *     target raw type
     */
    public IdentityConversion(Class<T> targetType) {
      super(targetType);
    }

    @Override
    protected T tryConvert(Object input) {
      return null;
    }
  }

  /**
   * Converts {@link Long Long}s and {@link Integer Integer}s into {@link Long}.
   */
  private static class IntegerOrLongToLong extends Conversion<Long> {
    /**
     * Create a new conversion.
     */
    public IntegerOrLongToLong() {
      super(Long.class);
    }

    @Override
    protected Long tryConvert(Object input) {
      return input instanceof Integer ? ((Integer) input).longValue() : null;
    }
  }

  /**
   * Converts {@link Number}s and some {@link String}s into {@link Double}s.
   */
  private static class NumberOrStringToDouble extends Conversion<Double> {
    /**
     * Special double values as strings.
     */
    private static final Map<String, Double> SPECIAL_DOUBLE_VALUES = ImmutableMap.<String, Double>builder()
        .put(Double.toString(Double.NaN), Double.NaN)
        .put(Double.toString(Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY)
        .put(Double.toString(Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY)
        .build();

    /**
     * Create a new conversion.
     */
    public NumberOrStringToDouble() {
      super(Double.class);
    }

    @Override
    public Double tryConvert(Object o) {
      if (o instanceof Number) {
        return ((Number) o).doubleValue();
      }
      Double d = SPECIAL_DOUBLE_VALUES.get(o);
      return d != null ? d : null;
    }
  }

  /**
   * Converts {@link Number}s and some {@link String}s into {@link Float}s.
   */
  private static class NumberOrStringToFloat extends Conversion<Float> {
    /**
     * Special float values as strings.
     */
    private static final Map<String, Float> SPECIAL_FLOAT_VALUES = ImmutableMap.<String, Float>builder()
        .put(Float.toString(Float.NaN), Float.NaN)
        .put(Float.toString(Float.NEGATIVE_INFINITY), Float.NEGATIVE_INFINITY)
        .put(Float.toString(Float.POSITIVE_INFINITY), Float.POSITIVE_INFINITY)
        .build();

    /**
     * Create a new conversion.
     */
    public NumberOrStringToFloat() {
      super(Float.class);
    }

    @Override
    public Float tryConvert(Object o) {
      if (o instanceof Number) {
        return ((Number) o).floatValue();
      }
      Float d = SPECIAL_FLOAT_VALUES.get(o);
      return d != null ? d : null;
    }
  }

  /**
   * List conversion that recursively considers the list's type parameters.
   * Raw lists and wildcards are not supported.
   */
  private static class ListConversion {
    /**
     * Convert a given list to a given target type.
     *
     * @param genericListType
     *     target type
     * @param list
     *     list to convert
     *
     * @return list of the target type
     */
    public Object convert(Type genericListType, Object list) {
      checkArgument(list instanceof List<?>, "Not a list.");

      if (!(genericListType instanceof ParameterizedType)) {
        throw new UnsupportedOperationException("Raw lists are not supported: " + genericListType);
      }

      Type genericElementType = ((ParameterizedType) genericListType).getActualTypeArguments()[0];
      Class<?> elementType;
      if (genericElementType instanceof ParameterizedType) {
        // Example: List<Map<String, String>>.
        elementType = (Class<?>) ((ParameterizedType) genericElementType).getRawType();
      } else if (genericElementType instanceof Class<?>) {
        // Example: List<String>.
        elementType = (Class<?>) genericElementType;
      } else {
        // Example: List<? extends Number>.
        throw new UnsupportedOperationException("Wildcards are not supported: " + genericListType);
      }

      Function<Object, ?> transformation = RecursiveConversion.create(genericElementType, elementType);
      // NOTE: ImmutableList disallows null elements.
      return ImmutableList.copyOf(Lists.transform((List<?>) list, transformation));
    }
  }

  /**
   * Map conversion that recursively considers the map's type parameters.
   * Only maps with String keys are supported.
   * Raw maps and wildcards are not supported.
   */
  private static class MapConversion {
    /**
     * Convert a given map to a given target type.
     *
     * @param genericMapType
     *     target type
     * @param map
     *     map to convert
     *
     * @return map of the target type
     */
    public Object convert(Type genericMapType, Object map) {
      checkArgument(map instanceof Map<?, ?>, "Not a map.");

      if (!(genericMapType instanceof ParameterizedType)) {
        throw new UnsupportedOperationException("Raw maps are not supported: " + genericMapType);
      }

      Type[] typeArguments = ((ParameterizedType) genericMapType).getActualTypeArguments();
      Type genericKeyType = typeArguments[0];
      if (genericKeyType != String.class) {
        throw new UnsupportedOperationException("Only maps with String keys are supported: " + genericMapType);
      }
      Type genericValueType = typeArguments[1];
      Class<?> elementType;
      if (genericValueType instanceof ParameterizedType) {
        // Example: Map<String, List<String>>.
        elementType = (Class<?>) ((ParameterizedType) genericValueType).getRawType();
      } else if (genericValueType instanceof Class<?>) {
        // Example: Map<String>.
        elementType = (Class<?>) genericValueType;
      } else {
        // Example: Map<String, ? extends Number>.
        throw new UnsupportedOperationException("Wildcards are not supported: " + genericMapType);
      }

      Function<Object, ?> transformation = RecursiveConversion.create(genericValueType, elementType);
      // NOTE: ImmutableMap disallows null values.
      return ImmutableMap.copyOf(Maps.transformValues((Map) map, transformation));
    }
  }
}
