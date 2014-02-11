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

import com.google.common.io.Closeables;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.ByteUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * A resource signature using message digests.
 *
 * @author Keith M. Hughes
 */
public class MessageDigestResourceSignature implements ResourceSignature {

  /**
   * Buffer size for digesting stream.
   */
  private static final int COPY_BUFFER_SIZE = 4096;

  /**
   * Digest signature algorithm.
   */
  public static final String SIGNATURE_ALGORITHM = "SHA-512";

  @Override
  public String getBundleSignature(File bundleFile) {
    FileInputStream fis = null;

    try {
      fis = new FileInputStream(bundleFile);
      return getBundleSignature(fis);
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException(String.format("Could not create signature for file %s",
          bundleFile.getAbsolutePath()), e);
    } finally {
      Closeables.closeQuietly(fis);
    }
  }

  /**
   * Provide a bundle signature for the stream.
   * @param inputStream
   *          stream which to digest
   * @return bundle signature for the given stream
   */
  public String getBundleSignature(InputStream inputStream) {
    try {
      byte[] buffer = new byte[COPY_BUFFER_SIZE];
      MessageDigest digest = MessageDigest.getInstance(SIGNATURE_ALGORITHM);
      int len;
      while ((len = inputStream.read(buffer)) > 0) {
        digest.update(buffer, 0, len);
      }
      return ByteUtils.toHexString(digest.digest());
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("Could not calculate stream signature", e);
    }
  }
}
