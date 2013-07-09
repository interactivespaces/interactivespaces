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

package interactivespaces.util.data.resource;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.util.ByteUtils;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * A resource signature using message digests.
 *
 * @author Keith M. Hughes
 */
public class MessageDigestResourceSignature implements ResourceSignature {

  @Override
  public String getBundleSignature(File bundleFile) {
    FileInputStream fis = null;

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-512");

      byte[] buffer = new byte[(int) bundleFile.length()];
      fis = new FileInputStream(bundleFile);
      fis.read(buffer);
      fis.close();

      digest.update(buffer);

      return ByteUtils.toHexString(digest.digest());
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not create signature for file %s",
          bundleFile.getAbsolutePath()), e);
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (Exception e) {
          // Don't care
        }
      }
    }
  }

}
