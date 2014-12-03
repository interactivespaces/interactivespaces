/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.activity.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a field into which a value from configuration will be injected.
 * By default, properties are required - an exception will be thrown if a
 * required property is not set. Thus, the common usage is:
 * <pre>
 *   {@code @ConfigurationProperty("property.name") private T value;}
 * </pre>
 * where {@code T} is any of the following:
 * <ul>
 *   <li>int / Integer
 *   <li>long / Long
 *   <li>double / Double
 *   <li>boolean / Boolean
 *   <li>String
 *   <li>List&lt;String&gt; / Collection&lt;String&gt; / Iterable&lt;String&gt;
 *   <li>Set&lt;String&gt;
 * </ul>
 * If a property is not required, the field's value will not be changed.
 * E.g., for a required property
 * <pre>
 *   {@code @ConfigurationProperty("property.name") private String value;}
 * </pre>
 * has the same effect as
 * <pre>
 *   {@code private String value = getConfiguration().getRequiredPropertyString("property.name");}
 * </pre>
 * and for a non-required property
 * <pre>
 *   {@code @ConfigurationProperty(name = "property.name", required = false) private int value = 42;}
 * </pre>
 * is an alternative to
 * <pre>
 *   {@code private int value = getConfiguration().getPropertyInteger("property.name", 42);}
 * </pre>
 * and
 * <pre>
 *   {@code @ConfigurationProperty(name = "property.name", required = false) private Integer value;}
 * </pre>
 * is equivalent to
 * <pre>
 *   {@code private Integer value = getConfiguration().getPropertyInteger("property.name", null);}
 * </pre>
 * If a field is not explicitly initialized in the corresponding constructor,
 * and there's no value for it in the configuration, it will hold a default value as specified
 * by <a href="http://docs.oracle.com/javase/specs/jls/se5.0/html/typesValues.html#96595">JLS</a>.
 *
 * @author Oleksandr Kelepko
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperty {

  /**
   * Configuration property name.
   */
  String value() default "";

  /**
   * Configuration property name. Usually used when multiple annotation arguments are used.
   */
  String name() default "";

  /**
   * {@code true} if the given property is required.
   */
  boolean required() default true;

  /**
   * Delimiter for list/set properties.
   */
  String delimiter() default ",";
}
