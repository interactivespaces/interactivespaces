/*
 * Copyright (C) 2013 Google Inc.
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

/**
 * Some useful classes for working with the javax.swing package in Interactive Spaces.
 *
 * <p>
 * These classes are necessary because Swing runs its own threads and it is necessary
 * to make sure the Swing threads, frames, and other UI elements are properly shut down
 * when a live activity shuts down.
 *
 * @author Keith M. Hughes
 */
package interactivespaces.util.ui.swing;
