/**
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

package interactivespaces.service.template.internal.freemarker;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.service.template.Templater;

import com.google.common.io.Closeables;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

/**
 * @author Keith M. Hughes
 */
public class FreemarkerTemplater implements Templater {

  /**
   * The directory containing the templates.
   */
  private final File templateDirectory;

  /**
   * The configuration used by Freemarker.
   */
  private Configuration freemarkerConfig;

  /**
   * Construct a new Freemarker templater.
   *
   * @param templateDirectory
   *          the directory containing the templates
   */
  public FreemarkerTemplater(File templateDirectory) {
    this.templateDirectory = templateDirectory;
  }

  @Override
  public void startup() {
    try {
      freemarkerConfig = new Configuration();
      freemarkerConfig.setDirectoryForTemplateLoading(templateDirectory);
      // Specify how templates will see the data-model. This is an
      // advanced topic... but just use this:
      freemarkerConfig.setObjectWrapper(new DefaultObjectWrapper());
    } catch (IOException e) {
      throw new InteractiveSpacesException("Cannot initialize Freemarker templater", e);
    }
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  @Override
  public String instantiateTemplate(String templateName, Map<String, Object> data) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Writer out = new OutputStreamWriter(baos);
    try {
      Template template = freemarkerConfig.getTemplate(templateName);

      template.process(data, out);
      out.close();
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not instantiate template %s", templateName), e);
    } finally {
      Closeables.closeQuietly(out);
    }

    return new String(baos.toString());
  }

  @Override
  public void writeTemplate(String templateName, Map<String, Object> data, File outputFile) {
    Writer out = null;
    try {
      Template template = freemarkerConfig.getTemplate(templateName);
      out = new FileWriter(outputFile);
      template.process(data, out);
      out.close();
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not instantiate template %s", templateName), e);
    } finally {
      Closeables.closeQuietly(out);
    }
  }
}
