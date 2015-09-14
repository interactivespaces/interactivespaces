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

/**
 * An adapter for {@link Source} instances that provide a bridge to another
 * system, e.g. a user interface element for the source file.
 *
 * @author Keith M. Hughes
 */
public interface SourceAdapter {

  /**
   * Get the program source for this adapter.
   *
   * @return The program source for this adapter.
   */
  Source getSource();

  /**
   * Set the latest content of the adapter.
   *
   * @param content
   *          The new content of the adapter.
   */
  void setContent(String content);

  /**
   * Get the latest content in the adapter.
   *
   * @return The latest content in the adapter.
   */
  String getContent();

  /**
   * Have the contents of the adapter been modified?
   *
   * @return True if the contents of the adapter have been modified, false
   *         otherwise.
   */
  boolean isContentModified();

  /**
   * Set whether or not the content is modified.
   *
   * @param modified
   *          True if modified, false otherwise.
   */
  void setContentModified(boolean modified);

  /**
   * Copy the content of the adapter to the source.
   */
  void synchronizeToSource();
}
