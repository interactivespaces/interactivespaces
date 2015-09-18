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

package interactivespaces.bridge.message.ros;

/**
 * A compiler for bridge specifications.
 *
 * @author Keith M. Hughes
 */
public interface RosMessageBridgeSpecificationCompiler {

  /**
   * Compile the source and give a specification.
   *
   * @param <SourceType>
   *          the type of the source message
   * @param <DestinationType>
   *          the type of the destination message
   * @param source
   *          the specification source
   *
   * @return the compiled specification
   */
  <SourceType, DestinationType> RosMessageBridgeSpecification<SourceType, DestinationType> compile(String source);
}
