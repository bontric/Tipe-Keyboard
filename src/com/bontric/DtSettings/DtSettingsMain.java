/*
 * @author Benedikt Wieder
 */


package com.bontric.DtSettings;

import com.bontric.DoubleTab.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
@SuppressWarnings("deprecation")
public class DtSettingsMain extends PreferenceActivity {

	public final static String bWS = "swypeActive";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}



}
