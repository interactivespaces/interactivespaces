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

package interactivespaces.service.control.opensoundcontrol;

/**
 * Useful constants for using Open Sound Control.
 *
 * @author Keith M. Hughes
 */
public class OpenSoundControlConstants {

  /**
   * int32 argument type for an Open Sound Control message argument.
   */
  public static final byte OPEN_SOUND_CONTROL_ARGUMENT_TYPE_INT32 = 0x69;

  /**
   * int64 argument type for an Open Sound Control message argument.
   */
  public static final byte OPEN_SOUND_CONTROL_ARGUMENT_TYPE_INT64 = 0x68;

  /**
   * float32 argument type for an Open Sound Control message argument.
   */
  public static final byte OPEN_SOUND_CONTROL_ARGUMENT_TYPE_FLOAT32 = 0x66;

  /**
   * float64 argument type for an Open Sound Control message argument.
   */
  public static final byte OPEN_SOUND_CONTROL_ARGUMENT_TYPE_FLOAT64 = 0x64;

  /**
   * string argument type for an Open Sound Control message argument.
   */
  public static final byte OPEN_SOUND_CONTROL_ARGUMENT_TYPE_STRING = 0x73;

  /**
   * blob argument type for an Open Sound Control message argument.
   */
  public static final byte OPEN_SOUND_CONTROL_ARGUMENT_TYPE_BLOB = 0x62;
}
