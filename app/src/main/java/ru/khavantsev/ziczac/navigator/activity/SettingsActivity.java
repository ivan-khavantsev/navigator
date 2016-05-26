package ru.khavantsev.ziczac.navigator.activity;

import android.preference.PreferenceActivity;
import android.os.Bundle;
import ru.khavantsev.ziczac.navigator.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }
}
