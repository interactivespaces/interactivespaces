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

package interactivespaces.workbench.tasks;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.process.BaseNativeApplicationRunnerListener;
import interactivespaces.util.process.NativeApplicationDescription;
import interactivespaces.util.process.NativeApplicationRunner;
import interactivespaces.util.process.NativeApplicationRunner.NativeApplicationRunnerState;
import interactivespaces.util.process.NativeApplicationRunnerCollection;

import com.google.common.collect.Maps;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * A workbench task instance factory for native applications.
 *
 * @author Keith M. Hughes
 */
public class NativeWorkbenchTaskFactory implements WorkbenchTaskFactory {

  /**
   * The file name prefix for instantiated task templates.
   */
  public static final String TEMPLATE_FILE_NAME_PREFIX = "workbench";

  /**
   * The file name suffix for instantiated task templates.
   */
  public static final String TEMPLATE_FILE_NAME_SUFFIX = ".native";

  /**
   * The template variable for obtaining the workbench task context.
   */
  public static final String TEMPLATE_VARIABLE_WORKBENCH_TASK_CONTEXT = "workbenchTaskContext";

  /**
   * The application description.
   */
  private NativeApplicationDescription applicationDescription;

  /**
   * Potential source file path for the template.
   */
  private String templateSource;

  /**
   * Potential content for the template.
   */
  private String templateContent;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new factory.
   *
   * @param applicationDescription
   *          the application description
   * @param templateSource
   *          a potential source file for the template
   * @param templateContent
   *          potential content for the template
   */
  public NativeWorkbenchTaskFactory(NativeApplicationDescription applicationDescription, String templateSource,
      String templateContent) {
    this.applicationDescription = applicationDescription;
    this.templateSource = templateSource;
    this.templateContent = templateContent;
  }

  @Override
  public WorkbenchTask newTask() {
    return new BaseWorkbenchTask() {
      @Override
      public void perform(WorkbenchTaskContext workbenchTaskContext) throws InteractiveSpacesException {
        Configuration config = getConfiguration(workbenchTaskContext);

        prepareForExecution(workbenchTaskContext, config);

        workbenchTaskContext.getWorkbench().getLog()
            .info(String.format("Native application %s", applicationDescription));

        NativeApplicationRunnerCollection nativeApplicationRunners =
            workbenchTaskContext.getNativeApplicationRunners();
        NativeApplicationRunner runner = nativeApplicationRunners.newNativeApplicationRunner();

        runner.setExecutablePath(config.evaluate(applicationDescription.getExecutablePath()));

        for (String commandArg : applicationDescription.getArguments()) {
          runner.addCommandArguments(config.evaluate(commandArg));
        }
        runner.addEnvironmentVariables(applicationDescription.getEnvironment());

        final CountDownLatch latch = new CountDownLatch(1);
        runner.addNativeApplicationRunnerListener(new BaseNativeApplicationRunnerListener() {

          @Override
          public void onNativeApplicationRunnerShutdown(NativeApplicationRunner runner) {
            latch.countDown();
          }
        });

        nativeApplicationRunners.addNativeApplicationRunner(runner);

        try {
          latch.await();
        } catch (InterruptedException e) {
          SimpleInteractiveSpacesException.throwFormattedException("Native command %s interrupted",
              applicationDescription);
        }

        NativeApplicationRunnerState state = runner.getState();
        if (!NativeApplicationRunnerState.SHUTDOWN.equals(state)) {
          SimpleInteractiveSpacesException.throwFormattedException("Native command %s ended in state %s",
              applicationDescription, state);
        }
      }
    };
  }

  /**
   * Get the configuration for the task.
   *
   * <p>
   * This method is meant to be overridden. By default it returns the workbench configuration.
   *
   * @param workbenchTaskContext
   *          the workbench task context
   *
   * @return the configuration
   */
  protected Configuration getConfiguration(WorkbenchTaskContext workbenchTaskContext) {
    return workbenchTaskContext.getWorkbench().getWorkbenchConfig();
  }

  /**
   * Get the base location for writing task templates.
   *
   * @param workbenchTaskContext
   *          the workbench task context
   *
   * @return a base location for a template
   */
  protected File getBaseTemplateDirectory(WorkbenchTaskContext workbenchTaskContext) {
    return workbenchTaskContext.getWorkbench().getSpaceEnvironment().getFilesystem().getTempDirectory();
  }

  /**
   * Create a template file.
   *
   * @param baseDirectory
   *          the base directory for template files
   *
   * @return a location for the template file
   */
  private File createTemplateFile(File baseDirectory) {
    return fileSupport.createTempFile(baseDirectory, TEMPLATE_FILE_NAME_PREFIX, TEMPLATE_FILE_NAME_SUFFIX);
  }

  /**
   * Prepare the native task for execution.
   *
   * @param workbenchTaskContext
   *          task context for the workbench
   * @param config
   *          the configuration to use
   */
  private void prepareForExecution(WorkbenchTaskContext workbenchTaskContext, Configuration config) {
    String executablePath = applicationDescription.getExecutablePath();
    if (executablePath == null) {
      if (templateContent == null || templateContent.isEmpty()) {
        File templateSourceFile = fileSupport.newFile(config.evaluate(templateSource));
        if (fileSupport.isFile(templateSourceFile)) {
          templateContent = fileSupport.readFile(templateSourceFile);
        } else {
          SimpleInteractiveSpacesException.newFormattedException("Could not find task template file %s",
              templateSourceFile.getAbsolutePath());
        }
      }

      String evaluatedContent = evaluateTemplateContent(workbenchTaskContext, config, templateContent);

      File templateFile = createTemplateFile(getBaseTemplateDirectory(workbenchTaskContext));
      fileSupport.writeFile(templateFile, evaluatedContent);
      fileSupport.setExecutable(templateFile, true);

      applicationDescription.setExecutablePath(templateFile.getAbsolutePath());
    }
  }

  /**
   * Evaluate the template content.
   *
   * @param workbenchTaskContext
   *          the workbench task context
   * @param config
   *          the configuration
   * @param content
   *          the template content
   *
   * @return the evaluated content
   */
  private String evaluateTemplateContent(WorkbenchTaskContext workbenchTaskContext, Configuration config,
      String content) {
    Map<String, Object> data = Maps.newHashMap();
    data.put(TEMPLATE_VARIABLE_WORKBENCH_TASK_CONTEXT, workbenchTaskContext);

    addAdditionalTemplateData(workbenchTaskContext, data);

    String templatedContent = workbenchTaskContext.getWorkbench().getTemplater().processStringTemplate(data, content);
    return config.evaluate(templatedContent);
  }

  /**
   * Add any additional template data needed for instantiating templates.
   *
   * @param workbenchTaskContext
   *          the workbench task context
   * @param data
   *          the template data
   */
  protected void addAdditionalTemplateData(WorkbenchTaskContext workbenchTaskContext, Map<String, Object> data) {
    // Default is add nothing
  }

  @Override
  public String toString() {
    return "NativeTaskFactory [applicationDescription=" + applicationDescription + "]";
  }
}
