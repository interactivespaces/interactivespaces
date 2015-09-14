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

package interactivespaces.master.ui.internal.web.misc;

import interactivespaces.master.api.messages.MasterApiMessageSupport;
import interactivespaces.master.api.messages.MasterApiMessages;
import interactivespaces.master.server.services.ExtensionManager;
import interactivespaces.master.ui.internal.web.BaseSpaceMasterController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * A spring MVC controller for extensions.
 *
 * @author Keith M. Hughes
 */
@Controller
public class ExtensionController extends BaseSpaceMasterController {

  /**
   * The manager for extensions.
   */
  private ExtensionManager extensionManager;

  @RequestMapping(value = "/extension/{extensionName}.json", method = RequestMethod.POST)
  public @ResponseBody Map<String, ? extends Object> executeExtension(@PathVariable String extensionName,
      @RequestBody Object argsIn, HttpServletResponse response) {
    if (Map.class.isAssignableFrom(argsIn.getClass())) {
      @SuppressWarnings("unchecked")
      Map<String, Object> args = (Map<String, Object>) argsIn;

      return extensionManager.evaluateApiExtension(extensionName, args);
    } else {
      return MasterApiMessageSupport.getFailureResponse(MasterApiMessages.MESSAGE_SPACE_CALL_ARGS_NOMAP,
          MasterApiMessages.MESSAGE_SPACE_DETAIL_CALL_ARGS_NOMAP);
    }
  }

  /**
   * Set the extension manager.
   *
   * @param extensionManager
   *          the extension manager to use
   */
  public void setExtensionManager(ExtensionManager extensionManager) {
    this.extensionManager = extensionManager;
  }
}
