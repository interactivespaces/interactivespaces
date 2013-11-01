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

package interactivespaces.service.comm.chat.internal.xmpp.smack;

import interactivespaces.service.BaseSupportedService;
import interactivespaces.service.comm.chat.ChatConnection;
import interactivespaces.service.comm.chat.ChatService;

/**
 * A {@link ChatService} for XMPP using the Smack libraries.
 *
 * @author Keith M. Hughes
 */
public class SmackXmppChatService extends BaseSupportedService implements ChatService {

  @Override
  public String getName() {
    return ChatService.SERVICE_NAME;
  }

  @Override
  public ChatConnection newChatConnection(String username, String password) {
    return new SmackXmppChatConnection(username, password);
  }
}
