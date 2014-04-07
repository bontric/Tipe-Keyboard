/**
 *@name SoftKeyboard
 *@author Benedikt John Wieder, Jakob Frick
 *
 * This class holds all information about the Keyboard.
 * It also handles Input-Independent events (like shift  / Symbolset)
 * 
 *  
 */

package com.bontric.tipeKeyboard;

import com.bontric.tipeSettings.TipeSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;

public class KeyboardHandler {
	// constant for now -> is going to be a setting
	public static final int CHARACTER_VIEW_HEIGHT = 300;

	public static boolean isShift;
	public static boolean isSymbolSet = false;
	public static String SymbolSet;
	public static String CharacterSet;
	public static int CharViewDarkColor = Color.BLACK;
	public static int CharViewLightColor = Color.DKGRAY;
	public static int CharViewFontColor = Color.WHITE;
	private static SharedPreferences sharedPrefs;

	public static float CharViewFontSize = 40; // Make this variable 

	public static void init(Context context) {
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		SymbolSet = sharedPrefs.getString(TipeSettings.SYMSET,"CHARSET ERROR! REPORT THIS");
		CharacterSet = sharedPrefs.getString(TipeSettings.CHARSET,
				"CHARSET ERROR! REPORT THIS");
		CharViewDarkColor = sharedPrefs.getInt(TipeSettings.CHARACTER_BG_DARK, Color.BLACK);
		CharViewDarkColor = sharedPrefs.getInt(TipeSettings.CHARACTER_BG_LIGHT, Color.DKGRAY);
		CharViewFontColor = sharedPrefs.getInt(TipeSettings.FONT_COLOR, Color.WHITE);
		
	}
}
