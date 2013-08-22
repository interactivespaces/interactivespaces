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

int32 transfer_type

# Represents the transfer of the controller's permanent data bundle.
int32 TRANSFER_TYPE_CONTROLLER_DATA_PERMANENT = 0

# The transfer get/post URL to use for transferring the data bundle.
string transfer_uri
