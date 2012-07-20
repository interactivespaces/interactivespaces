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

# UUID of the activity.
string uuid

# The operation to perform on the activity.
int32 operation

# if the activity does not want to do an operation,
# force it to. This will not work with every operation.
bool force

# Force the activity to send its current status
int32 OPERATION_STATUS = 0

# Start the activity up
int32 OPERATION_STARTUP = 1

# Activate the activity.
int32 OPERATION_ACTIVATE = 2

# Deactivate the activity.
int32 OPERATION_DEACTIVATE = 3

# Shut the activity down.
int32 OPERATION_SHUTDOWN = 4

# Configure the activity. The payload contains the
# ActivityConfigurationRequest object.
int32 OPERATION_CONFIGURE = 5

# Delete the activity from the controller.
int32 OPERATION_DELETE = 6

byte[] data
