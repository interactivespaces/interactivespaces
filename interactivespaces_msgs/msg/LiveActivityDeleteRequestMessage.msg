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

# goal definition for deleting a live activity

# UUID of the live activity
string uuid

# Identifying name of the activity
string identifying_name

# Version of the activity
string version

# Should the deletion be forced?
int32 force

# The deletion should not be forced
int32 FORCE_FALSE = 0

# The deletion should be forced
int32 FORCE_TRUE = 1