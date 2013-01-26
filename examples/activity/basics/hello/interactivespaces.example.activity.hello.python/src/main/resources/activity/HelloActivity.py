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

from interactivespaces.example import releaseTest
import example

from interactivespaces.activity.impl import BaseActivity

class HelloActivity(BaseActivity):
    def onActivityStartup(self):
        releaseTest()
        example.test()
        self.log.info("Hello Python Activity startup")

    def onActivityActivate(self):
        self.log.info("Hello Python Activity activated")

    def onActivityDeactivate(self):
        self.log.info("Hello Python Activity deactivated")

    def onActivityShutdown(self):
        self.log.info("Hello Python Activity shutting down")

    def onActivityCleanup(self):
        self.log.info("Hello Python Activity cleanup")

    def onActivityFailure(self):
        self.log.info("Hello Python Activity failure")

    def onActivityCheckState(self):
        self.log.info("Hello Python Activity checking state")
        return True

    def onActivityConfigurationUpdate(self, update):
        self.log.info("Hello Python Activity config update %s" % update)
