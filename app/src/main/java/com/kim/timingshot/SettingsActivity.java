package com.kim.timingshot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment(), null)
                .commit();

        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if( key.equals("bgm") ) {
                    if (sharedPreferences.getBoolean(key, true)) {
                        startService(new Intent(SettingsActivity.this, MainBgmService.class));
                    } else {
                        stopService(new Intent(SettingsActivity.this, MainBgmService.class));
                    }
                }
            }
        });
    }

    public static class SettingsFragment extends PreferenceFragment{
        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_activity);
        }
    }
}
