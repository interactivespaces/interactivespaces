##
# Copyright (C) 2013 Google Inc.
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

# The response of a resource deployment commit to an Interactive Spaces container.

# An ID for the request.
string transaction_id

# Type of the request
int32 status

# This is the response to a commit which failed.
int32 STATUS_FAILURE = 0

# This is the response to a commit which suceeded.
int32 STATUS_SUCCESS = 1
