// Copyright 2013 Google Inc. All Rights Reserved.

package interactivespaces.master.server.services.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.common.ResourceRepositoryUploadChannel;
import interactivespaces.master.server.services.ActiveSpaceController;
import interactivespaces.resource.repository.ResourceRepositoryServer;
import interactivespaces.util.data.resource.CopyableResource;
import interactivespaces.util.data.resource.CopyableResourceListener;

/**
 * @author peringknife@google.com (Trevor Pering)
 */
public class BasicMasterDataBundleManagerTest {

  /**
   * Current instance of main class under test.
   */
  BasicMasterDataBundleManager masterDataBundleManager;

  /**
   * Mock controller to use.
   */
  ActiveSpaceController activeSpaceController;

  /**
   * Mock resource repository.
   */
  ResourceRepositoryServer resourceRepositoryServer;

  /**
   * Transient test results container.
   */
  TestResults testResults;

  // Various test constants.
  static final String TEST_RESOURCE_CATEGORY = "data";
  static final String TEST_UUID = "1bd6a9762679cba97d6b";
  static final String TEST_RESOURCE_VERSION = "noversion";
  static final String TEST_URI_SEPARATOR = "-";
  static final String TEST_OUTPUT_CONTENTS = "Qjwhqekjwh ekqjwhekqjweh q\n" +
      "WJQHD kqjhd qwDHOuwhqdouwh qWhodwq wqjdhwkqjdhqwk djhqKWJDhkqwjd\n";
  static final String TEST_RESOURCE = combineArgs(new Object[] {
      TEST_RESOURCE_CATEGORY, TEST_UUID, TEST_RESOURCE_VERSION } );

  /**
   * Container class for all the test variables.
   */
  private static class TestResults {
    ByteArrayOutputStream testOutputStream;
    String outputUri;
    String sourceUri;
    String destinationUri;
    ResourceRepositoryUploadChannel uploadChannel;
    CopyableResourceListener resourceListener;
  }

  @Before
  @SuppressWarnings("unchecked")
  public void setup() {
    testResults = new TestResults();

    activeSpaceController = Mockito.mock(ActiveSpaceController.class, RETURNS_DEEP_STUBS);
    when(activeSpaceController.getController().getUuid()).thenReturn(TEST_UUID);

    resourceRepositoryServer = Mockito.mock(ResourceRepositoryServer.class);

    doAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Exception {
        return combineArgs(invocation.getArguments());
      }
    }).when(resourceRepositoryServer).getResourceUri(anyString(), anyString(), anyString());

    doAnswer(new Answer<OutputStream>() {
      @Override
      public OutputStream answer(InvocationOnMock invocation) throws Exception {
        assertNull(testResults.testOutputStream);
        testResults.testOutputStream = new ByteArrayOutputStream();
        testResults.outputUri = combineArgs(invocation.getArguments());
        return testResults.testOutputStream;
      }
    }).when(resourceRepositoryServer).createResourceOutputStream(anyString(), anyString(),
        anyString());

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Exception {
        testResults.uploadChannel = (ResourceRepositoryUploadChannel) invocation.getArguments()[0];
        testResults.resourceListener = (CopyableResourceListener) invocation.getArguments()[1];
        return null;
      }
    }).when(resourceRepositoryServer).registerResourceUploadListener(
        any(ResourceRepositoryUploadChannel.class), any(CopyableResourceListener.class));

    masterDataBundleManager = new TestBasicMasterDataBundleManager();
    masterDataBundleManager.setRepositoryServer(resourceRepositoryServer);
    masterDataBundleManager.startup();
  }

  static String combineArgs(Object[] arguments) {
    return arguments[0] + TEST_URI_SEPARATOR + arguments[1] + TEST_URI_SEPARATOR + arguments[2];
  }

  @After
  public void tearDown() {
    masterDataBundleManager = null;
    activeSpaceController = null;
    testResults = null;
  }

  @Test
  public void testCaptureControllerDataBundle() {
    masterDataBundleManager.captureControllerDataBundle(activeSpaceController);

    assertNull(testResults.testOutputStream);
    assertNull(testResults.outputUri);
    assertNull(testResults.sourceUri);
    assertEquals(TEST_RESOURCE, testResults.destinationUri);
  }

  @Test
  public void testRestoreControllerDataBundle() {
    masterDataBundleManager.restoreControllerDataBundle(activeSpaceController);

    assertEquals(ResourceRepositoryUploadChannel.DATA_BUNDLE_UPLOAD, testResults.uploadChannel);
    testResults.resourceListener.onUploadSuccess(TEST_UUID, new TestCopyableResource());

    assertEquals(TEST_OUTPUT_CONTENTS, testResults.testOutputStream.toString());
    assertEquals(TEST_RESOURCE, testResults.outputUri);
    assertEquals(TEST_RESOURCE, testResults.sourceUri);
    assertNull(testResults.destinationUri);
  }

  class TestCopyableResource implements CopyableResource {
    public boolean moveTo(File destination) {
      throw new IllegalStateException("Should not be calling moveTo");
    }
    public boolean copyTo(OutputStream destination) {
      try {
        testResults.testOutputStream.write(TEST_OUTPUT_CONTENTS.getBytes());
      } catch (IOException e) {
        throw new InteractiveSpacesException("Error writing to output", e);
      }
      return true;
    }
  }

  class TestBasicMasterDataBundleManager extends BasicMasterDataBundleManager {
    @Override
    protected void sendControllerDataBundleCaptureRequest(ActiveSpaceController controller,
        String destinationUri) {
      testResults.destinationUri = destinationUri;
    }

    @Override
    protected void sendControllerDataBundleRestoreRequest(ActiveSpaceController controller,
        String sourceUri) {
      testResults.sourceUri = sourceUri;
    }
  }
}
