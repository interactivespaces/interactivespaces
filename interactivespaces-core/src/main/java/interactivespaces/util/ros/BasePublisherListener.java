/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.util.ros;

import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.PublisherListener;

/**
 * An implementation of the {@link PublisherListener} which provides default
 * implementations for all methods which do nothing.
 *
 * @author Keith M. Hughes
 */
public class BasePublisherListener<T> implements PublisherListener<T> {

  @Override
  public void onMasterRegistrationSuccess(Publisher<T> registrant) {
    // Default is do nothing.
  }

  @Override
  public void onMasterRegistrationFailure(Publisher<T> registrant) {
    // Default is do nothing.
  }

  @Override
  public void onMasterUnregistrationSuccess(Publisher<T> registrant) {
    // Default is do nothing.
  }

  @Override
  public void onMasterUnregistrationFailure(Publisher<T> registrant) {
    // Default is do nothing.
  }

  @Override
  public void onNewSubscriber(Publisher<T> publisher, SubscriberIdentifier subscriberIdentifier) {
    // Default is do nothing.
  }

  @Override
  public void onShutdown(Publisher<T> publisher) {
    // Default is do nothing.
  }
}