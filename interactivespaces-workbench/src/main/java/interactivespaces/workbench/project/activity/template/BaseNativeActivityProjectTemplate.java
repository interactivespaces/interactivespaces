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

package interactivespaces.workbench.project.activity.template;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.project.ProjectConfigurationProperty;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.activity.ActivityProject;

import java.util.Map;

/**
 * A common base for interactive spaces native activity projects.
 *
 * @author Trevor Pering
 */
public class BaseNativeActivityProjectTemplate extends BaseActivityProjectTemplate {

  public static final String LANGUAGE = "base";

  /**
   * Construct a new instance.
   */
  public BaseNativeActivityProjectTemplate() {
    super("Generic Base Java Activity Project", LANGUAGE);
  }

  /**
   * Construct a new instance with the given non-default parameters.
   *
   * @param displayName
   *          ui display name
   * @param language
   *          template language
   */
  public BaseNativeActivityProjectTemplate(String displayName, String language) {
    super(displayName, language);
  }

  @Override
  public void onTemplateSetup(ProjectCreationSpecification spec, ActivityProject activityProject,
      Map<String, Object> fullTemplateData) {

    if (activityProject.getActivityClass() == null) {
      throw new SimpleInteractiveSpacesException("Activity class not set by subclass");
    }
  }
}
