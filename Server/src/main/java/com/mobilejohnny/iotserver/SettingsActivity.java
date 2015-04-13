package com.mobilejohnny.iotserver;

import android.os.Bundle;
import android.preference.*;

public class SettingsActivity extends PreferenceActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
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

    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_connection);

            final Preference prefConnection =  findPreference("connection_type");
            final Preference prefDestination =  findPreference("destination_type");
            final Preference prefPort =  findPreference("port");
            final Preference prefDeviceName =  findPreference("device_name");



            bindPreferenceSummaryToValue(prefConnection);
            bindPreferenceSummaryToValue(prefDestination);
            bindPreferenceSummaryToValue(prefDeviceName);
            bindPreferenceSummaryToValue(prefPort);

            prefDestination.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    String[] connectType = getResources().getStringArray(R.array.pref_destination_list_titles);
                    if(o.toString().equals(connectType[0]))
                    {
                        prefDeviceName.setEnabled(true);
                    }
                    else
                    {
                        prefDeviceName.setEnabled(false);
                    }
                    return sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,o);
                }
            });
        }
    }

}
