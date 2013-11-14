/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.configuration;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.evaluation.ExpressionEvaluator;
import interactivespaces.evaluation.ExpressionEvaluatorFactory;
import interactivespaces.system.InteractiveSpacesFilesystem;

import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * A file based {@link SystemConfigurationStorageManager}.
 *
 * @author Keith M. Hughes
 */
public class FileSystemConfigurationStorageManager implements SystemConfigurationStorageManager {

  /**
   * Location of the configuration file folder relative to the root of the
   * Interactive Spaces installation.
   */
  private static final String CONFIGURATION_FILE_FOLDER = "config/interactivespaces";

  /**
   * The extension for configuration files.
   */
  private static final String CONFIGURATION_FILE_EXTENSION = ".conf";

  /**
   * The system configuration.
   */
  private Configuration systemConfiguration;

  /**
   * Factory for expression evaluators.
   */
  private ExpressionEvaluatorFactory expressionEvaluatorFactory;

  /**
   * The interactivespaces file system.
   */
  private InteractiveSpacesFilesystem interactiveSpacesFilesystem;

  /**
   * Logger.
   */
  private Log log;

  @Override
  public void startup() {
    ExpressionEvaluator evaluator = expressionEvaluatorFactory.newEvaluator();
    SimpleConfiguration sconfig = new SimpleConfiguration(evaluator);
    evaluator.setEvaluationEnvironment(sconfig);

    systemConfiguration = sconfig;

    for (File configFile : getConfigFiles()) {
      loadConfigFile(systemConfiguration, configFile);
    }
  }

  @Override
  public void shutdown() {
    // Nothing to do.
  }

  @Override
  public Configuration getSystemConfiguration() {
    return systemConfiguration;
  }

  /**
   * Load the contents of a configuration file into a configuration.
   *
   * @param configuration
   * @param configFile
   */
  private void loadConfigFile(Configuration configuration, File configFile) {
    InputStream in = null;
    try {
      if (log.isInfoEnabled()) {
        log.info(String.format("Loading config file %s", configFile));
      }

      in = new FileInputStream(configFile);

      Properties configProperties = new Properties();
      configProperties.load(in);

      for (Entry<Object, Object> entry : configProperties.entrySet()) {
        configuration.setValue(entry.getKey().toString(), entry.getValue().toString());
      }
    } catch (Exception e) {
      log.error("Could not load configuration", e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (Exception e) {
          // Don't care.
        }
      }
    }
  }

  /**
   * Get all configuration files from the Interactive Spaces configuration
   * folder.
   *
   * @return
   */
  private File[] getConfigFiles() {
    File configurationFolder = new File(interactiveSpacesFilesystem.getInstallDirectory(), CONFIGURATION_FILE_FOLDER);
    if (configurationFolder.exists()) {
      if (!configurationFolder.isDirectory()) {
        throw new InteractiveSpacesException(String.format(
            "Interactive Spaces configuration folder %s is not a directory", configurationFolder));
      }
    } else {
      throw new InteractiveSpacesException(String.format("Interactive Spaces configuration folder %s does not exist",
          configurationFolder));
    }

    File[] configFiles = configurationFolder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        String filename = name.toLowerCase();
        return filename.endsWith(CONFIGURATION_FILE_EXTENSION);
      }
    });

    if (configFiles.length == 0) {
      throw new InteractiveSpacesException(String.format(
          "Interactive Spaces configuration folder %s contains no files ending with %s", configurationFolder,
          CONFIGURATION_FILE_EXTENSION));
    }

    return configFiles;
  }

  /**
   * @param expressionEvaluatorFactory
   *          the expressionEvaluatorFactory to set
   */
  public void setExpressionEvaluatorFactory(ExpressionEvaluatorFactory expressionEvaluatorFactory) {
    this.expressionEvaluatorFactory = expressionEvaluatorFactory;
  }

  /**
   * @param log
   *          the log to set
   */
  public void setLog(Log log) {
    this.log = log;
  }

  /**
   * @param interactiveSpacesFilesystem
   *          the interactiveSpacesFilesystem to set
   */
  public void setInteractiveSpacesFilesystem(InteractiveSpacesFilesystem interactiveSpacesFilesystem) {
    this.interactiveSpacesFilesystem = interactiveSpacesFilesystem;
  }
}
