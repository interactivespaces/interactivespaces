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

# A resource deployment query request for the Interactive Spaces Container.
# The query will be asking if a set of resources are available on the remote container.

# An ID for the request.
string transaction_id

# Type of the request
int32 type

# This is a query to decide if specific resources are already in the remote
# container. The queries field will have contents.
int32 TYPE_SPECIFIC_QUERY = 0

# A generic query. Looking to discover everything available in the container.
int32 TYPE_GENERAL_QUERY = 0

# The individual items in the query.
ContainerResourceQueryItem[] items
