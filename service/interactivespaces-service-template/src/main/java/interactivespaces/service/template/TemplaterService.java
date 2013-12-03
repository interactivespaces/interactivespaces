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

package interactivespaces.service.template;

import interactivespaces.service.SupportedService;

import java.io.File;

/**
 * An Interactive Spaces service for instantiating text based templates.
 *
 * <p>
 * The template will contain special strings which can be evaluated by the
 * templating engine. Instantiating the template will replace those special
 * strings with the value of the template expression.
 *
 * @author Keith M. Hughes
 */
public interface TemplaterService extends SupportedService {

  /**
   * Service name for templaters.
   */
  String SERVICE_NAME = "text.templater";

  /**
   * Create a templater for the given template directory.
   *
   * @param templateDirectory
   *        the directory containing the templates
   *
   * @return the new templater
   */
  Templater newTemplater(File templateDirectory);
}
