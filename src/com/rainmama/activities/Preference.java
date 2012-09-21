package com.rainmama.activities;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.rainmama.R;

public class Preference extends SherlockPreferenceActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
