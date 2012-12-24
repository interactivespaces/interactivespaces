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

from interactivespaces.activity.impl.web import BaseWebActivity

class ExamplePythonWebActivity(BaseWebActivity):
    def onActivityActivate(self):
        self.log.info("Activated " + self.configuration.evaluate("${a}"))
        self.sendImageUrl("images/Geoffrey.jpg")

    def onActivityDeactivate(self):
        self.log.info("Deactivated " + self.configuration.evaluate("${a}"))
        self.sendImageUrl("images/Lion.jpg")

    def sendImageUrl(self, imageUrl):
        data = {}
        data["imageUrl"] = imageUrl
        msg = {}
        msg["data"] = data

        # Send data to all websocket connections
        self.sendAllWebSocketJson(data)

    def onNewWebSocketConnection(self, connectionId):
        self.log.info("Got web socket connection from connection " + connectionId)

    def onWebSocketClose(self, connectionId):
        self.log.info("Got web socket close from connection " + connectionId)

    def onWebSocketReceive(self, connectionId, data):
        self.log.info("Got web socket data from connection " + connectionId)
