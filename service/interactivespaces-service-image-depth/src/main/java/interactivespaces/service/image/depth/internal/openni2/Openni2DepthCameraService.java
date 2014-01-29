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

package interactivespaces.service.image.depth.internal.openni2;

import interactivespaces.service.BaseSupportedService;
import interactivespaces.service.image.depth.DepthCameraEndpoint;
import interactivespaces.service.image.depth.DepthCameraService;

import com.google.common.collect.Lists;

import com.primesense.nite.NiTE;
import org.apache.commons.logging.Log;
import org.openni.DeviceInfo;
import org.openni.OpenNI;

import java.util.List;

/**
 * A depth camera service using OpenNI2.
 *
 * @author Keith M. Hughes
 */
public class Openni2DepthCameraService extends BaseSupportedService implements DepthCameraService {

  @Override
  public String getName() {
    return DepthCameraService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    OpenNI.initialize();
    NiTE.initialize();

    getSpaceEnvironment().getExecutorService().submit(new Runnable() {

      @Override
      public void run() {
        DepthCameraEndpoint endpoint = null;
        try {
          endpoint = newDepthCameraEndpoint(getSpaceEnvironment().getLog());
          endpoint.startup();

          Thread.sleep(20000);
        } catch (Exception e) {
          getSpaceEnvironment().getLog().error("Endpoint failed", e);
        } finally {
          if (endpoint != null) {
            try {
              endpoint.shutdown();
            } catch (Exception e) {
              getSpaceEnvironment().getLog().error("Endpoint shutdown failed", e);
            }
          }
        }

      }

    });
  }

  @Override
  public List<String> getDepthCamerasAvailable() {
    List<String> cameras = Lists.newArrayList();

    for (DeviceInfo device : OpenNI.enumerateDevices()) {
      cameras.add(device.getUri());
    }

    return cameras;
  }

  @Override
  public DepthCameraEndpoint newDepthCameraEndpoint(Log log) {
    return new Openni2DepthCameraEndpoint(log);
  }
}
