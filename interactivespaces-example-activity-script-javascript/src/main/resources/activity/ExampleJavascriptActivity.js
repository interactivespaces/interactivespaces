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

interactivespaces.activity.impl.BaseActivity {
    onActivitySetup: function() {
        this.getLog().info("Simple Javascript Example Activity setup");
    },

    onActivityStartup: function() {
        this.getLog().info("Simple Javascript Example Activity startup");
    },

    onActivityActivate: function() {
        this.getLog().info("Simple Javascript Example Activity activate");
    },

    onActivityDeactivate: function() {
        this.getLog().info("Simple Javascript Example Activity deactivate");
    },

    onActivityShutdown: function() {
        this.getLog().info("Simple Javascript Example Activity shutdown");
    },

    onActivityCleanup: function() {
        this.getLog().info("Simple Javascript Example Activity cleanup");
    },

    onActivityFailure: function() {
        this.getLog().error("Simple Javascript Example Activity failure");
    },

    onActivityCheckState: function() {
        this.getLog().info("Simple Javascript Example Activity checking state");
        return true;
    },

    onActivityConfigurationUpdate: function(update) {
        this.getLog().info("Simple Javascript Example Activity config update " + update);
    },
}
