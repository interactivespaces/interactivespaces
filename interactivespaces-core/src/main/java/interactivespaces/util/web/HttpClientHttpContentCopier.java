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

package interactivespaces.util.web;

import interactivespaces.InteractiveSpacesException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An {@link HttpContentCopier} which uses Apache HttpClient.
 *
 * @author Keith M. Hughes
 */
public class HttpClientHttpContentCopier implements HttpContentCopier {

  /**
   * Number of bytes in the copy buffer.
   */
  private static final int BUFFER_SIZE = 4096;

  /**
   * The HTTPClient instance which does the actual transfer.
   */
  private HttpClient client;

  @Override
  public void startup() {
    client = new DefaultHttpClient();
  }

  @Override
  public void shutdown() {
    client.getConnectionManager().shutdown();
  }

  @Override
  public void copy(String sourceUri, File destination) {
    HttpEntity entity = null;
    try {
      HttpGet httpget = new HttpGet(sourceUri);
      HttpResponse response = client.execute(httpget);

      entity = response.getEntity();

      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        if (entity != null) {
          InputStream in = entity.getContent();
          try {
            transferFile(in, destination);
          } finally {
            in.close();
          }

        }
      } else {
        throw new InteractiveSpacesException(String.format(
            "Server returned bad status code %d for source URI %s during HTTP copy", statusCode,
            sourceUri));
      }
    } catch (InteractiveSpacesException e) {
      throw e;
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format(
          "Could not read source URI %s during HTTP copy", sourceUri), e);
    } finally {
      if (entity != null) {
        try {
          EntityUtils.consume(entity);
        } catch (IOException e) {
          throw new InteractiveSpacesException(String.format(
              "Could not consume entity content for %s during HTTP copy", sourceUri), e);
        }
      }
    }
  }

  @Override
  public void copyTo(String destinationUri, File source, String sourceParameterName,
      Map<String, String> params) {
    HttpEntity entity = null;
    try {
      HttpPost httppost = new HttpPost(destinationUri);

      MultipartEntity mpEntity = new MultipartEntity();

      mpEntity.addPart(sourceParameterName, new FileBody(source));

      if (params != null) {
        for (Entry<String, String> entry : params.entrySet()) {
          mpEntity.addPart(entry.getKey(), new StringBody(entry.getValue()));
        }
      }

      httppost.setEntity(mpEntity);

      HttpResponse response = client.execute(httppost);

      entity = response.getEntity();

      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        throw new InteractiveSpacesException(String.format(
            "Server returned bad status code %d for source URI %s during HTTP copy", statusCode,
            destinationUri));
      }
    } catch (InteractiveSpacesException e) {
      throw e;
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format(
          "Could not send file to destination URI %s during HTTP copy", destinationUri), e);
    } finally {
      if (entity != null) {
        try {
          EntityUtils.consume(entity);
        } catch (IOException e) {
          throw new InteractiveSpacesException(String.format(
              "Could not consume entity content for %s during HTTP copy", destinationUri), e);
        }
      }
    }
  }

  /**
   * Transfer the content from the HTTP input stream to the destination file.
   *
   * @param in
   *          the HTTP result
   * @param destination
   *          the file to copy the content to
   *
   * @throws IOException
   */
  private void transferFile(InputStream in, File destination) throws IOException {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(destination);

      byte[] buffer = new byte[BUFFER_SIZE];

      int len;
      while ((len = in.read(buffer)) > 0)
        out.write(buffer, 0, len);

      out.flush();
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          // Don't care.
        }
      }
    }
  }
}
