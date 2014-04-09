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
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;

public class TipeService extends InputMethodService {

	static final boolean DEBUG = false;
	Vibrator mVibrator;
	TipeView mTipeView;

	public void onCreate() {
		super.onCreate();
		getResources();
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		KeyboardHandler.input_connection.setIMS(this);
	}

	@SuppressLint("NewApi")
	public View onCreateInputView() {
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
		mTipeView.init();
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
		//Create Candites view 
		return KeyboardHandler.input_connection.initCandidateView(this);
	}

}
