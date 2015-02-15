/*
 * Copyright (C) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

void setup() {
  Serial.begin(9600);
}

void loop() {
  int val1 = analogRead(0);
  
  // The value is sent in raw bytes, not a string.
  Serial.write(val1 >> 8);
  Serial.write(val1 & 0xff);
    
  int val2 = analogRead(1);
  
  // The value is sent in raw bytes, not a string.
  Serial.write(val2 >> 8);
  Serial.write(val2 & 0xff);

  delay(250);
}
