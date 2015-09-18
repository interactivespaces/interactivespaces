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

/**
 * An annotation processor that injects configuration values into fields marked with
 * {@link interactivespaces.activity.annotation.ConfigurationProperty}.
 *
 * @author Oleksandr Kelepko
 */
public interface ConfigurationPropertyAnnotationProcessor {

  /**
   * Injects config parameters into a given object. Scans the objects for the fields marked with
   * {@link interactivespaces.activity.annotation.ConfigurationProperty}. {@link ConfigurationProperty#value()}
   * corresponds to a configuration property name.
   *
   * @param obj
   *          object into which config parameters will be injected
   *
   * @see interactivespaces.activity.annotation.ConfigurationProperty
   * @see interactivespaces.configuration.Configuration
   */
  void process(Object obj);
}
