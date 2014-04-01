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

package interactivespaces.util.uuid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * A {@link UuidGenerator} which uses the Java native class.
 *
 * @author Keith M. Hughes
 */
public class JavaUuidGenerator implements UuidGenerator {

  @Override
  public void startup() {
    // Nothing to do.
  }

  @Override
  public void shutdown() {
    // Nothing to do.
  }

  @Override
  public String newUuid() {
    return UUID.randomUUID().toString();
  }

  @Override
  public String newNameUuid(String namespace, String name) {
    UUID namespaceUuid = UUID.fromString(namespace);

    long msb = namespaceUuid.getMostSignificantBits();
    long lsb = namespaceUuid.getLeastSignificantBits();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      // The bytes are written into the stream from the most significant byte to
      // the least significant.  position says how far to shift to get the current
      // byte we are interested in aligned with the lower 8 bits that will be written
      // into the output stream. Note it gradually moves from most significant byte to
      // least significant byte.
      int position = 56;
      for (int i = 0; i < 8; i++) {
        outputStream.write((byte) (msb >>> position));
        position -= 8;
      }
      position = 56;
      for (int i = 0; i < 8; i++) {
        outputStream.write((byte) (lsb >>> position));
        position -= 8;
      }
      outputStream.write(name.getBytes());
    } catch (IOException e) {
      // Will not happen since in memory.
    }

    return UUID.nameUUIDFromBytes(outputStream.toByteArray()).toString();
  }
}
