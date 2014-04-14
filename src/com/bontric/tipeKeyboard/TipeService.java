/**
 *@name SoftKeyboard
 *@author Benedikt John Wieder, Jakob Frick
 *
 * Implementation of the Input Method Service.
 * Handles the IME life cycle and text input.
 * 
 *  
 */
package com.bontric.tipeKeyboard;

import android.annotation.SuppressLint;
import android.inputmethodservice.InputMethodService;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;

import com.bontric.tipeSettings.TipeSettings;

public class TipeService extends InputMethodService {

	static final boolean DEBUG = false;
	Vibrator mVibrator;
	TipeView mTipeView;

	boolean showCandidates;
	
	public void onCreate() {
		super.onCreate();
		getResources();
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
	}

	@SuppressLint("NewApi")
	public View onCreateInputView() {
		/*
		 * reset settings -> Handles changes in preferences
		 */
		KeyboardHandler.input_connection.setIMS(this);
		/*
		 * Initialize all Views within the TipeViewLayout
		 */
		mTipeView = null;
		mTipeView = (TipeView) this.getLayoutInflater().inflate(
				R.layout.tipe_view, null);
		mTipeView.init();

		return mTipeView;
	}

	public void onFinishInput() {
		super.onFinishInput();
		KeyboardHandler.input_connection.resetComposedWord();
	}

	public void onFinishInputView(boolean finishingInput) {
		super.onFinishInputView(finishingInput);

	}

	public void onInitializeInterface() {

	}

	public Vibrator getCurVibrator(){
		return mVibrator;
	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {

		super.onStartInput(attribute, restarting);
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
		/*
		 * Initalize the keyboard if the settings have changed
		 */
		
		// Show candidates for text or in doubt
	/*	showCandidates = 
		(attribute.inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT ||
		(attribute.inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_NULL ||
		(attribute.inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_TEXT_VARIATION_NORMAL ;*/
		
		if (true) {
			mTipeView.initKeyboardHandler(this);
			mTipeView.init();
			TipeSettings.settings_changed = false;
		}
	}

	@Override
	public void onDisplayCompletions(CompletionInfo[] completions) {
	}

	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd,
			int newSelStart, int newSelEnd, int candidatesStart,
			int candidatesEnd) {

	}

	@Override
	public View onCreateCandidatesView() {
		// Create Candites view
		if(!showCandidates) 
			return null;
		else
			return KeyboardHandler.input_connection.initCandidateView(this);
	}
	
	public void onExtractedCursorMovement (int dx, int dy){
		Log.d("onExtractedCursorMovement", "Here is cursor movment");
	}

}
