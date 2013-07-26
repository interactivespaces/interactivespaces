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

package interactivespaces.master.ui.internal.web.admin;

import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;
import interactivespaces.network.client.NetworkInformationClient;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring MVC controller for troubleshooting.
 *
 * @author Keith M. Hughes
 */
@org.springframework.stereotype.Controller
public class TroubleshootController extends BaseSpaceMasterController {

  /**
   * client for picking up network information.
   */
  private NetworkInformationClient networkInformationClient;

  /**
   * Display a list of all controllers.
   *
   * @return Model and view for controller list display.
   */
  @RequestMapping("/troubleshoot/all.html")
  public ModelAndView listControllers() {
    ModelAndView mav = getModelAndView();
    mav.setViewName("troubleshoot/TroubleshootAll");

    return mav;
  }

  @RequestMapping(value = "/troubleshoot/topics.html", method = RequestMethod.GET)
  public ModelAndView networkTopicController() {
    ModelAndView mav = getModelAndView();
    mav.setViewName("troubleshoot/TroubleshootTopicsAll");

    mav.addObject("topics", networkInformationClient.getTopics());

    return mav;
  }

  @RequestMapping(value = "/troubleshoot/nodes.html", method = RequestMethod.GET)
  public ModelAndView networkNodeController() {
    ModelAndView mav = getModelAndView();
    mav.setViewName("troubleshoot/TroubleshootNodesAll");

    mav.addObject("nodes", networkInformationClient.getNodes());

    return mav;
  }

  /**
   * @param networkInformationClient
   *          the networkInformationClient to set
   */
  public void setNetworkInformationClient(NetworkInformationClient networkInformationClient) {
    this.networkInformationClient = networkInformationClient;
  }

}
