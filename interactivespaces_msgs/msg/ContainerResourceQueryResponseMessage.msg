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

# The response of a resource deployment query to an Interactive Spaces container.

# An ID for the request.
string transaction_id

# Type of the request
int32 status

# The container does not contain all members of the specific query.
# The conatiner contents will be supplied so that a complete set of needs can
# be deployed.
int32 STATUS_SPECIFIC_QUERY_NOT_SATISFIED = 0

# The specific query was satisfied, all elements of the query are available.
int32 STATUS_SPECIFIC_QUERY_SATISFIED = 1

# This is the response to a general query for everything in the container.
int32 STATUS_GENERAL_QUERY_RESPONSE = 2
