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

package interactivespaces.workbench.project.activity.type.android;

import com.google.common.collect.Lists;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.activity.ProjectBuildContext;
import interactivespaces.workbench.project.activity.builder.java.JavaProjectExtensions;
import interactivespaces.workbench.util.NativeCommandsExecutor;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A java activity extension for Android
 *
 * @author Keith M. Hughes
 */
public class AndroidJavaprojectExtension implements JavaProjectExtensions {

  /**
   * The configuration property name for the Android SDK home.
   */
  public static final String PROPERTY_ANDROID_SDK_HOME = "android.sdk.home";

  /**
   * The configuration property name for the Android platform version to use.
   */
  public static final String PROPERTY_ANDROID_PLATFORM = "android.platform";

  @Override
  public void addToClasspath(List<File> classpath, InteractiveSpacesWorkbench workbench) {
    SimpleConfiguration properties = workbench.getWorkbenchConfig();
    String androidJar =
        properties.getRequiredPropertyString(PROPERTY_ANDROID_SDK_HOME) + "/platforms/"
            + properties.getRequiredPropertyString(PROPERTY_ANDROID_PLATFORM) + "/android.jar";
    File androidJarFile = new File(androidJar);

    if (androidJarFile.exists()) {
      classpath.add(androidJarFile);
    } else {
      throw new InteractiveSpacesException(String.format("Could not find Android jar file %s",
          androidJarFile.getAbsolutePath()));
    }

    workbench.addAlternateControllerExtensionsClasspath(classpath, "android");

  }

  @Override
  public void postProcessJar(ProjectBuildContext context, File jarFile) {
    String platformToolsDirectory =
        context.getWorkbench().getWorkbenchConfig()
            .getRequiredPropertyString(PROPERTY_ANDROID_SDK_HOME)
            + "/platform-tools/";

    List<String> dxCommand = Lists.newArrayList();
    dxCommand.add(platformToolsDirectory + "dx");
    dxCommand.add("--dex");
    dxCommand.add("--output=classes.dex");
    dxCommand.add(jarFile.getAbsolutePath());

    List<String> aaptCommand = Lists.newArrayList();
    aaptCommand.add(platformToolsDirectory + "aapt");
    aaptCommand.add("add");
    aaptCommand.add(jarFile.getAbsolutePath());
    aaptCommand.add("classes.dex");

    @SuppressWarnings("unchecked")
    List<List<String>> commands = Lists.newArrayList(dxCommand, aaptCommand);

    NativeCommandsExecutor executor = new NativeCommandsExecutor();
    executor.executeCommands(commands);
  }

  /**
   * Get the required property from the workbench properties
   *
   * @param config
   *          the workbench configuration
   * @param property
   *          the name of the required property
   *
   * @return the Android SDK Home
   *
   * @throws InteractiveSpacesException
   *           the property is missing or empty
   */
  private String getRequiredProperty(Map<String, String> config, String property) {
    String value = config.get(property);
    if (value != null && !value.trim().isEmpty()) {
      return value;
    } else {
      throw new InteractiveSpacesException(String.format("Missing or empty workbench property %s",
          property));
    }
  }
}
