/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.master.server.services.internal.ros;

import com.google.common.collect.Lists;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.osgi.common.RosEnvironment;
import org.ros.osgi.master.core.internal.BaseRosMasterController;

/**
 * Test the StandardMasterRosContext.
 *
 * @author Keith M. Hughes
 */
public class StandardMasterRosContextTest {

  private StandardMasterRosContext context;

  private TestRosMasterController rosMasterController;

  @Mock
  private RosEnvironment rosEnvironment;

  @Mock
  private NodeConfiguration nodeConfiguration;

  @Mock
  private ConnectedNode masterNode;

  @Mock
  private Log log;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    rosMasterController = new TestRosMasterController();

    context = new StandardMasterRosContext(rosMasterController);
    context.setLog(log);
    context.setRosEnvironment(rosEnvironment);

    Mockito.when(rosEnvironment.getPublicNodeConfigurationWithNodeName()).thenReturn(nodeConfiguration);

    // Fake ROSJava signaling a connection event.
    Mockito.when(rosEnvironment.newNode(nodeConfiguration, Lists.newArrayList(context.getMasterNodeListener())))
        .thenAnswer(new Answer<ConnectedNode>() {
          @Override
          public ConnectedNode answer(InvocationOnMock invocation) throws Throwable {
            context.getMasterNodeListener().onStart(masterNode);
            return masterNode;
          }
        });
  }

  /**
   * Test starting up the context.
   */
  @Test
  public void testStartup() {
    context.startup();

    Assert.assertEquals(rosEnvironment, rosMasterController.getRosEnvironment());
    Mockito.verify(nodeConfiguration).setNodeName(MasterRosContext.ROS_NODENAME_INTERACTIVESPACES_MASTER);
    Mockito.verify(rosEnvironment).newNode(nodeConfiguration, Lists.newArrayList(context.getMasterNodeListener()));
    Assert.assertEquals(masterNode, context.getMasterNode());
  }

  /**
   * Test shutting down the context.
   */
  @Test
  public void testShutdown() {
    context.startup();

    context.shutdown();
    Mockito.verify(masterNode).shutdown();

    // Wrong pieces in place for the node to call its listeners, so call directly.
    context.getMasterNodeListener().onShutdownComplete(context.getMasterNode());
    Assert.assertTrue(rosMasterController.isShutdownCalled());
  }

  /**
   * A ROS Master controller that merely signals startup and shutdown.
   *
   * @author Keith M. Hughes
   */
  private class TestRosMasterController extends BaseRosMasterController {

    /**
     * {@code true} if shutdown was called.
     */
    private boolean shutdownCalled = false;

    @Override
    public void startup() {
      signalRosMasterStartup();
    }

    @Override
    public void shutdown() {
      signalRosMasterShutdown();
      shutdownCalled = true;
    }

    /**
     * Was shutdown called?
     *
     * @return {@code true} if shutdown called
     */
    public boolean isShutdownCalled() {
      return shutdownCalled;
    }
  }
}
