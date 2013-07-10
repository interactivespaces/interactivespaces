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

# a resource for the Interactive Spaces Container.

# name of the resource
string name

# minimum and maximum version of the resource
string minimumVersion
string maximumVersion

# Is the resource required?
bool required

int32 location
int32 LOCATION_BOOTSTRAP = 0
int32 LOCATION_NONCORE = 1
int32 LOCATION_LIB_SYSTEM = 2
int32 LOCATION_CONFIG = 3
int32 LOCATION_ROOT = 4

# Resources available which fit the version range and name
LocatableResourceDescription[] resources
