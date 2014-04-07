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
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;



public class SoftKeyboard extends InputMethodService {

	static final boolean DEBUG = false;
	
	public void onCreate() {
		super.onCreate();
		getResources();
		/*
		 * loads default settings on first start! ducking important..
		 */
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		
	}

	@SuppressLint("NewApi")
	public View onCreateInputView() {

		return this.getLayoutInflater().inflate(R.layout.tipe_view, null);
	}
	
        
	public void onFinishInput() {
		super.onFinishInput();
		

	}

	public void onFinishInputView(boolean finishingInput) {
		super.onFinishInputView(finishingInput);
	}

	public void onInitializeInterface() {
	

	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {

		super.onStartInput(attribute, restarting);
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {


	}

	@Override public void onDisplayCompletions(CompletionInfo[] completions) {
	}
	

	
	

	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd,
			int newSelStart, int newSelEnd, int candidatesStart,
			int candidatesEnd) {

	}


}
