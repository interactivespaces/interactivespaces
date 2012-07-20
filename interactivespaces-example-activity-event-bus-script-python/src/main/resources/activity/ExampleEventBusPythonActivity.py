##
# Copyright (C) 2012 Google Inc.
#  
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not
#  use this file except in compliance with the License. You may obtain a copy of
#  the License at
#  
#  http://www.apache.org/licenses/LICENSE-2.0
#  
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#  License for the specific language governing permissions and limitations under
#  the License.
##

from interactivespaces.activity.impl import BaseActivity
from interactivespaces.event import EventListener

class ExampleEventBusPythonActivity(BaseActivity):
    def onActivityStartup(self):
        self.log.info("Python Event Bus Example Activity startup")
        class MyEventListener(EventListener):
            def __init__(self, spaceEnvironment, uuid):
                self.spaceEnvironment = spaceEnvironment
                self.uuid = uuid

            def onEvent(self, event):
                if event.source != self.uuid:
                    self.spaceEnvironment.log.info("Event received %s %s" % (event.type, event.source))
                    self.spaceEnvironment.log.info(event.data)
                else:
                    self.spaceEnvironment.log.info("I hear my own voice inside my head")
                    
        self.eventListener = MyEventListener(self.spaceEnvironment, self.uuid)
        self.eventSubscriber = self.spaceEnvironment.getValue('interactivespaces.event.bus.subscriber')
        if self.eventSubscriber:
            self.log.info("Have event subscriber")
            self.eventSubscriber.addEventListener("activity", self.eventListener)
        else:
            self.log.info("Have no event subscriber")

        self.eventPublisher = self.spaceEnvironment.getValue('interactivespaces.event.bus.publisher')
        if self.eventPublisher:
            self.log.info("Have event publisher")
        else:
            self.log.info("Have no event publisher")

            
    def onActivityShutdown(self):
        self.log.info("Python Event Bus Example Activity shutting down")

    def onActivityActivate(self):
        self.log.info("Python Event Bus Example Activity activated")
        if self.eventPublisher:
            data = {'action': 'activate'}
            self.eventPublisher.createAndPublishEvent("activity", self.uuid, data)

    def onActivityDeactivate(self):
        self.log.info("Python Event Bus Example Activity deactivated")
        if self.eventPublisher:
            data = {'action': 'deactivate'}
            self.eventPublisher.createAndPublishEvent("activity", self.uuid, data)

    def onActivityCleanup(self):
        if self.eventSubscriber:
          self.eventSubscriber.removeEventListener("activity", self.eventListener)
