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
import android.hardware.Camera.ShutterCallback;
import android.inputmethodservice.Keyboard;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.WindowManager;

public class KeyboardHandler {
	// constant for now -> is going to be a setting
	public static int CHARACTER_VIEW_HEIGHT = 300;
	public static int KEYBOARD_WIDTH;
	public static int LOWER_BAR_VIEW_HEIGHT =100;

	public static boolean shiftStateChanged = false;
	/*
	 * tell handleShift() what to do :)
	 */
	public static boolean shiftState = false;
	public static boolean isSymbolSet = false;
	
	
	public static String SymbolSet;
	public static String CharacterSet;
	public static int CharViewDarkColor = Color.BLACK;
	public static int CharViewLightColor = Color.DKGRAY;
	public static int CharViewFontColor = Color.WHITE;
	private static SharedPreferences sharedPrefs;

	public static InputHandler inputConnection = new InputHandler();
	public static float defaultFontSize = 40; // Make this variable

	public static void setLayoutView(){
		
	}
	
	public static void init(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		KEYBOARD_WIDTH = display.getWidth();
		
		
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		SymbolSet = sharedPrefs.getString(TipeSettings.SYMSET,
				"CHARSET ERROR! REPORT THIS");
		CharacterSet = sharedPrefs.getString(TipeSettings.CHARSET,
				"CHARSET ERROR! REPORT THIS");
		CharViewDarkColor = sharedPrefs.getInt(TipeSettings.CHARACTER_BG_DARK,
				Color.BLACK);
		CharViewDarkColor = sharedPrefs.getInt(TipeSettings.CHARACTER_BG_LIGHT,
				Color.DKGRAY);
		CharViewFontColor = sharedPrefs.getInt(TipeSettings.FONT_COLOR,
				Color.WHITE);
		
		

	}

	public static void handleShift() {
		
		/*
		 * String.toUpperCase() is handling the German "ß" wrong (replaces it
		 * with "SS")... so this is a rather bad 'n' dirty fix..but i have no
		 * other idea than rewriting the toUpperCase() function
		 */
		if (shiftState && !isSymbolSet) {
			
			String cs = CharacterSet.replace('ß', '\uffff');
			CharacterSet = cs.toUpperCase(Locale.GERMAN).replace('\uffff', 'ß');
			
			/*
			 * note this keyboard is for german use right now.. work on locale
			 * one day @ben
			 */
			
		} else {

			String cs = CharacterSet.replace('ß', '\uffff');
			CharacterSet = cs.toLowerCase(Locale.GERMAN).replace('\uffff', 'ß');
		}
		shiftStateChanged = true;
	}
}
