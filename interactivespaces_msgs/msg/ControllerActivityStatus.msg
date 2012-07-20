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

int32 STATUS_UNKNOWN = 0
int32 STATUS_DOESNT_EXIST = 1
int32 STATUS_DEPLOY_FAILURE = 2
int32 STATUS_READY = 3
int32 STATUS_STARTUP_FAILURE = 4
int32 STATUS_RUNNING = 5
int32 STATUS_ACTIVATE_FAILURE = 6
int32 STATUS_ACTIVE = 7
int32 STATUS_DEACTIVATE_FAILURE = 8
int32 STATUS_SHUTDOWN_FAILURE = 9
int32 STATUS_CRASH = 10
int32 STATUS_CRASH_ATTEMPT_RESTART = 11
int32 STATUS_STARTUP_ATTEMPT = 12
int32 STATUS_DEPLOY_ATTEMPT = 13
int32 STATUS_ACTIVATE_ATTEMPT = 14
int32 STATUS_DEACTIVATE_ATTEMPT = 15
int32 STATUS_SHUTDOWN_ATTEMPT = 16
