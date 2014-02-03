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

package interactivespaces.service.image.depth.internal.openni2;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.image.depth.internal.openni2.libraries.NiTE2Library.NiteStatus;
import interactivespaces.service.image.depth.internal.openni2.libraries.OpenNI2Library;
import interactivespaces.service.image.depth.internal.openni2.libraries.OpenNI2Library.OniStatus;

import org.bridj.IntValuedEnum;
import org.bridj.Pointer;

/**
 * Support for use of OpenNI2 and NiTE2.
 *
 * @author Keith M. Hughes
 */
public class OpenNi2Support {

  /**
   * Get the extended error message from an OpenNI call that failed.
   *
   * @param baseMessage
   *          the base message
   * @param status
   *          the status from the OpenNI call
   *
   * @throws InteractiveSpacesException
   *           the exception with the extended messaging
   */
  public static void throwExtendedOpenNIError(String baseMessage, IntValuedEnum<OniStatus> status)
      throws InteractiveSpacesException {
    Pointer<Byte> errorPointer = OpenNI2Library.oniGetExtendedError();

    throw new SimpleInteractiveSpacesException(String.format("%s, status was %s: %s", baseMessage, status,
        errorPointer.getCString()));
  }

  /**
   * Get the extended error message from an OpenNI call that failed.
   *
   * @param baseMessage
   *          the base message
   * @param status
   *          the status from the OpenNI call
   *
   * @throws InteractiveSpacesException
   *           the exception with the extended messaging
   */
  public static void throwExtendedNiteError(String baseMessage, IntValuedEnum<NiteStatus> status)
      throws InteractiveSpacesException {
    Pointer<Byte> errorPointer = OpenNI2Library.oniGetExtendedError();

    throw new SimpleInteractiveSpacesException(String.format("%s, status was %s: %s", baseMessage, status,
        errorPointer.getCString()));
  }
}
