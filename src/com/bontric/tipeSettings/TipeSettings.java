/**
 *@name TipeSettings
 *@author Benedikt John Wieder, Jakob Frick
 *
 * Implementation of the settings menu. 
 * static values are needed for reference
 */

package com.bontric.tipeSettings;


import com.bontric.tipeKeyboard.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

@SuppressWarnings("deprecation")
public class TipeSettings extends PreferenceActivity {

	
	
	public final static String SWIPE_SENSITIVITY = "swipe_sensitivity";
	
	/*
	 * charsets
	 */

	public final static String LANGUAGE = "cusLanguage";
	public final static String CHARSET = "cusCharset";
	public final static String SYMSET = "cusSymset";
	/*
	 * colors
	 */
	public final static String FONT_COLOR = "normFontColor";
	public final static String BACKGROUND_COLOR = "backgroundColor";
	public final static String CHARACTER_BG_LIGHT = "background_light";
	public final static String CHARACTER_BG_DARK = "background_dark";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

}
