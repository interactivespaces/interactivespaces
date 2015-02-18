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
import interactivespaces.util.process.BaseNativeApplicationRunnerListener;
import interactivespaces.util.process.NativeApplicationDescription;
import interactivespaces.util.process.NativeApplicationRunner;
import interactivespaces.util.process.NativeApplicationRunner.NativeApplicationRunnerState;
import interactivespaces.util.process.NativeApplicationRunnerCollection;

import java.util.concurrent.CountDownLatch;

/**
 * A workbench task instance factory for native applications.
 *
 * @author Keith M. Hughes
 */
public class NativeWorkbenchTaskFactory implements WorkbenchTaskFactory {

  /**
   * The application description.
   */
  private NativeApplicationDescription applicationDescription;

  /**
   * Construct a new factory.
   *
   * @param applicationDescription
   *          the application description
   */
  public NativeWorkbenchTaskFactory(NativeApplicationDescription applicationDescription) {
    this.applicationDescription = applicationDescription;
  }

  @Override
  public WorkbenchTask newTask() {
    // TODO Auto-generated method stub
    return new BaseWorkbenchTask() {
      @Override
      public void perform(WorkbenchTaskContext workbenchTaskContext) throws InteractiveSpacesException {
        workbenchTaskContext.getWorkbench().getLog()
            .info(String.format("Native application %s", applicationDescription));

        NativeApplicationRunnerCollection nativeApplicationRunners = workbenchTaskContext.getNativeApplicationRunners();
        NativeApplicationRunner runner = nativeApplicationRunners.newNativeApplicationRunner();

        Configuration config = getConfiguration(workbenchTaskContext);
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

  @Override
  public String toString() {
    return "NativeTaskFactory [applicationDescription=" + applicationDescription + "]";
  }
}
