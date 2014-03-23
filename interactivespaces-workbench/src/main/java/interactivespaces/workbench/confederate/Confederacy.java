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

package interactivespaces.workbench.confederate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.TemplateFile;
import interactivespaces.workbench.project.TemplateVar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A confederacy of projects.
 *
 * @author Keith M. Hughes
 */
public class Confederacy {

  private final List<Project> projectList = Lists.newArrayList();

  private File baseDirectory;

  private File specificationSource;

  private final List<TemplateFile> templateFiles = Lists.newArrayList();

  private final List<TemplateVar> templateVars = Lists.newArrayList();

  public List<Project> getProjectList() {
    return projectList;
  }

  public void addProject(Project project) {
    projectList.add(project);
  }

  public File getBaseDirectory() {
    return baseDirectory;
  }

  public void setBaseDirectory(File baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  public File getSpecificationSource() {
    return specificationSource;
  }

  public void setSpecificationSource(File specDirectory) {
    this.specificationSource = specDirectory;
  }

  public List<TemplateFile> getTemplateFiles() {
    return templateFiles;
  }

  public void addTemplateFile(TemplateFile templateFile) {
    templateFiles.add(templateFile);
  }

  public List<TemplateVar> getTemplateVars() {
    return templateVars;
  }

  public void addTemplateVar(TemplateVar templateVar) {
    templateVars.add(templateVar);
  }

}
