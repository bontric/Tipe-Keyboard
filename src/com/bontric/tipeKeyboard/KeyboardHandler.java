/**
 *@name SoftKeyboard
 *@author Benedikt John Wieder, Jakob Frick
 *
 * This class holds all information about the Keyboard.
 * It also handles Input-Independent events (like shift  / Symbolset)
 *
 *Copyright  2014 Benedikt Wieder, Jakob Frick

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.bontric.tipeKeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import com.bontric.tipeSettings.TipeSettings;

import java.util.Locale;

class KeyboardHandler {
    // constant for now -> is going to be a setting
    public static int character_view_height;
    public static int lower_bar_view_height;
    public static int upper_bar_view_height;
    public static float default_font_size;
    public static float candidate_font_size = 35;

    public static boolean charset_changed = false;
    public static boolean shift_state = true;
    public static boolean is_symbol_set = false;
    public static boolean use_haptic_feedback = false;
    public static boolean use_auto_capitalization = true;
    public static boolean show_suggestions = true;
    public static boolean no_boundries = true;
    public static boolean use_tap_tap_mode = false;
    public static boolean use_zoom_mode = false;
    public static boolean use_auto_correction = true;


    public static String symbol_set;
    public static String character_set;
    public static String current_charset;
    public static String word_separators;

    public static int char_view_dark_color;
    public static int char_view_light_color;
    public static int default_font_color;
    public static int highlight_font_color;
    public static int background_color;

    public static int longpress_timeout;
    public static int keyboard_width;
    private static int keyboard_height;
    public static float zoom_factor;

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
        if (sharedPrefs.getBoolean(TipeSettings.USE_CUSTOM_CHARSET, false)) {
            character_set = sharedPrefs.getString(TipeSettings.CUSTOM_CHARSET, "");
        } else {
            character_set = sharedPrefs.getString(TipeSettings.CHARSET, context
                    .getResources().getString(R.string.defaultCharset));
        }

        current_charset = character_set;

        word_separators = context.getResources().getString(
                R.string.word_separators);
        // ===== COLORS=====================
        background_color = sharedPrefs.getInt(TipeSettings.BACKGROUND_COLOR,
                Color.BLACK);
        char_view_dark_color = background_color;
        //sharedPrefs.getInt( TipeSettings.CHARACTER_BG_DARK, Color.BLACK);
        char_view_light_color = sharedPrefs.getInt(
                TipeSettings.CHARACTER_BG_LIGHT, Color.DKGRAY);
        default_font_color = sharedPrefs.getInt(TipeSettings.FONT_COLOR,
                Color.WHITE);
        highlight_font_color = sharedPrefs.getInt(TipeSettings.HIGH_FONT_COLOR,
                Color.YELLOW);

        // ====== BOOLEANS ===
        use_tap_tap_mode = sharedPrefs.getBoolean(
                TipeSettings.USE_TAP_TAP_MODE, false);

        use_haptic_feedback = sharedPrefs.getBoolean(
                TipeSettings.USE_HAPTIC_FEEDBACK, false);
        use_auto_capitalization = sharedPrefs.getBoolean(
                TipeSettings.USE_AUTO_CAPITALIZATION, true);
        show_suggestions = sharedPrefs.getBoolean(
                TipeSettings.SHOW_SUGGESTIONS, true);
        is_symbol_set = false;
        no_boundries = sharedPrefs.getBoolean(
                TipeSettings.CHARACTER_AREA_NO_BOUNDS, true);

        use_auto_correction = sharedPrefs.getBoolean(TipeSettings.AUTO_CORRECTION,true);

        // ======= ADVANCED ===
        longpress_timeout = (int) (250 + 500 * sharedPrefs.getFloat(
                TipeSettings.LONGPRESS_TIMEOUT, (float) 0.5));
        zoom_factor = (float) (1 + sharedPrefs.getFloat(TipeSettings.ZOOM_FACTOR, (float) 0.5)*2);
        mTipeView = tipeView;

        // ====== Keyboard Size (TESTING) !====
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        size.x = wm.getDefaultDisplay().getWidth();
        size.y = wm.getDefaultDisplay().getHeight();
        // Compatibility with older versions!
        keyboard_height = (int) (size.y * (0.3 + 0.3 * sharedPrefs.getFloat(
                TipeSettings.KEYBOARD_HEIGHT, 1)));
        keyboard_width = size.x; // custom width might be a feature one day
        character_view_height = (int) (0.60 * keyboard_height);
        lower_bar_view_height = (int) (0.20 * keyboard_height);
        upper_bar_view_height = (int) (0.20 * keyboard_height);
        default_font_size = (int) (30 + 10 * (sharedPrefs.getFloat(
                TipeSettings.KEYBOARD_HEIGHT, 1) + sharedPrefs.getFloat(
                TipeSettings.KEYBOARD_WIDTH, 1)));
        handleShift();

    }

    public static void handleShift() {

		/*
         * String.toUpperCase() is handling the German "ß" wrong (replaces it
		 * with "SS")... so this is a rather bad 'n' dirty fix..but i have no
		 * other idea than rewriting the toUpperCase() function
		 */
        if (!is_symbol_set) {
            if (shift_state) {

                String cs = current_charset.replace('ß', '\uffff');
                current_charset = cs.toUpperCase(Locale.GERMAN)
                        .replace('\uffff', 'ß');

			/*
             * note this keyboard is for german use right now.. work on locale
			 * one day @ben
			 */

            } else {

                String cs = current_charset.replace('ß', '\uffff');
                current_charset = cs.toLowerCase(Locale.GERMAN)
                        .replace('\uffff', 'ß');
            }
            charset_changed = true;
            mTipeView.invalidate();
        }

    }

    public static void handleSym() {
        charset_changed = true;
        is_symbol_set = !is_symbol_set;
        if (is_symbol_set) {
            current_charset = symbol_set;
        } else {
            current_charset = character_set;
        }
        mTipeView.invalidate();
    }

}
