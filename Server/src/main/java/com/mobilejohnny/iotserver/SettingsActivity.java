package com.mobilejohnny.iotserver;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            getFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new GeneralPreferenceFragment())
                    .commit();
        }
        else
        {
            addPreferencesFromResource(R.xml.pref_connection);

            bindPreferenceSummaryToValue(findPreference("connection_type"));
            bindPreferenceSummaryToValue(findPreference("port"));
            bindPreferenceDevice( findPreference("destination_type"), findPreference("device_name"));
        }
    }

    private static void bindPreferenceDevice(Preference prefDestination, final Preference prefDeviceName) {
        bindPreferenceSummaryToValue(prefDestination);
        bindPreferenceSummaryToValue(prefDeviceName);

        prefDestination.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String[] connectType = preference.getContext().getResources().getStringArray(R.array.pref_destination_list_titles);
                if (o.toString().equals(connectType[0])) {
                    prefDeviceName.setEnabled(true);
                } else {
                    prefDeviceName.setEnabled(false);
                }
                return sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, o);
            }
        });
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            }  else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_connection);

            bindPreferenceSummaryToValue(findPreference("connection_type"));
            bindPreferenceSummaryToValue(findPreference("port"));

            bindPreferenceDevice(findPreference("destination_type"), findPreference("device_name"));
        }
    }

}
