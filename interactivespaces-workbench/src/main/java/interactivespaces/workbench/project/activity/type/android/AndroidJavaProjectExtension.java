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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.util.process.NativeCommandsExecutor;
import interactivespaces.workbench.project.builder.ProjectBuildContext;
import interactivespaces.workbench.project.java.JavaProjectExtension;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * A java activity extension for Android.
 *
 * @author Keith M. Hughes
 */
public class AndroidJavaProjectExtension implements JavaProjectExtension {

  /**
   * The configuration property name for the Android SDK home.
   */
  public static final String PROPERTY_ANDROID_SDK_HOME = "android.sdk.home";

  /**
   * The configuration property name for the buildtools to use, relative to
   * {@link #PROPERTY_ANDROID_SDK_HOME}.
   */
  public static final String PROPERTY_ANDROID_SDK_BUILDTOOLS = "android.buildtools";

  /**
   * The configuration property name for the Android platform version to use.
   */
  public static final String PROPERTY_ANDROID_PLATFORM = "android.platform";

  @Override
  public void addToClasspath(List<File> classpath, ProjectBuildContext context) {
    Configuration properties = context.getProject().getConfiguration();
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

    context.getWorkbench().addAlternateControllerExtensionsClasspath(classpath, "android");
  }

  @Override
  public void postProcessJar(ProjectBuildContext context, File jarFile) {
    Configuration configuration = context.getProject().getConfiguration();
    String platformToolsDirectory =
        configuration.getRequiredPropertyString(PROPERTY_ANDROID_SDK_HOME) + "/"
            + configuration.getRequiredPropertyString(PROPERTY_ANDROID_SDK_BUILDTOOLS) + "/";

    // TODO(keith): make configuration properties with the args interpolated in
    List<String> dxCommand =
        Arrays.asList(platformToolsDirectory + "dx", "--dex", "--output=classes.dex", jarFile.getAbsolutePath());

    List<String> aaptCommand =
        Arrays.asList(platformToolsDirectory + "aapt", "add", jarFile.getAbsolutePath(), "classes.dex");

    @SuppressWarnings("unchecked")
    List<List<String>> commands = Lists.newArrayList(dxCommand, aaptCommand);

    NativeCommandsExecutor executor = new NativeCommandsExecutor();
    executor.executeCommands(commands);
  }
}
