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

package interactivespaces.workbench.project.activity;

import interactivespaces.workbench.project.Project;

/**
 * A simple implementation of a {@link Source}.
 *
 * @author Keith M. Hughes
 */
public class SimpleSource implements Source {

  /**
   * The project this file is associated with.
   */
  private Project project;

  /**
   * The path of where this source is in the file system.
   */
  private String path;

  /**
   * The content stored in this source.
   */
  private String content = "";

  /**
   * The adapter for this source. It can be null.
   */
  private SourceAdapter adapter;

  public SimpleSource() {
    // Do nothing
  }

  public SimpleSource(Project project, String path) {
    this.project = project;
    this.path = path;
  }

  @Override
  public Project getProject() {
    return project;
  }

  @Override
  public void setProject(Project project) {
    this.project = project;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String getName() {
    String name = null;
    if (path != null) {
      int index = path.lastIndexOf("/");
      if (index != -1)
        name = path.substring(index + 1);
      else
        name = path;
    }

    return name;
  }

  @Override
  public String getContent() {
    copyFromAdapter();

    return content;
  }

  @Override
  public void setContent(String content) {
    this.content = content;

    copyToAdapter();
  }

  @Override
  public SourceAdapter getAdapter() {
    return adapter;
  }

  @Override
  public void setAdapter(SourceAdapter a) {
    // Only do all this work if the adapter is actually changing.
    if (adapter != a) {
      // The outgoing adapter, if modified, must be copied.
      copyFromAdapter();

      adapter = a;
      copyToAdapter();
    }
  }

  /**
   * Copy the contents of the adapter, if any.
   */
  private void copyFromAdapter() {
    if (adapter != null && adapter.isContentModified())
      content = adapter.getContent();
  }

  /**
   * Copy the contents to the adapter, if any.
   */
  private void copyToAdapter() {
    if (adapter != null)
      adapter.setContent(content);
  }
}
