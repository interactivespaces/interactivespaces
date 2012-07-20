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

class ExamplePythonActivity(BaseActivity):
    def onActivityStartup(self):
        releaseTest()
        example.test()
        self.getLog().info("Simple Python Example Activity startup")

    def onActivityShutdown(self):
        self.getLog().info("Simple Python Example Activity shutting down")

    def onActivityActivate(self):
        self.getLog().info("Simple Python Example Activity activated")

    def onActivityDeactivate(self):
        self.getLog().info("Simple Python Example Activity deactivated")
