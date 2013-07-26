/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.util.i18n;

import java.util.List;
import java.util.Locale;

/**
 * A source of internationalization data.
 *
 * <p>
 * Implementations of this class are not thread safe.
 *
 * @author Keith M. Hughes
 */
public interface I18nSource {

  /**
   * Get the string associated with a given key.
   *
   * @param key
   *          the key
   *
   * @return the value for the key, if found, or {@code null} if not found.
   */
  String getMessage(String key);

  /**
   * Get a formatted message.
   *
   * @param messageKey
   *          the key for the message
   * @param args
   *          the arguments for the message
   *
   * @return the formatted message.
   */
  String getMessage(String messageKey, List<String> args);

  /**
   * Get the locale for the source.
   *
   * @return the locale for the source
   */
  Locale getLocale();
}
