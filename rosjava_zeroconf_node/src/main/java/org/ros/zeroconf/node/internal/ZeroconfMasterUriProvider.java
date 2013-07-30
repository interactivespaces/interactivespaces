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

package org.ros.zeroconf.node.internal;

import org.ros.exception.RosRuntimeException;
import org.ros.master.uri.MasterUriProvider;
import org.ros.zeroconf.common.ZeroconfRosMasterInfo;
import org.ros.zeroconf.common.selector.ZeroconfServiceSelector;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * A {@link MasterUriProvider} which uses zeroconf for URI resolution.
 *
 * @author Keith M. Hughes
 */
public class ZeroconfMasterUriProvider implements MasterUriProvider {

  /**
   * The service selector for zeroconf.
   */
  private ZeroconfServiceSelector<ZeroconfRosMasterInfo> masterZeroconfServiceSelector;

  public ZeroconfMasterUriProvider(
      ZeroconfServiceSelector<ZeroconfRosMasterInfo> masterZeroconfServiceSelector) {
    this.masterZeroconfServiceSelector = masterZeroconfServiceSelector;
  }

  @Override
  public URI getMasterUri() throws RosRuntimeException {
    // TODO(keith): get better protocol selection once rosjava supports
    // other protocols
    return masterZeroconfServiceSelector.getService().getUri("http");
  }

  @Override
  public URI getMasterUri(long timeout, TimeUnit unit) {
    ZeroconfRosMasterInfo masterInfo = masterZeroconfServiceSelector.getService();
    if (masterInfo != null) {
      // TODO(keith): get better protocol selection once rosjava supports
      // other protocols
      return masterInfo.getUri("http");
    } else {
      return null;
    }
  }
}
