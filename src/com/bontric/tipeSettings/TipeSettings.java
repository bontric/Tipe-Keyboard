/**
 *@name TipeSettings
 *@author Benedikt John Wieder, Jakob Frick
 *
 * Implementation of the settings menu. 
 * static values are needed for reference
 */

package com.bontric.tipeSettings;


import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.bontric.tipeKeyboard.R;

@SuppressWarnings("deprecation")
public class TipeSettings extends PreferenceActivity {


    public final static String SWIPE_SENSITIVITY = "swipe_sensitivity";

	/*
     * charsets
	 */

    public final static String CHARSET = "languageCharset";
    public final static String CUSTOM_CHARSET = "cusCharset";
    public final static String CUSTOM_SYMSET = "cusSymset";
    /*
     * colors
     */
    public final static String FONT_COLOR = "normFontColor";
    public final static String BACKGROUND_COLOR = "backgroundColor";
    public final static String CHARACTER_BG_LIGHT = "background_light";

    public final static String SPACE_LEAVING_CHARACTER_AREA = "spaceLeavingCharacterArea";
    public final static String USE_HAPTIC_FEEDBACK = "useHapticFeedback";
    public final static String USE_AUTO_CAPITALIZATION = "autocapitalization";
    public final static String USE_CUSTOM_SYMSET = "useCustomSymset";
    public final static String USE_CUSTOM_CHARSET = "useCustomCharset";
    public final static String SHOW_SUGGESTIONS = "showSuggestions";
    public final static String USE_TAP_TAP_MODE = "tapTapMode";

    public final static String KEYBOARD_HEIGHT = "keyboard_height";
    public final static String KEYBOARD_WIDTH = "keyboard_width";
    public final static String LONGPRESS_TIMEOUT = "longpress_timeout";


    public static boolean settings_changed = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    public void onContentChanged() {
        super.onContentChanged();
        settings_changed = true;
    }

}
