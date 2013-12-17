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
string uuid

# Status of the operation this is a response for
int32 status
int32 STATUS_HEARTBEAT = 0
int32 STATUS_SHUTDOWN = 1
int32 STATUS_RESTARTED = 2
int32 STATUS_INSTALL = 3

# The payload contains a full status for the controller.
int32 STATUS_FULL = 4

# The payload contains a response for an activity install.
int32 STATUS_ACTIVITY_INSTALL = 5

# The payload contains a response for an activity deletion.
int32 STATUS_ACTIVITY_DELETE = 6
int32 STATUS_DATA_RESTORE = 7
int32 STATUS_DATA_CAPTURE = 8

# The payload contains a response for a container resource query.
int32 STATUS_CONTAINER_RESOURCE_QUERY = 9

# The payload contains a response for a container resource commit.
int32 STATUS_CONTAINER_RESOURCE_COMMIT = 10

# A string identifier giving more detail of the status
string statusCode

# More detail about the status, e.g. a stack trace
string statusDetail

# Any additional data for the message
string data_type
byte[] data

