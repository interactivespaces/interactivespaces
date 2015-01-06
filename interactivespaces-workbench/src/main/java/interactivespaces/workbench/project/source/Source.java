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

package interactivespaces.workbench.project.source;

import interactivespaces.workbench.project.Project;

/**
 * A project source file.
 *
 * @author Keith M. Hughes
 */
public interface Source {

  /**
   * Get the project this source is in.
   *
   * @return the project this source is in, can be {@code null}
   */
  Project getProject();

  /**
   * Set the project this source is in.
   *
   * @param project
   *          the project this source is in, can be {@code null}
   */
  void setProject(Project project);

  /**
   * Get the name of the file.
   *
   * @return the name of the file
   */
  String getName();

  /**
   * Get the path for this source relative to the containing project.
   *
   * @return the path
   */
  String getPath();

  /**
   * Set the path for this source relative to the containing project.
   *
   * @param path
   *          the path
   */
  void setPath(String path);

  /**
   * Get the complete content of the source.
   *
   * @return the complete content of the source
   */
  String getContent();

  /**
   * Set the complete content of the source.
   *
   * @param content
   *          the complete content of the source
   */
  void setContent(String content);

  /**
   * Set the {@link SourceAdapter} for this source.
   *
   * @param adapter
   *          the adapter for this source, can be {@code null}
   */
  void setAdapter(SourceAdapter adapter);

  /**
   * Get the {@link SourceAdapter} for this source.
   *
   * @return the adapter for this source, can be {@code null}
   */
  SourceAdapter getAdapter();
}
