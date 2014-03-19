package interactivespaces.controller.client.node;

import interactivespaces.common.ResourceRepositoryUploadChannel;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.web.HttpContentCopier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 */
public class SpaceControllerDataBundleManagerTest {

  // Test constants for mocking real system.
  private static final String TEST_DESTINATION = "http://test/foo.bar";
  private static final String TEST_UUID = "ab7abc79ebf97ce";
  private static final File   TEST_TEMP_DIRECTORY = new File("temp");
  private static final File   TEST_DATA_DIRECTORY = new File("data");

  /**
   * Container class for collecting test results.
   */
  class TestResults {
    File zipTarget;
    File unzipSource;
    File unzipBaseLocation;
    String copySourceUri;
    File copyDestination;
    String copyToDestinationUri;
    File copyToSource;
    String copyToParameterName;
    Map<String, String> copyToParams;
  }

  /**
   * The primary class under test.
   */
  private SpaceControllerDataBundleManager dataBundleManager;

  /**
   * Container object used for collecting mocked test results.
   */
  private TestResults testResults;

  @Before
  @SuppressWarnings("unchecked")
  public void setup() {
    SpaceControllerControl spaceController = Mockito.mock(SpaceControllerControl.class, RETURNS_DEEP_STUBS);
    when(spaceController.getControllerInfo().getUuid()).thenReturn(TEST_UUID);
    when(spaceController.getSpaceEnvironment().getFilesystem().getTempDirectory(anyString()))
        .thenAnswer(new Answer<File>() {
          @Override
          public File answer(InvocationOnMock invocation) throws Exception {
            return new File(TEST_TEMP_DIRECTORY, (String) invocation.getArguments()[0]);
          }
        });
    when(spaceController.getSpaceEnvironment().getFilesystem().getDataDirectory())
        .thenReturn(TEST_DATA_DIRECTORY);

    testResults = new TestResults();

    FileSupport fileSupport = Mockito.mock(FileSupport.class);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Exception {
        testResults.unzipSource = (File) invocation.getArguments()[0];
        testResults.unzipBaseLocation = (File) invocation.getArguments()[1];
        return null;
      }
    }).when(fileSupport).unzip(any(File.class), any(File.class));

    doAnswer(new Answer<ZipOutputStream>() {
      @Override
      public ZipOutputStream answer(InvocationOnMock invocation) throws Exception {
        testResults.zipTarget = (File) invocation.getArguments()[0];
        return Mockito.mock(ZipOutputStream.class);
      }
    }).when(fileSupport).createZipOutputStream(any(File.class));

    doAnswer(new Answer<Boolean>() {
      @Override
      public Boolean answer(InvocationOnMock invocation) throws Exception {
        return true;
      }
    }).when(fileSupport).rename(any(File.class), any(File.class));

    HttpContentCopier contentCopier = Mockito.mock(HttpContentCopier.class);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Exception {
        testResults.copySourceUri = (String) invocation.getArguments()[0];
        testResults.copyDestination = (File) invocation.getArguments()[1];
        return null;
      }
    }).when(contentCopier).copy(anyString(), any(File.class));

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Exception {
        testResults.copyToDestinationUri = (String) invocation.getArguments()[0];
        testResults.copyToSource = (File) invocation.getArguments()[1];
        testResults.copyToParameterName = (String) invocation.getArguments()[2];
        testResults.copyToParams = (Map<String, String>) invocation.getArguments()[3];
        return null;
      }
    }).when(contentCopier).copyTo(anyString(), any(File.class), anyString(), any(Map.class));

    dataBundleManager = new SpaceControllerDataBundleManager(contentCopier, fileSupport);
    dataBundleManager.setSpaceController(spaceController);
  }

  @After
  public void tearDown() {
    dataBundleManager = null;
    testResults = null;
  }

  private boolean isValidTempPath(File path) {
    while ((path = path.getParentFile()) != null) {
      if (TEST_TEMP_DIRECTORY.equals(path)) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void testCaptureControllerDataBundle() {
    dataBundleManager.captureControllerDataBundle(TEST_DESTINATION);
    assertTrue(isValidTempPath(testResults.zipTarget));
    assertEquals(TEST_DESTINATION, testResults.copyToDestinationUri);
    assertEquals(testResults.zipTarget, testResults.copyToSource);
    assertEquals(ResourceRepositoryUploadChannel.DATA_BUNDLE_UPLOAD.getChannelId(),
        testResults.copyToParameterName);
    assertEquals(TEST_UUID, testResults.copyToParams.get("uuid"));
  }

  @Test
  public void testRestoreControllerDataBundle() {
    dataBundleManager.restoreControllerDataBundle(TEST_DESTINATION);
    assertTrue(isValidTempPath(testResults.copyDestination));
    assertEquals(TEST_DESTINATION, testResults.copySourceUri);
    assertEquals(testResults.unzipSource, testResults.copyDestination);
    assertNotSame(testResults.unzipBaseLocation, testResults.unzipSource);
  }
}
