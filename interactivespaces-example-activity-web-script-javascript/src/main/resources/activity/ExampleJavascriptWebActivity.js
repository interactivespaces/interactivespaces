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

interactivespaces.activity.impl.web.BaseWebActivity {
    onActivityActivate: function() {
        this.getLog().info("Activated " + this.getConfiguration().evaluate("${a}"));
        this.sendImageUrl("images/Geoffrey.jpg");
    }, 

    onActivityDeactivate: function() {
        this.getLog().info("Deactivated " + this.getConfiguration().evaluate("${a}"));
        this.sendImageUrl("images/Lion.jpg");
    }, 

    sendImageUrl: function(imageUrl) {
        data = {};
        data["imageUrl"] = imageUrl;
        msg = {};
        msg["data"] = data;

        // Send data to all websocket connections
        this.sendAllWebSocketJson(data);
    }, 

    onNewWebSocketConnection: function(connectionId) {
        this.getLog().info("Got web socket connection from connection " + connectionId);
    },

    onWebSocketClose: function(connectionId) {
        this.getLog().info("Got web socket close from connection " + connectionId);
    },

    onWebSocketReceive: function(connectionId, data) {
        this.getLog().info("Got web socket data from connection " + connectionId);
    }
}
