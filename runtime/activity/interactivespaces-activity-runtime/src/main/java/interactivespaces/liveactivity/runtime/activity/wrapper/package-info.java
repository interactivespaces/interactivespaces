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

/**
 * Activity wrappers are used by the Interactive Spaces runtime to hide details of what the activity looks like or how
 * it is created. A given wrapper reference should only be used to create one instance at a time.
 *
 * <p>
 * Activities are instantiated with the {@link ActivityWrapper.newInstance()} method. When done with the instance,
 * {@link ActivityWrapper.done()} should be called.
 *
 * @author Keith M. Hughes
 */
package interactivespaces.liveactivity.runtime.activity.wrapper;

