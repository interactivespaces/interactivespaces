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

package interactivespaces.service.mail.receiver.internal;

import interactivespaces.service.BaseSupportedService;
import interactivespaces.service.mail.receiver.MailReceiver;
import interactivespaces.service.mail.receiver.MailReceiverService;

import org.apache.commons.logging.Log;

/**
 * A {@link MailReceiverService} which uses Dumbster.
 *
 * @author Keith M. Hughes
 */
public class DumbsterMailReceiverService extends BaseSupportedService implements
    MailReceiverService {

  @Override
  public String getName() {
    return MailReceiverService.SERVICE_NAME;
  }

  @Override
  public MailReceiver newMailReceiver(int port, Log log) {
    return new DumbsterMailReceiver(port, log);
  }
}
