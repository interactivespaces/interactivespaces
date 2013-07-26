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

package interactivespaces.util.web;

import com.google.common.collect.Maps;

import interactivespaces.service.web.HttpResponseCode;
import interactivespaces.service.web.server.HttpDynamicRequestHandler;
import interactivespaces.service.web.server.HttpFileUpload;
import interactivespaces.service.web.server.HttpFileUploadListener;
import interactivespaces.service.web.server.HttpRequest;
import interactivespaces.service.web.server.HttpResponse;
import interactivespaces.service.web.server.internal.netty.NettyWebServer;
import interactivespaces.util.io.Files;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test the {@link HttpClientHttpContentCopier} class.
 *
 * @author Keith M. Hughes
 */
public class HttpClientHttpContentCopierTest {
  private static final String TEST_CONTENT = "This is a test";
  private Log log;
  private ScheduledExecutorService threadPool;
  private HttpClientHttpContentCopier copier;
  private NettyWebServer webServer;
  private int webServerPort;
  private String webServerUriPrefix;

  @Before
  public void setup() {
    log = new Jdk14Logger("goober"); // Mockito.mock(Log.class);

    threadPool = Executors.newScheduledThreadPool(100);

    copier = new HttpClientHttpContentCopier();
    copier.startup();

    webServerPort = 10031;
    webServerUriPrefix = "websockettest";
    webServer = new NettyWebServer("test-server", webServerPort, threadPool, log);
    webServer.addDynamicContentHandler("/" + webServerUriPrefix, true,
        new HttpDynamicRequestHandler() {

          @Override
          public void handle(HttpRequest request, HttpResponse response) {
            String path = request.getUri().getPath();

            if (path.endsWith("/error")) {
              response.setResponseCode(HttpResponseCode.FORBIDDEN);
            } else {
              try {
                response.getOutputStream().write(TEST_CONTENT.getBytes());
              } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }
          }
        });

    webServer.startup();
  }

  @After
  public void cleanup() {
    copier.shutdown();

    webServer.shutdown();

    threadPool.shutdown();
  }

  /**
   * Test a successful transfer copying from the remote.
   *
   * @throws Exception
   */
  @Test
  public void testCopyFromSuccess() throws Exception {
    File destination = getTempFile();

    String sourceUri = getUrlPrefix();

    testCopyFromSuccessfulTransfer(destination, sourceUri);
  }

  /**
   * Test having a failure copying from the remote and then attempting to read
   * again.
   *
   * @throws Exception
   */
  @Test
  public void testCopyFromFailure() throws Exception {
    File destination = getTempFile();

    String sourceUri = getUrlPrefix();

    try {
      copier.copy(sourceUri + "error", destination);

      Assert.fail();
    } catch (Exception e) {
      // Expected
    }

    testCopyFromSuccessfulTransfer(destination, sourceUri);
  }

  /**
   * Test a file upload.
   */
  @Test
  public void testFileUpload() throws Exception {
    File source = getTempFile();
    final File destination = getTempFile();

    Files.writeFile(source, TEST_CONTENT);

    final AtomicReference<Map<String, String>> receivedParameters =
        new AtomicReference<Map<String, String>>();
    final CountDownLatch latch = new CountDownLatch(1);

    webServer.setHttpFileUploadListener(new HttpFileUploadListener() {
      @Override
      public void handleHttpFileUpload(HttpFileUpload fileUpload) {
        fileUpload.moveTo(destination);
        receivedParameters.set(fileUpload.getParameters());
        latch.countDown();
      }
    });

    String sourceParameterName = "myfile";
    Map<String, String> expectedParameters = Maps.newHashMap();
    expectedParameters.put("foo", "bar");
    expectedParameters.put("bletch", "spam");

    copier.copyTo(getUrlPrefix(), source, sourceParameterName, expectedParameters);

    Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));

    Assert.assertEquals(TEST_CONTENT, Files.readFile(destination).trim());
    Assert.assertEquals(expectedParameters, receivedParameters.get());
  }

  /**
   * @param destination
   * @param sourceUri
   */
  private void testCopyFromSuccessfulTransfer(File destination, String sourceUri) {
    copier.copy(sourceUri + "success", destination);
    String content = Files.readFile(destination);
    Assert.assertEquals(TEST_CONTENT, content.trim());
  }

  /**
   * @return
   */
  private String getUrlPrefix() {
    return String.format("http://127.0.0.1:%d/%s/", webServerPort, webServerUriPrefix);
  }

  /**
   * Get an appropriately named temp file.
   *
   * @return The temp file
   *
   * @throws IOException
   */
  private File getTempFile() throws IOException {
    File tempdir = new File(System.getProperty("java.io.tmpdir"));
    File file = File.createTempFile("interactivespaces-", "test", tempdir);
    file.deleteOnExit();
    return file;
  }

}
