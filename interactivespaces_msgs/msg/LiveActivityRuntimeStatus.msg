##
# Copyright (C) 2012 Google Inc.
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
##

string uuid
int32 status

# The status of the live activity is unknown.
int32 STATUS_LIVE_ACTIVITY_UNKNOWN = 0

# The live activity does not exist on the controller.
int32 STATUS_LIVE_ACTIVITY_DOESNT_EXIST = 1

# The live activity has failed to deploy on the controller.
int32 STATUS_LIVE_ACTIVITY_DEPLOY_FAILURE = 2

# The live activity is in the READY state.
int32 STATUS_LIVE_ACTIVITY_READY = 3

# The live activity has failed to start up.
int32 STATUS_LIVE_ACTIVITY_STARTUP_FAILURE = 4

# The live activity is running.
int32 STATUS_LIVE_ACTIVITY_RUNNING = 5

# The live activity has failed to activate.
int32 STATUS_LIVE_ACTIVITY_ACTIVATE_FAILURE = 6

# The live activity is active.
int32 STATUS_LIVE_ACTIVITY_ACTIVE = 7

# The live activity has failed to deactivate.
int32 STATUS_LIVE_ACTIVITY_DEACTIVATE_FAILURE = 8

# The live activity has failed to shut down.
int32 STATUS_LIVE_ACTIVITY_SHUTDOWN_FAILURE = 9

# The live activity has crashed.
int32 STATUS_LIVE_ACTIVITY_CRASH = 10

# The crashed live activity is attempting a restart.
int32 STATUS_LIVE_ACTIVITY_CRASH_ATTEMPT_RESTART = 11

# The live activity is attempting to start up.
int32 STATUS_LIVE_ACTIVITY_STARTUP_ATTEMPT = 12

# A deployment of the live activity is being attempted.
int32 STATUS_LIVE_ACTIVITY_DEPLOY_ATTEMPT = 13

# The live activity is attempting to activate.
int32 STATUS_LIVE_ACTIVITY_ACTIVATE_ATTEMPT = 14

# The live activity is attempting to deactivate.
int32 STATUS_LIVE_ACTIVITY_DEACTIVATE_ATTEMPT = 15

# The live activity is attempting to shut down.
int32 STATUS_LIVE_ACTIVITY_SHUTDOWN_ATTEMPT = 16

# A string identifier giving more detail of the status.
string statusCode

# More detail about the status, e.g. a stack trace.
string statusDetail
