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

package interactivespaces.android.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * The root Android Activity for controlling Interactive Spaces.
 *
 * @author Keith M. Hughes
 */
public class InteractiveSpacesRootActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Immediately start the foreground service
        Intent serviceIntent = new Intent(this, InteractiveSpacesService.class);
        startService(serviceIntent);
    }
}
