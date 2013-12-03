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

import interactivespaces.util.resource.ManagedResource;

import java.io.File;
import java.util.Map;

/**
 * A text templater.
 *
 * <p>
 * See {@link TemplaterService} for details on what templates are.
 *
 * @author Keith M. Hughes
 */
public interface Templater extends ManagedResource {

  /**
   * Instantiate a template given a set of data.
   *
   * @param templateName
   *          which template to use
   * @param data
   *          data for the template
   *
   * @return the instantiated template
   */
  String instantiateTemplate(String templateName, Map<String, Object> data);

  /**
   * Instantiate a template given a set of data and write the contents to a
   * file.
   *
   * @param templateName
   *          which template to use
   * @param data
   *          data for the template
   * @param outputFile
   *          file to writre the template to
   */
  void writeTemplate(String templateName, Map<String, Object> data, File outputFile);
}
