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

	
	/*
	 * swype
	 */
	public final static String swypeActive = "swypeActive";
	public final static String swipeSensitivity = "swipe_sensitivity";
	
	/*
	 * charsets
	 */

	public final static String cusLanguage = "cusLanguage";
	public final static String useAdvancedCharset = "useAdvancedCharset";
	public final static String useCustomCharset = "useCustomCharset";
	public final static String useCustomSymset = "useCustomSymset";
	public final static String cusCharset = "cusCharset";
	public final static String cusSymset = "cusSymset";
	/*
	 * colors
	 */
	public final static String normFontColor = "normFontColor";
	public final static String emphFontColor = "emphFontColor";
	public final static String lightBgColor = "background_light";
	public final static String darkBgColor = "background_dark";
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

}
