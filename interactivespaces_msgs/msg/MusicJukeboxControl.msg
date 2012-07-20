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

# Play a single track. The track info below is expected.
int32 OPERATION_PLAY_TRACK = 0

# Pause whatever is being played.
int32 OPERATION_PAUSE = 1

# Stop whatever the jukebox is doing
int32 OPERATION_STOP = 2

# Shuffle random tracks
int32 OPERATION_SHUFFLE = 3

# Queue up a single track. The track info below is expected.
int32 OPERATION_QUEUE_TRACK = 4

# The ID of the track to play. Can be null.
string id

# How many milliseoncds into the track to begin
int64 begin

# How many milliseconds of the track to play
int64 duration

