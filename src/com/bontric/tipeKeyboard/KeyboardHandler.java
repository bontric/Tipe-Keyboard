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
import android.graphics.Point;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.WindowManager;

public class KeyboardHandler {
	// constant for now -> is going to be a setting
	public static int character_view_height;
	public static int lower_bar_view_height;
	public static int upper_bar_view_height;
	public static float default_font_size; // Make this variable

	public static boolean charset_changed = false;
	public static boolean shift_state = false;
	public static boolean is_symbol_set = false;
	public static boolean use_audio_feedback;
	public static boolean use_haptic_feedback;
	public static boolean use_auto_capitalization;
	public static boolean show_suggestions;

	public static String symbol_set;
	public static String character_set;
	public static String word_separators;

	public static int char_view_dark_color;
	public static int char_view_light_color;
	public static int default_font_color;
	public static int background_color;
	public static int candidate_view_background_color;

	public static int longpress_timeout;
	public static int keyboard_width;
	public static int keyboard_height;

	public static InputHandler input_connection = new InputHandler();

	private static SharedPreferences sharedPrefs;
	private static TipeView mTipeView;

	public static void init(Context context, TipeView tipeView) {

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

		// ===== Strings=====================

		if (sharedPrefs.getBoolean(TipeSettings.USE_CUSTOM_SYMSET, false)) {
			symbol_set = sharedPrefs.getString(TipeSettings.CUSTOM_SYMSET, "");
		} else {
			symbol_set = context.getResources().getString(R.string.SymbolSet);
		}
		character_set = sharedPrefs.getString(TipeSettings.CHARSET, context
				.getResources().getString(R.string.defaultCharset));
		word_separators = context.getResources().getString(
				R.string.word_separators);
		// ===== COLORS=====================

		char_view_dark_color = sharedPrefs.getInt(
				TipeSettings.CHARACTER_BG_DARK, Color.BLACK);
		char_view_light_color = sharedPrefs.getInt(
				TipeSettings.CHARACTER_BG_LIGHT, Color.DKGRAY);
		default_font_color = sharedPrefs.getInt(TipeSettings.FONT_COLOR,
				Color.WHITE);
		background_color = sharedPrefs.getInt(TipeSettings.BACKGROUND_COLOR,
				Color.BLACK);
		candidate_view_background_color = sharedPrefs.getInt(
				TipeSettings.CANDIDATE_VIEW_BACKGROUND, Color.BLACK);

		// ====== BOOLEANS ===
		use_audio_feedback = sharedPrefs.getBoolean(
				TipeSettings.USE_AUDIO_FEEDBACK, false);
		use_haptic_feedback = sharedPrefs.getBoolean(
				TipeSettings.USE_HAPTIC_FEEDBACK, false);
		use_auto_capitalization = sharedPrefs.getBoolean(
				TipeSettings.USE_AUTO_CAPITALIZATION, false);
		show_suggestions = sharedPrefs.getBoolean(
				TipeSettings.SHOW_SUGGESTIONS, false);

		// ======= ADVANCED ===
		longpress_timeout = (int) (250 + 500 * sharedPrefs.getFloat(
				TipeSettings.LONGPRESS_TIMEOUT, (float) 0.5));
		mTipeView = tipeView;

		// ====== Keyboard Size (TESTING) !====
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Point size = new Point();
		wm.getDefaultDisplay().getSize(size);
		;
		keyboard_height = (int) (size.y * (0.3 + 0.3 * sharedPrefs.getFloat(
				TipeSettings.KEYBOARD_HEIGHT, 1)));
		keyboard_width = (int) (size.x * (0.3 + 0.7 * sharedPrefs.getFloat(
				TipeSettings.KEYBOARD_WIDTH, 1)));
		character_view_height = (int) (0.65 * keyboard_height);
		lower_bar_view_height = (int) (0.20 * keyboard_height);
		upper_bar_view_height = (int) (0.15 * keyboard_height);
		default_font_size = (int) (30 + 10 * (sharedPrefs.getFloat(
				TipeSettings.KEYBOARD_HEIGHT, 1) + sharedPrefs.getFloat(
				TipeSettings.KEYBOARD_WIDTH, 1)));

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
