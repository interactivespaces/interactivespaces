/*
 * Copyright (C) 2014 Google Inc.
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
 * The Open Sound Control service provides both Open Sound Control client and server implementations.
 *
 * <p>
 * Open Sound Control outgoing messages may be sent multiple times once they are created. This can be useful
 * if you wish to create a collection of messages that need to be sent frequently, they only need to be created once.
 *
 * @author keith M. Hughes
 */
package interactivespaces.service.control.opensoundcontrol;
