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

# result of a deletion of a live activity

# UUID of the live activity
string uuid

# Time deleted on controller
int64 time_deleted

int32 status
int32 STATUS_SUCCESS = 0
int32 STATUS_FAILURE = 1
int32 STATUS_DOESNT_EXIST = 2


# A string identifier giving more detail of the status
string statusCode

# More detail about the status, e.g. a stack trace
string statusDetail
