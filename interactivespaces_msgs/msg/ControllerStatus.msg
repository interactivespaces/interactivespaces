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

# UUID of the controller
string controller_uuid

# Status of the operation this is a response for
int32 status

# A controller heartbeat.
int32 STATUS_CONTROLLER_HEARTBEAT = 0

# A controller shutdown acknowledgement.
int32 STATUS_CONTROLLER_SHUTDOWN = 1

# The controller has restarted.
int32 STATUS_CONTROLLER_RESTARTED = 2

# This is a live activity status update.
# The payload contains a LiveActivityRuntimeStatus.
int32 STATUS_CONTROLLER_LIVE_ACTIVITY_RUNTIME_STATUS = 3

# The payload contains a full status for the controller.
int32 STATUS_CONTROLLER_FULL_STATUS = 4

# The payload contains a response for an activity install.
int32 STATUS_CONTROLLER_ACTIVITY_INSTALL = 5

# The payload contains a response for an activity deletion.
int32 STATUS_CONTROLLER_ACTIVITY_DELETE = 6

# The payload contains a response for a data restore.
int32 STATUS_CONTROLLER_DATA_RESTORE = 7

# The payload contains a response for a data capture.
int32 STATUS_CONTROLLER_DATA_CAPTURE = 8

# The payload contains a response for a container resource query.
int32 STATUS_CONTROLLER_CONTAINER_RESOURCE_QUERY = 9

# The payload contains a response for a container resource commit.
int32 STATUS_CONTROLLER_CONTAINER_RESOURCE_COMMIT = 10

# A string identifier giving more detail of the status
string statusCode

# More detail about the status, e.g. a stack trace
string statusDetail

# Any additional data for the message
string payload_type
byte[] payload

