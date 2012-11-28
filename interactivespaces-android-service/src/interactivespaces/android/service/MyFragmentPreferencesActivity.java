/**
 * 
 */
package interactivespaces.android.service;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Show the preferences screen.
 * 
 * @author Keith M. Hughes
 */
public class MyFragmentPreferencesActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
	}
}
