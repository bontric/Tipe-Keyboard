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

import java.util.Locale;

import com.bontric.tipeSettings.TipeSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.WindowManager;

public class KeyboardHandler {
	// constant for now -> is going to be a setting
	public static int character_view_height = 300;
	public static int keyboard_width;
	public static int lower_bar_view_height = 100;
	public static int upper_bar_view_height = 100;
	public static float default_font_size = 40; // Make this variable

	public static boolean charset_changed = false;
	public static boolean shift_state = false;
	public static boolean is_symbol_set = false;

	public static String symbol_set;
	public static String character_set;
	
	public static int char_view_dark_color = Color.BLACK;
	public static int char_view_light_color = Color.DKGRAY;
	public static int char_view_font_color = Color.WHITE;

	public static InputHandler input_connection = new InputHandler();

	public static int background_color = Color.BLACK;

	private static SharedPreferences sharedPrefs;
	private static TipeView mTipeView;

	public static void setLayoutView() {

	}

	public static void init(Context context, TipeView mTView) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		keyboard_width = display.getWidth();

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		symbol_set = sharedPrefs.getString(TipeSettings.SYMSET,
				"CHARSET ERROR! REPORT THIS");
		character_set = sharedPrefs.getString(TipeSettings.CHARSET,
				"CHARSET ERROR! REPORT THIS");
		char_view_dark_color = sharedPrefs.getInt(
				TipeSettings.CHARACTER_BG_DARK, Color.BLACK);
		char_view_dark_color = sharedPrefs.getInt(
				TipeSettings.CHARACTER_BG_LIGHT, Color.DKGRAY);
		char_view_font_color = sharedPrefs.getInt(TipeSettings.FONT_COLOR,
				Color.WHITE);
		background_color = sharedPrefs.getInt(TipeSettings.BACKGROUND_COLOR,
				Color.BLACK);

		mTipeView = mTView;

	}

	public static void handleShift() {

		/*
		 * String.toUpperCase() is handling the German "ß" wrong (replaces it
		 * with "SS")... so this is a rather bad 'n' dirty fix..but i have no
		 * other idea than rewriting the toUpperCase() function
		 */
		if (shift_state && !is_symbol_set) {

			String cs = character_set.replace('ß', '\uffff');
			character_set = cs.toUpperCase(Locale.GERMAN)
					.replace('\uffff', 'ß');

			/*
			 * note this keyboard is for german use right now.. work on locale
			 * one day @ben
			 */

		} else {

			String cs = character_set.replace('ß', '\uffff');
			character_set = cs.toLowerCase(Locale.GERMAN)
					.replace('\uffff', 'ß');
		}
		charset_changed = true;
		mTipeView.invalidate();
	}

	public static void handleSym() {
		is_symbol_set = !is_symbol_set;
		charset_changed = true;
		mTipeView.invalidate();
	}
}
