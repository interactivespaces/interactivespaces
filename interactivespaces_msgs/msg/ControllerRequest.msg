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

int32 operation

# Rwquest the full status of the controller.
int32 OPERATION_STATUS = 0

# Shut down the entire controller.
int32 OPERATION_SHUTDOWN_CONTROLLER = 1

# Shut down all activities in the controller.
int32 OPERATION_SHUTDOWN_ACTIVITIES = 2

# Shut down all activities in the controller.
int32 OPERATION_DEPLOY_LIVE_ACTIVITY = 3

# Any payload needed by the request.
byte[] payload

